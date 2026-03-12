import { save, open } from "@tauri-apps/plugin-dialog";
import { useMemo, useState } from "react";
import type { AppView } from "./components/AppSidebar";
import { DashboardLayout } from "./components/DashboardLayout";
import { DayDetailPanel } from "./components/DayDetailPanel";
import { ErrorBoundary } from "./components/ErrorBoundary";
import { HeatmapCalendar } from "./components/HeatmapCalendar";
import { SettingsPage } from "./components/SettingsPage";
import { StatsCards } from "./components/StatsCards";
import { SupplementPanel } from "./components/SupplementPanel";
import { TodayPanel } from "./components/TodayPanel";
import { useDashboardData } from "./hooks/useDashboardData";
import type { TrainingType } from "./types/dashboard";
import {
  buildHeatmapCells,
  calculateDashboardStats,
  formatFullDate,
  getDateKey,
  getSafeRecordForDate,
  normalizeDashboardData,
} from "./utils/dashboard";

function App() {
  const {
    data,
    loading,
    saving: syncing,
    error,
    updateRecord,
    addTrainingType,
    removeTrainingType,
    addSupplement,
    removeSupplement,
    importData,
    exportData,
  } = useDashboardData();
  const todayKey = getDateKey(new Date());
  const [selectedDate, setSelectedDate] = useState(todayKey);
  const [actionError, setActionError] = useState<string | null>(null);
  const [currentView, setCurrentView] = useState<AppView>("dashboard");

  const normalizedData = useMemo(() => normalizeDashboardData(data), [data]);
  const heatmapCells = useMemo(
    () => buildHeatmapCells(normalizedData.recordsByDate, normalizedData.supplementsConfig),
    [normalizedData.recordsByDate, normalizedData.supplementsConfig],
  );
  const stats = useMemo(
    () =>
      calculateDashboardStats(
        normalizedData.recordsByDate,
        normalizedData.supplementsConfig,
      ),
    [normalizedData.recordsByDate, normalizedData.supplementsConfig],
  );
  const todayRecord = getSafeRecordForDate(
    todayKey,
    normalizedData.recordsByDate,
    normalizedData.supplementsConfig,
  );
  const selectedRecord = getSafeRecordForDate(
    selectedDate,
    normalizedData.recordsByDate,
    normalizedData.supplementsConfig,
  );

  const handleExport = async () => {
    try {
      const filePath = await save({
        title: "导出 FitBoard 数据",
        defaultPath: `fitboard-${todayKey}.json`,
        filters: [{ name: "JSON", extensions: ["json"] }],
      });

      if (typeof filePath === "string") {
        await exportData(filePath);
        setActionError(null);
      }
    } catch (exportError) {
      console.error("FitBoard export failed:", exportError);
      setActionError(exportError instanceof Error ? exportError.message : "导出失败");
    }
  };

  const handleImport = async () => {
    try {
      const filePath = await open({
        title: "导入 FitBoard 数据",
        multiple: false,
        directory: false,
        filters: [{ name: "JSON", extensions: ["json"] }],
      });

      if (typeof filePath !== "string") {
        return;
      }

      if (!window.confirm("导入会覆盖当前本地数据，是否继续？")) {
        return;
      }

      const imported = await importData(filePath);
      if (imported) {
        setSelectedDate(todayKey);
        setActionError(null);
      }
    } catch (importError) {
      console.error("FitBoard import failed:", importError);
      setActionError(importError instanceof Error ? importError.message : "导入失败");
    }
  };

  return (
    <DashboardLayout
      currentView={currentView}
      onChangeView={setCurrentView}
      title={currentView === "dashboard" ? "FitBoard" : "设置"}
      subtitle={
        currentView === "dashboard"
          ? "今日概览"
          : "管理本地数据与补剂配置"
      }
      dateLabel={formatFullDate(new Date())}
      error={actionError ?? error}
    >
      {currentView === "dashboard" ? (
        <div className="flex flex-1 flex-col gap-4">
          <section className="grid gap-4 xl:grid-cols-[minmax(0,1.45fr)_320px]">
            <ErrorBoundary title="热力图区域异常" description="热力图已降级，其他模块仍可继续使用。">
              <HeatmapCalendar
                cells={heatmapCells}
                selectedDate={selectedDate}
                onSelectDate={(date) => {
                  if (typeof date === "string" && date) {
                    setSelectedDate(date);
                  }
                }}
              />
            </ErrorBoundary>
            <StatsCards stats={stats} />
          </section>

          <section className="grid flex-1 gap-4 xl:grid-cols-[minmax(0,1fr)_minmax(0,1fr)_340px]">
            <ErrorBoundary title="今日交互区域异常" description="训练模块发生错误，已阻止整页黑屏。">
              <TodayPanel
                record={todayRecord}
                trainingTypesConfig={normalizedData.trainingTypesConfig}
                onToggleTraining={(type) => {
                  updateRecord(todayKey, (record) => {
                    const currentTypes = Array.isArray(record.trainingTypes)
                      ? record.trainingTypes
                      : [];
                    const hasType = currentTypes.includes(type);
                    const nextTypes: TrainingType[] = hasType
                      ? currentTypes.filter((item) => item !== type)
                      : [...currentTypes, type];

                    return { ...record, trainingTypes: nextTypes };
                  });
                }}
                onWorkoutCompletedChange={(checked) => {
                  updateRecord(todayKey, (record) => ({
                    ...record,
                    trainingCompleted: Boolean(checked),
                  }));
                }}
                onNoteChange={(note) => {
                  updateRecord(todayKey, (record) => ({
                    ...record,
                    note: typeof note === "string" ? note : "",
                  }));
                }}
              />
            </ErrorBoundary>
            <ErrorBoundary title="补剂区域异常" description="补剂模块发生错误，已阻止整页黑屏。">
              <SupplementPanel
                record={todayRecord}
                supplementsConfig={normalizedData.supplementsConfig}
                onToggleSupplement={(key) => {
                  if (!key) {
                    return;
                  }

                  updateRecord(todayKey, (record) => ({
                    ...record,
                    supplements: {
                      ...(record.supplements ?? {}),
                      [key]: !record.supplements?.[key],
                    },
                  }));
                }}
              />
            </ErrorBoundary>
            <ErrorBoundary title="日期详情区域异常" description="当天详情已降级为空状态。">
              <DayDetailPanel
                date={selectedDate}
                record={selectedRecord}
                supplementsConfig={normalizedData.supplementsConfig}
              />
            </ErrorBoundary>
          </section>
        </div>
      ) : (
        <ErrorBoundary title="设置页异常" description="设置页已降级，Dashboard 仍可继续使用。">
          <SettingsPage
            trainingTypesConfig={normalizedData.trainingTypesConfig}
            onAddTrainingType={addTrainingType}
            onDeleteTrainingType={removeTrainingType}
            supplementsConfig={normalizedData.supplementsConfig}
            onAddSupplement={addSupplement}
            onDeleteSupplement={removeSupplement}
            onImport={() => {
              void handleImport();
            }}
            onExport={() => {
              void handleExport();
            }}
            disabled={loading || syncing}
          />
        </ErrorBoundary>
      )}
    </DashboardLayout>
  );
}

export default App;
