type DataManagementPanelProps = {
  onImport: () => void;
  onExport: () => void;
  disabled?: boolean;
};

export function DataManagementPanel({
  onImport,
  onExport,
  disabled = false,
}: DataManagementPanelProps) {
  return (
    <section className="rounded-[26px] border border-[#dce4d5] bg-[#f9fbf6]/96 p-4 shadow-[0_14px_30px_rgba(83,104,77,0.07)]">
      <div className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
        <div>
          <p className="text-xs text-[#7d897a]">数据</p>
          <h2 className="mt-1 text-lg font-semibold text-[#243127]">数据管理</h2>
          <p className="mt-1.5 text-sm text-[#71816f]">
            导入会覆盖当前本地数据。导出会包含每日记录、补剂配置和备注。
          </p>
        </div>
        <div className="flex flex-wrap gap-3">
          <button
            type="button"
            onClick={onImport}
            disabled={disabled}
            className="rounded-[18px] border border-[#d9e2d5] bg-[#ffffff] px-4 py-2.5 text-sm text-[#4a5e4d] transition hover:border-[#cfd8ca] hover:bg-[#f8faf5] disabled:cursor-not-allowed disabled:opacity-50"
          >
            导入 JSON
          </button>
          <button
            type="button"
            onClick={onExport}
            disabled={disabled}
            className="rounded-[18px] border border-[#cfe2d0] bg-[#edf7ef] px-4 py-2.5 text-sm text-[#3e7248] transition hover:border-[#bed9c1] hover:bg-[#e7f3e8] disabled:cursor-not-allowed disabled:opacity-50"
          >
            导出 JSON
          </button>
        </div>
      </div>
    </section>
  );
}
