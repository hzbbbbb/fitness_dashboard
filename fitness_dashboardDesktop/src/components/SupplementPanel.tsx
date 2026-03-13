import type { DailyRecord, SupplementName } from "../types/dashboard";
import { countCheckedSupplements } from "../utils/dashboard";

type SupplementPanelProps = {
  record: DailyRecord;
  supplementsConfig: SupplementName[];
  onToggleSupplement: (key: SupplementName) => void;
};

export function SupplementPanel({
  record,
  supplementsConfig,
  onToggleSupplement,
}: SupplementPanelProps) {
  const safeConfig = Array.isArray(supplementsConfig) ? supplementsConfig : [];
  const checkedCount = countCheckedSupplements(record, safeConfig);

  return (
    <section className="flex h-full min-h-0 flex-col rounded-[26px] border border-[#dce4d5] bg-[#f9fbf6]/96 p-4 shadow-[0_14px_30px_rgba(83,104,77,0.07)]">
      <div className="mb-4 flex items-center justify-between">
        <div>
          <p className="text-xs text-[#7d897a]">补剂</p>
          <h2 className="mt-1 text-lg font-semibold text-[#243127]">今日补剂</h2>
        </div>
        <span className="rounded-full border border-[#d8e5d8] bg-[#eef6ec] px-3 py-1 text-sm text-[#52725a]">
          {checkedCount}/{safeConfig.length}
        </span>
      </div>

      <div className="min-h-0 flex-1 overflow-hidden rounded-[22px] border border-[#e3eadf] bg-[#f6faf3]/85 px-2 py-2">
        <div className="fitboard-scrollbar h-full space-y-2 overflow-y-auto pr-1">
          {safeConfig.length === 0 ? (
            <div className="rounded-[18px] border border-dashed border-[#d8dfd2] px-4 py-5 text-sm text-[#8e988c]">
              还没有补剂配置，请前往设置页添加。
            </div>
          ) : null}
          {safeConfig.map((key) => {
            const checked = Boolean(record.supplements?.[key]);

            return (
              <button
                key={key}
                type="button"
                onClick={() => {
                  if (typeof onToggleSupplement === "function") {
                    onToggleSupplement(key);
                  }
                }}
                className={`flex w-full items-center justify-between rounded-[18px] border px-3.5 py-2.5 text-left transition ${
                  checked
                    ? "border-[#cbe0cd] bg-[#edf7ef] text-[#25472d]"
                    : "border-[#dde4d9] bg-[#ffffff] text-[#5f6d5f] hover:border-[#cfd8ca] hover:bg-[#f8faf5]"
                }`}
              >
                <div className="flex items-center gap-3">
                  <span
                    className={`flex h-5 w-5 items-center justify-center rounded-full border text-[10px] ${
                      checked
                        ? "border-[#b9d7bc] bg-[#dff0e2] text-[#387147]"
                        : "border-[#d5ddd2] bg-[#f7f8f3] text-[#9aa497]"
                    }`}
                  >
                    {checked ? "✓" : ""}
                  </span>
                  <span className="font-medium">{key}</span>
                </div>
                <span
                  className={`rounded-full px-2.5 py-1 text-xs ${
                    checked
                      ? "bg-[#dff0e2] text-[#3e7248]"
                      : "bg-[#f1f3ee] text-[#879185]"
                  }`}
                >
                  {checked ? "已吃" : "未吃"}
                </span>
              </button>
            );
          })}
        </div>
      </div>
    </section>
  );
}
