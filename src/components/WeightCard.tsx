import type { DailyRecord, WeightEntry } from "../types/dashboard";
import {
  formatWeight,
  formatWeightDelta,
} from "../utils/dashboard";

type WeightCardProps = {
  record: DailyRecord;
  previousEntry: WeightEntry | null;
  onOpenTrend: () => void;
};

export function WeightCard({
  record,
  previousEntry,
  onOpenTrend,
}: WeightCardProps) {
  const currentWeight = record.fastedWeight;
  const delta =
    typeof currentWeight === "number" && previousEntry
      ? Math.round((currentWeight - previousEntry.weight) * 10) / 10
      : null;

  return (
    <article
      role="button"
      tabIndex={0}
      onClick={onOpenTrend}
      onKeyDown={(event) => {
        if (event.key === "Enter" || event.key === " ") {
          event.preventDefault();
          onOpenTrend();
        }
      }}
      className="flex h-full cursor-pointer flex-col rounded-[24px] border border-[#dce4d5] bg-[#f9fbf6]/96 p-4 shadow-[0_14px_30px_rgba(83,104,77,0.07)] transition hover:border-[#d2ddd0] hover:bg-[#fbfcf8]"
    >
      <div className="flex items-start justify-between gap-3">
        <div>
          <p className="text-xs text-[#7d897a]">体重</p>
          <h2 className="mt-1 text-base font-semibold text-[#243127]">空腹体重</h2>
        </div>
        <span className="rounded-full border border-[#d8e5d8] bg-[#eef6ec] px-2.5 py-1 text-xs text-[#52725a]">
          趋势
        </span>
      </div>

      <div className="mt-auto pt-3">
        <div className="text-[30px] font-semibold tracking-tight text-[#243127]">
          {formatWeight(currentWeight)}
        </div>
        <div className="mt-2 flex items-center justify-between gap-3 text-sm">
          <span className="text-[#7f8b7c]">
            {delta === null ? "暂无历史对比" : "较上次变化"}
          </span>
          {delta === null ? null : (
            <span className={delta <= 0 ? "text-[#4f7e58]" : "text-[#8a6c54]"}>
              {formatWeightDelta(delta)}
            </span>
          )}
        </div>
        <p className="mt-3 text-xs text-[#8c978a]">点击查看趋势并录入 {record.date}</p>
      </div>
    </article>
  );
}
