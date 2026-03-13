import { useEffect, useMemo, useState } from "react";
import type { DailyRecord, WeightEntry } from "../types/dashboard";
import {
  formatWeight,
  getWeightEntries,
  parseDateKey,
  sanitizeWeightInput,
} from "../utils/dashboard";

type WeightTrendDialogProps = {
  open: boolean;
  selectedDate: string;
  record: DailyRecord;
  recordsByDate: Record<string, DailyRecord>;
  onSaveWeight: (weight: number | undefined) => void;
  onClose: () => void;
};

const PERIOD_OPTIONS = [
  { value: 7, label: "7 天" },
  { value: 30, label: "30 天" },
  { value: 90, label: "90 天" },
] as const;

function formatShortDate(date: string): string {
  return new Intl.DateTimeFormat("zh-CN", {
    month: "numeric",
    day: "numeric",
  }).format(parseDateKey(date));
}

function buildChartPath(points: Array<{ x: number; y: number }>): string {
  return points.map((point, index) => `${index === 0 ? "M" : "L"} ${point.x} ${point.y}`).join(" ");
}

function getChartPoints(entries: WeightEntry[]) {
  const chartWidth = 640;
  const chartHeight = 220;
  const padding = { top: 20, right: 18, bottom: 28, left: 18 };

  if (entries.length === 0) {
    return {
      chartWidth,
      chartHeight,
      points: [],
      ticks: [],
      yTicks: [],
      path: "",
    };
  }

  const weights = entries.map((entry) => entry.weight);
  const minWeight = Math.min(...weights);
  const maxWeight = Math.max(...weights);
  const span = Math.max(maxWeight - minWeight, 0.8);
  const paddedMin = minWeight - 0.3;
  const paddedMax = paddedMin + span + 0.6;
  const drawableWidth = chartWidth - padding.left - padding.right;
  const drawableHeight = chartHeight - padding.top - padding.bottom;

  const points = entries.map((entry, index) => {
    const ratioX = entries.length === 1 ? 0.5 : index / (entries.length - 1);
    const ratioY = (entry.weight - paddedMin) / (paddedMax - paddedMin);

    return {
      x: padding.left + ratioX * drawableWidth,
      y: padding.top + drawableHeight - ratioY * drawableHeight,
      label: entry.date,
      weight: entry.weight,
    };
  });

  const tickIndexes = Array.from(new Set([0, Math.floor((entries.length - 1) / 2), entries.length - 1]));
  const ticks = tickIndexes.map((index) => ({
    x: points[index]?.x ?? padding.left,
    label: formatShortDate(entries[index]?.date ?? entries[0].date),
  }));

  const yTicks = [0, 0.5, 1].map((ratio) => {
    const value = paddedMax - (paddedMax - paddedMin) * ratio;
    return {
      y: padding.top + drawableHeight * ratio,
      label: `${value.toFixed(1)} kg`,
    };
  });

  return {
    chartWidth,
    chartHeight,
    points,
    ticks,
    yTicks,
    path: buildChartPath(points),
  };
}

export function WeightTrendDialog({
  open,
  selectedDate,
  record,
  recordsByDate,
  onSaveWeight,
  onClose,
}: WeightTrendDialogProps) {
  const [period, setPeriod] = useState<7 | 30 | 90>(30);
  const [draft, setDraft] = useState("");

  useEffect(() => {
    if (!open) {
      return;
    }

    const handleEscape = (event: KeyboardEvent) => {
      if (event.key === "Escape") {
        onClose();
      }
    };

    window.addEventListener("keydown", handleEscape);
    return () => {
      window.removeEventListener("keydown", handleEscape);
    };
  }, [open, onClose]);

  useEffect(() => {
    if (open) {
      setPeriod(30);
      setDraft(typeof record.fastedWeight === "number" ? record.fastedWeight.toFixed(1) : "");
    }
  }, [open, record.fastedWeight, selectedDate]);

  const entries = useMemo(
    () => getWeightEntries(recordsByDate, selectedDate, period),
    [period, recordsByDate, selectedDate],
  );
  const chart = useMemo(() => getChartPoints(entries), [entries]);

  const handleSave = () => {
    const nextWeight = sanitizeWeightInput(draft);

    if (draft.trim() && typeof nextWeight !== "number") {
      setDraft(typeof record.fastedWeight === "number" ? record.fastedWeight.toFixed(1) : "");
      return;
    }

    onSaveWeight(nextWeight);
    setDraft(typeof nextWeight === "number" ? nextWeight.toFixed(1) : "");
  };

  if (!open) {
    return null;
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-[rgba(40,54,40,0.18)] px-4 py-6">
      <div
        className="absolute inset-0"
        onClick={onClose}
        aria-hidden="true"
      />
      <div className="relative w-full max-w-[760px] rounded-[28px] border border-[#dce4d5] bg-[#f9fbf6] p-5 shadow-[0_22px_50px_rgba(83,104,77,0.16)]">
        <div className="flex items-start justify-between gap-4">
          <div>
            <p className="text-xs text-[#7d897a]">体重趋势</p>
            <h2 className="mt-1 text-xl font-semibold text-[#243127]">空腹体重变化</h2>
            <p className="mt-1 text-sm text-[#849080]">截止 {formatShortDate(selectedDate)}</p>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="rounded-full border border-[#d8e5d8] bg-[#eef6ec] px-3 py-1.5 text-sm text-[#52725a] transition hover:bg-[#e7f2e5]"
          >
            关闭
          </button>
        </div>

        <div className="mt-4 flex gap-2">
          {PERIOD_OPTIONS.map((option) => (
            <button
              key={option.value}
              type="button"
              onClick={() => setPeriod(option.value)}
              className={`rounded-full px-3 py-1.5 text-sm transition ${
                period === option.value
                  ? "border border-[#cfe2d0] bg-[#edf7ef] text-[#3e7248]"
                  : "border border-[#e0e6dc] bg-[#ffffff] text-[#71816f] hover:bg-[#f7faf4]"
              }`}
            >
              最近 {option.label}
            </button>
          ))}
        </div>

        <div className="mt-4 rounded-[20px] border border-[#e2e8de] bg-[#f4f8f1] p-4">
          <div className="flex flex-wrap items-end gap-3">
            <label className="min-w-0 flex-1">
              <span className="mb-1 block text-xs text-[#8a9488]">空腹体重 kg</span>
              <input
                inputMode="decimal"
                value={draft}
                onChange={(event) => setDraft(event.currentTarget.value)}
                onKeyDown={(event) => {
                  if (event.key === "Enter") {
                    event.preventDefault();
                    handleSave();
                  }
                }}
                placeholder="例如 68.4"
                className="w-full rounded-[16px] border border-[#dde4d9] bg-[#ffffff] px-3 py-2.5 text-sm text-[#243127] outline-none transition placeholder:text-[#9aa497] focus:border-[#bed9c1] focus:bg-[#fbfcf8]"
              />
            </label>
            <button
              type="button"
              onClick={handleSave}
              className="rounded-[16px] border border-[#d8e5d8] bg-[#eef6ec] px-3 py-2.5 text-sm text-[#3e7248] transition hover:bg-[#e7f2e5]"
            >
              保存
            </button>
            <button
              type="button"
              onClick={() => {
                setDraft("");
                onSaveWeight(undefined);
              }}
              className="rounded-[16px] border border-[#e1e6de] bg-[#f7f8f4] px-3 py-2.5 text-sm text-[#6f7b6e] transition hover:bg-[#f0f3ec]"
            >
              清空
            </button>
          </div>
        </div>

        <div className="mt-5 rounded-[22px] border border-[#e2e8de] bg-[#ffffff] p-4">
          {entries.length === 0 ? (
            <div className="flex h-[260px] items-center justify-center text-sm text-[#8e988c]">
              当前时间范围内还没有体重记录。
            </div>
          ) : (
            <>
              <div className="mb-3 flex items-end justify-between gap-4">
                <div className="text-sm text-[#71816f]">
                  最近一条：<span className="font-medium text-[#243127]">{formatWeight(entries[entries.length - 1]?.weight)}</span>
                </div>
                <div className="text-sm text-[#71816f]">
                  共 {entries.length} 条记录
                </div>
              </div>
              <svg viewBox={`0 0 ${chart.chartWidth} ${chart.chartHeight}`} className="h-[260px] w-full">
                {chart.yTicks.map((tick) => (
                  <g key={tick.label}>
                    <line
                      x1={18}
                      x2={622}
                      y1={tick.y}
                      y2={tick.y}
                      stroke="#e8eee4"
                      strokeWidth="1"
                    />
                    <text x={626} y={tick.y + 4} fontSize="11" fill="#8a9488" textAnchor="end">
                      {tick.label}
                    </text>
                  </g>
                ))}
                <path
                  d={chart.path}
                  fill="none"
                  stroke="#78ae7f"
                  strokeWidth="3"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                />
                {chart.points.map((point) => (
                  <g key={point.label}>
                    <circle cx={point.x} cy={point.y} r="4.5" fill="#78ae7f" />
                    <circle cx={point.x} cy={point.y} r="2.2" fill="#f9fbf6" />
                  </g>
                ))}
                {chart.ticks.map((tick) => (
                  <text
                    key={tick.label}
                    x={tick.x}
                    y={208}
                    fontSize="11"
                    fill="#8a9488"
                    textAnchor="middle"
                  >
                    {tick.label}
                  </text>
                ))}
              </svg>
            </>
          )}
        </div>
      </div>
    </div>
  );
}
