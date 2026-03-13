import type { DailyRecord, TrainingType } from "../types/dashboard";

type TodayPanelProps = {
  record: DailyRecord;
  trainingTypesConfig: TrainingType[];
  onToggleTraining: (type: TrainingType) => void;
  onWorkoutCompletedChange: (checked: boolean) => void;
  onNoteChange: (note: string) => void;
};

export function TodayPanel({
  record,
  trainingTypesConfig,
  onToggleTraining,
  onWorkoutCompletedChange,
  onNoteChange,
}: TodayPanelProps) {
  const trainingTypes = Array.isArray(record.trainingTypes) ? record.trainingTypes : [];
  const safeConfig = Array.isArray(trainingTypesConfig) ? trainingTypesConfig : [];
  const note = typeof record.note === "string" ? record.note : "";

  return (
    <section className="flex h-full min-h-0 flex-col rounded-[26px] border border-[#dce4d5] bg-[#f9fbf6]/96 p-4 shadow-[0_14px_30px_rgba(83,104,77,0.07)]">
      <div className="mb-4 flex items-center justify-between gap-3">
        <div>
          <p className="text-xs text-[#7d897a]">训练</p>
          <h2 className="mt-1 text-lg font-semibold text-[#243127]">今日训练</h2>
        </div>
        <label className="flex cursor-pointer items-center gap-2 rounded-full border border-[#cfe2d0] bg-[#edf7ef] px-3 py-1.5 text-sm text-[#3e7248]">
          <input
            type="checkbox"
            checked={record.trainingCompleted}
            onChange={(event) => onWorkoutCompletedChange(event.currentTarget.checked)}
            className="h-4 w-4 accent-[#6aa373]"
          />
          已完成
        </label>
      </div>

      <div className="mb-3 flex min-h-0 flex-1 flex-col">
        <p className="mb-2 text-sm text-[#667263]">训练类型</p>
        <div className="min-h-0 flex-1 overflow-hidden rounded-[22px] border border-[#e3eadf] bg-[#f6faf3]/85 px-2 py-2">
          <div className="fitboard-scrollbar h-full space-y-2 overflow-y-auto pr-1">
            {safeConfig.length === 0 ? (
              <div className="rounded-[18px] border border-dashed border-[#d8dfd2] px-4 py-5 text-sm text-[#8e988c]">
                还没有训练类型配置，请前往设置页添加。
              </div>
            ) : null}
            {safeConfig.map((type) => {
              const active = trainingTypes.includes(type);

              return (
                <button
                  key={type}
                  type="button"
                  onClick={() => onToggleTraining(type)}
                  className={`flex w-full items-center justify-between rounded-[18px] border px-3.5 py-2.5 text-left text-sm transition ${
                    active
                      ? "border-[#cbe0cd] bg-[#edf7ef] text-[#25472d]"
                      : "border-[#dde4d9] bg-[#ffffff] text-[#5f6d5f] hover:border-[#cfd8ca] hover:bg-[#f8faf5]"
                  }`}
                >
                  <span className="flex items-center gap-3">
                    <span
                      className={`flex h-5 w-5 items-center justify-center rounded-full border text-[10px] ${
                        active
                          ? "border-[#b9d7bc] bg-[#dff0e2] text-[#387147]"
                          : "border-[#d5ddd2] bg-[#f7f8f3] text-[#9aa497]"
                      }`}
                    >
                      {active ? "✓" : ""}
                    </span>
                    <span className="font-medium">{type}</span>
                  </span>
                  <span
                    className={`rounded-full px-2.5 py-1 text-xs ${
                      active
                        ? "bg-[#dff0e2] text-[#3e7248]"
                        : "bg-[#f1f3ee] text-[#879185]"
                    }`}
                  >
                    {active ? "已选" : "未选"}
                  </span>
                </button>
              );
            })}
          </div>
        </div>
      </div>

      <div className="mt-auto">
        <div className="mb-2 flex items-center justify-between">
          <p className="text-sm text-[#667263]">备注</p>
          <span className="text-xs text-[#9aa497]">{note.length}/140</span>
        </div>
        <textarea
          value={note}
          maxLength={140}
          onChange={(event) => onNoteChange(event.currentTarget.value)}
          placeholder="记录今日状态、训练强度或者恢复感受..."
          className="min-h-24 w-full resize-none rounded-[20px] border border-[#dde4d9] bg-[#ffffff] px-4 py-3 text-sm text-[#243127] outline-none transition placeholder:text-[#9aa497] focus:border-[#bed9c1] focus:bg-[#fbfcf8]"
        />
      </div>
    </section>
  );
}
