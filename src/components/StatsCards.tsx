import type { ReactNode } from "react";
import type { DashboardStats } from "../types/dashboard";

type StatsCardsProps = {
  stats: DashboardStats;
  extraCard?: ReactNode;
};

const items = [
  { key: "weeklyTrainingCount", label: "本周训练次数", suffix: "次" },
  { key: "currentStreak", label: "当前连续打卡", suffix: "天" },
  { key: "completionRate7d", label: "最近 7 天完成率", suffix: "%" },
] as const;

export function StatsCards({ stats, extraCard }: StatsCardsProps) {
  return (
    <section className="grid auto-rows-fr gap-3 sm:grid-cols-2">
      {items.map((item) => (
        <article
          key={item.key}
          className="flex h-full flex-col rounded-[24px] border border-[#dce4d5] bg-[#f9fbf6]/96 p-4 shadow-[0_14px_30px_rgba(83,104,77,0.07)]"
        >
          <p className="text-xs text-[#7d897a]">{item.label}</p>
          <div className="mt-auto pt-3 flex items-end gap-1.5">
            <span className="text-[32px] font-semibold tracking-tight text-[#243127]">
              {stats[item.key]}
            </span>
            <span className="pb-1 text-sm text-[#879185]">{item.suffix}</span>
          </div>
        </article>
      ))}
      {extraCard}
    </section>
  );
}
