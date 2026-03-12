import { useEffect, useMemo, useRef, useState } from "react";
import type { HeatmapCellData } from "../types/dashboard";
import {
  formatDateWithWeekday,
  getHeatmapLevelStyles,
  getMonthAnchors,
  getScoreText,
} from "../utils/dashboard";

type HeatmapCalendarProps = {
  cells: HeatmapCellData[];
  selectedDate: string;
  onSelectDate: (date: string) => void;
};

const weekLabels = ["Mon", "Wed", "Fri"];
const HEATMAP_GAP = 5;
const HEATMAP_MIN_CELL_SIZE = 14;
const HEATMAP_MAX_CELL_SIZE = 18;

export function HeatmapCalendar({
  cells,
  selectedDate,
  onSelectDate,
}: HeatmapCalendarProps) {
  const [hoveredDate, setHoveredDate] = useState<string | null>(null);
  const [cellSize, setCellSize] = useState(HEATMAP_MIN_CELL_SIZE);
  const gridWrapperRef = useRef<HTMLDivElement | null>(null);
  const safeCells = Array.isArray(cells) ? cells : [];

  const weeks = useMemo(() => {
    const grouped: HeatmapCellData[][] = [];

    for (let index = 0; index < safeCells.length; index += 7) {
      grouped.push(safeCells.slice(index, index + 7));
    }

    return grouped;
  }, [safeCells]);

  const monthAnchors = useMemo(() => getMonthAnchors(weeks), [weeks]);
  const totalWeeks = Math.max(weeks.length, 1);
  const hoveredCell =
    safeCells.find((cell) => cell.date === hoveredDate) ??
    safeCells.find((cell) => cell.date === selectedDate);

  useEffect(() => {
    const element = gridWrapperRef.current;

    if (!element) {
      return;
    }

    const updateCellSize = (width: number) => {
      const safeWidth = Math.max(width, 0);
      const totalGapWidth = Math.max(totalWeeks - 1, 0) * HEATMAP_GAP;
      const nextCellSize = Math.floor((safeWidth - totalGapWidth) / totalWeeks);
      const boundedCellSize = Math.max(
        HEATMAP_MIN_CELL_SIZE,
        Math.min(HEATMAP_MAX_CELL_SIZE, nextCellSize),
      );

      setCellSize(boundedCellSize);
    };

    updateCellSize(element.clientWidth);

    const observer = new ResizeObserver((entries) => {
      const entry = entries[0];

      if (!entry) {
        return;
      }

      updateCellSize(entry.contentRect.width);
    });

    observer.observe(element);

    return () => {
      observer.disconnect();
    };
  }, [totalWeeks]);

  const heatmapGridStyle = {
    gridTemplateColumns: `repeat(${totalWeeks}, ${cellSize}px)`,
  } as const;
  const heatmapTrackWidth = totalWeeks * cellSize + Math.max(totalWeeks - 1, 0) * HEATMAP_GAP;

  return (
    <section className="rounded-[28px] border border-[#dce4d5] bg-[#f9fbf6]/96 p-4 shadow-[0_16px_36px_rgba(83,104,77,0.08)] sm:p-5">
      <div className="mb-2 flex justify-end">
        <span className="text-xs text-[#8a9488]">
          {hoveredCell ? `${formatDateWithWeekday(hoveredCell.date)} · ${getScoreText(hoveredCell.score)}` : ""}
        </span>
      </div>

      <div className="min-w-0">
        <div className="grid w-full grid-cols-[30px_minmax(0,1fr)] items-start gap-x-3">
          <div className="flex shrink-0 flex-col pt-6 text-[10px] text-[#95a08f]">
            {Array.from({ length: 7 }, (_, index) => (
              <div
                key={index}
                className="flex items-center last:mb-0"
                style={{
                  height: `${cellSize}px`,
                  marginBottom: index === 6 ? "0px" : `${HEATMAP_GAP}px`,
                }}
              >
                {weekLabels.includes(["Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"][index])
                  ? ["Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"][index]
                  : ""}
              </div>
            ))}
          </div>

          <div ref={gridWrapperRef} className="min-w-0">
            <div
              className="mx-auto mb-2 grid items-end text-[10px] text-[#95a08f]"
              style={{
                ...heatmapGridStyle,
                columnGap: `${HEATMAP_GAP}px`,
                width: `${heatmapTrackWidth}px`,
                maxWidth: "100%",
              }}
            >
              {monthAnchors.map((anchor) => (
                <span
                  key={`${anchor.month}-${anchor.column}`}
                  className="whitespace-nowrap"
                  style={{ gridColumnStart: anchor.column + 1 }}
                >
                  {anchor.month}
                </span>
              ))}
            </div>

            <div
              className="mx-auto grid"
              style={{
                ...heatmapGridStyle,
                columnGap: `${HEATMAP_GAP}px`,
                width: `${heatmapTrackWidth}px`,
                maxWidth: "100%",
              }}
            >
              {weeks.map((week, weekIndex) => (
                <div
                  key={weekIndex}
                  className="grid"
                  style={{ rowGap: `${HEATMAP_GAP}px` }}
                >
                  {week.map((cell) => {
                    const isSelected = cell.date === selectedDate;
                    const colorClass = getHeatmapLevelStyles(cell.score, cell.isInRange);

                    return (
                      <button
                        key={cell.date}
                        type="button"
                        title={`${formatDateWithWeekday(cell.date)} - ${getScoreText(cell.score)}`}
                        onMouseEnter={() => setHoveredDate(cell.date)}
                        onMouseLeave={() => setHoveredDate(null)}
                        onClick={() => cell.isInRange && onSelectDate(cell.date)}
                        className={`shrink-0 rounded-[4px] border transition ${
                          isSelected
                            ? "border-[#6aa373] shadow-[0_0_0_1px_rgba(106,163,115,0.24)]"
                            : "border-[#d8dfd2]"
                        } ${colorClass} ${cell.isInRange ? "cursor-pointer" : "cursor-default"}`}
                        style={{ width: `${cellSize}px`, height: `${cellSize}px` }}
                        aria-label={`${cell.date} ${getScoreText(cell.score)}`}
                        disabled={!cell.isInRange}
                      />
                    );
                  })}
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
