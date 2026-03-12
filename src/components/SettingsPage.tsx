import { useState } from "react";
import type { SupplementName, TrainingType } from "../types/dashboard";
import { DataManagementPanel } from "./DataManagementPanel";

type SettingsPageProps = {
  trainingTypesConfig: TrainingType[];
  onAddTrainingType: (name: string) => boolean;
  onDeleteTrainingType: (name: TrainingType) => boolean;
  supplementsConfig: SupplementName[];
  onAddSupplement: (name: string) => boolean;
  onDeleteSupplement: (name: SupplementName) => boolean;
  onImport: () => void;
  onExport: () => void;
  disabled?: boolean;
};

export function SettingsPage({
  trainingTypesConfig,
  onAddTrainingType,
  onDeleteTrainingType,
  supplementsConfig,
  onAddSupplement,
  onDeleteSupplement,
  onImport,
  onExport,
  disabled = false,
}: SettingsPageProps) {
  const [trainingDraftName, setTrainingDraftName] = useState("");
  const [supplementDraftName, setSupplementDraftName] = useState("");
  const safeTrainingConfig = Array.isArray(trainingTypesConfig) ? trainingTypesConfig : [];
  const safeConfig = Array.isArray(supplementsConfig) ? supplementsConfig : [];

  const handleAddTrainingType = () => {
    if (onAddTrainingType(trainingDraftName)) {
      setTrainingDraftName("");
    }
  };

  const handleAddSupplement = () => {
    if (onAddSupplement(supplementDraftName)) {
      setSupplementDraftName("");
    }
  };

  return (
    <div className="space-y-6">
      <DataManagementPanel onImport={onImport} onExport={onExport} disabled={disabled} />

      <section className="rounded-[26px] border border-[#dce4d5] bg-[#f9fbf6]/96 p-4 shadow-[0_14px_30px_rgba(83,104,77,0.07)]">
        <div className="mb-5">
          <p className="text-xs text-[#7d897a]">训练</p>
          <h2 className="mt-1 text-lg font-semibold text-[#243127]">训练类型管理</h2>
          <p className="mt-1.5 text-sm text-[#71816f]">
            管理首页训练勾选清单。新增后可在今日打卡页直接选择。
          </p>
        </div>

        <div className="mb-4 flex flex-col gap-3 sm:flex-row">
          <input
            type="text"
            value={trainingDraftName}
            onChange={(event) => setTrainingDraftName(event.currentTarget.value)}
            onKeyDown={(event) => {
              if (event.key === "Enter") {
                event.preventDefault();
                handleAddTrainingType();
              }
            }}
            placeholder="新增训练类型"
            className="flex-1 rounded-[18px] border border-[#dde4d9] bg-[#ffffff] px-4 py-2.5 text-sm text-[#243127] outline-none transition placeholder:text-[#9aa497] focus:border-[#bed9c1] focus:bg-[#fbfcf8]"
          />
          <button
            type="button"
            onClick={handleAddTrainingType}
            className="rounded-[18px] border border-[#cfe2d0] bg-[#edf7ef] px-4 py-2.5 text-sm text-[#3e7248] transition hover:border-[#bed9c1] hover:bg-[#e7f3e8]"
          >
            新增训练类型
          </button>
        </div>

        <div className="space-y-3">
          {safeTrainingConfig.length === 0 ? (
            <div className="rounded-[18px] border border-dashed border-[#d8dfd2] px-4 py-5 text-sm text-[#8e988c]">
              当前没有训练类型配置，可先新增一个训练类型。
            </div>
          ) : null}
          {safeTrainingConfig.map((name) => (
            <div
              key={name}
              className="flex items-center justify-between rounded-[18px] border border-[#e0e6dc] bg-[#ffffff] px-4 py-3"
            >
              <div>
                <div className="font-medium text-[#243127]">{name}</div>
                <div className="mt-1 text-xs text-[#8a9488]">用于首页训练打卡清单</div>
              </div>
              <button
                type="button"
                onClick={() => onDeleteTrainingType(name)}
                disabled={disabled || safeTrainingConfig.length <= 1}
                className="rounded-xl border border-rose-200 bg-rose-50 px-3 py-2 text-sm text-rose-500 transition hover:border-rose-300 hover:bg-rose-100 disabled:cursor-not-allowed disabled:opacity-45"
              >
                删除
              </button>
            </div>
          ))}
        </div>
      </section>

      <section className="rounded-[26px] border border-[#dce4d5] bg-[#f9fbf6]/96 p-4 shadow-[0_14px_30px_rgba(83,104,77,0.07)]">
        <div className="mb-5">
          <p className="text-xs text-[#7d897a]">补剂</p>
          <h2 className="mt-1 text-lg font-semibold text-[#243127]">补剂管理</h2>
          <p className="mt-1.5 text-sm text-[#71816f]">
            管理每日补剂清单。新增后会同步到所有日期记录中。
          </p>
        </div>

        <div className="mb-4 flex flex-col gap-3 sm:flex-row">
          <input
            type="text"
            value={supplementDraftName}
            onChange={(event) => setSupplementDraftName(event.currentTarget.value)}
            onKeyDown={(event) => {
              if (event.key === "Enter") {
                event.preventDefault();
                handleAddSupplement();
              }
            }}
            placeholder="新增补剂名称"
            className="flex-1 rounded-[18px] border border-[#dde4d9] bg-[#ffffff] px-4 py-2.5 text-sm text-[#243127] outline-none transition placeholder:text-[#9aa497] focus:border-[#bed9c1] focus:bg-[#fbfcf8]"
          />
          <button
            type="button"
            onClick={handleAddSupplement}
            className="rounded-[18px] border border-[#cfe2d0] bg-[#edf7ef] px-4 py-2.5 text-sm text-[#3e7248] transition hover:border-[#bed9c1] hover:bg-[#e7f3e8]"
          >
            新增补剂
          </button>
        </div>

        <div className="space-y-3">
          {safeConfig.length === 0 ? (
            <div className="rounded-[18px] border border-dashed border-[#d8dfd2] px-4 py-5 text-sm text-[#8e988c]">
              当前没有补剂配置，可先新增一个补剂。
            </div>
          ) : null}
          {safeConfig.map((name) => (
            <div
              key={name}
              className="flex items-center justify-between rounded-[18px] border border-[#e0e6dc] bg-[#ffffff] px-4 py-3"
            >
              <div>
                <div className="font-medium text-[#243127]">{name}</div>
                <div className="mt-1 text-xs text-[#8a9488]">用于首页补剂打卡和热力图统计</div>
              </div>
              <button
                type="button"
                onClick={() => onDeleteSupplement(name)}
                disabled={disabled || safeConfig.length <= 1}
                className="rounded-xl border border-rose-200 bg-rose-50 px-3 py-2 text-sm text-rose-500 transition hover:border-rose-300 hover:bg-rose-100 disabled:cursor-not-allowed disabled:opacity-45"
              >
                删除
              </button>
            </div>
          ))}
        </div>
      </section>

      <section className="rounded-[26px] border border-[#dce4d5] bg-[#f9fbf6]/96 p-4 shadow-[0_14px_30px_rgba(83,104,77,0.07)]">
        <p className="text-xs text-[#7d897a]">说明</p>
        <h2 className="mt-1 text-lg font-semibold text-[#243127]">本地数据</h2>
        <p className="mt-3 leading-7 text-[#5d6a5c]">
          本地数据仅保存在当前设备，可通过导入导出 JSON 进行迁移和备份。
        </p>
      </section>
    </div>
  );
}
