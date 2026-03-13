import type { DailyRecord, SupplementName } from "../types/dashboard";
import {
  TRAINING_TYPES,
  calculateDailyScore,
  createEmptyRecord,
  formatDateWithWeekday,
  formatSupplementCount,
  getScoreText,
  normalizeSupplementsConfig,
} from "../utils/dashboard";

type DayDetailPanelProps = {
  date: string;
  record?: DailyRecord;
  supplementsConfig: SupplementName[];
};

const trainingSet = new Set(TRAINING_TYPES);

export function DayDetailPanel({
  date,
  record,
  supplementsConfig,
}: DayDetailPanelProps) {
  const safeSupplementsConfig = normalizeSupplementsConfig(supplementsConfig);
  const detailRecord = record ?? createEmptyRecord(date, safeSupplementsConfig);
  const score = calculateDailyScore(detailRecord, safeSupplementsConfig);
  const checkedSupplements = safeSupplementsConfig.filter(
    (key) => detailRecord?.supplements[key],
  );

  return (
    <aside className="flex h-full flex-col rounded-[26px] border border-[#dce4d5] bg-[#f9fbf6]/96 p-4 shadow-[0_14px_30px_rgba(83,104,77,0.07)]">
      <div className="mb-4 flex items-center justify-between gap-3">
        <div>
          <p className="text-xs text-[#7d897a]">详情</p>
          <h2 className="mt-1 text-lg font-semibold text-[#243127]">{formatDateWithWeekday(date)}</h2>
        </div>
        <div className="rounded-full border border-[#cfe2d0] bg-[#edf7ef] px-3 py-1.5 text-sm text-[#3e7248]">
          {getScoreText(score)}
        </div>
      </div>

      <div className="grid flex-1 gap-3 text-sm text-[#5d6a5c]">
        <section className="rounded-[18px] border border-[#e0e6dc] bg-[#ffffff] p-3">
          <p className="mb-2 text-xs text-[#8a9488]">训练</p>
          <p className="mb-2 text-[#243127]">
            {detailRecord.trainingCompleted ? "已完成训练" : "未完成训练"}
          </p>
          <div className="flex flex-wrap gap-2">
            {detailRecord.trainingTypes.length ? (
              detailRecord.trainingTypes
                .filter((item) => trainingSet.has(item))
                .map((type) => (
                  <span
                    key={type}
                    className="rounded-full border border-[#d6e7d8] bg-[#eef6ec] px-2.5 py-1 text-xs text-[#406d49]"
                  >
                    {type}
                  </span>
                ))
            ) : (
              <span className="text-[#8e988c]">暂无训练类型</span>
            )}
          </div>
        </section>

        <section className="rounded-[18px] border border-[#e0e6dc] bg-[#ffffff] p-3">
          <p className="mb-2 text-xs text-[#8a9488]">补剂</p>
          <p className="mb-2 text-[#243127]">
            {formatSupplementCount(detailRecord, safeSupplementsConfig)}
          </p>
          <div className="space-y-2">
            {safeSupplementsConfig.map((key) => (
              <div key={key} className="flex items-center justify-between">
                <span>{key}</span>
                <span
                  className={detailRecord.supplements[key] ? "text-[#3e7248]" : "text-[#8e988c]"}
                >
                  {detailRecord.supplements[key] ? "已吃" : "未吃"}
                </span>
              </div>
            ))}
          </div>
        </section>

        <section className="rounded-[18px] border border-[#e0e6dc] bg-[#ffffff] p-3">
          <p className="mb-2 text-xs text-[#8a9488]">备注</p>
          <p className="leading-6 text-[#243127]">
            {detailRecord.note.trim() ? detailRecord.note : "当天没有备注。"}
          </p>
        </section>

        <section className="rounded-[18px] border border-[#e0e6dc] bg-[#ffffff] p-3">
          <p className="mb-2 text-xs text-[#8a9488]">更新</p>
          <p className="text-[#5d6a5c]">{detailRecord.updatedAt || "尚未保存任何记录"}</p>
        </section>

        {checkedSupplements.length === 0 ? null : (
          <section className="rounded-[18px] border border-[#e0e6dc] bg-[#ffffff] p-3">
            <p className="mb-2 text-xs text-[#8a9488]">小结</p>
            <p className="text-[#243127]">
              今天已记录 {checkedSupplements.length} 项补剂，训练状态为
              {detailRecord.trainingCompleted ? "完成" : "未完成"}。
            </p>
          </section>
        )}
      </div>
    </aside>
  );
}
