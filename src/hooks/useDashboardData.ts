import { invoke } from "@tauri-apps/api/core";
import { useEffect, useRef, useState } from "react";
import type { DailyRecord, DashboardData } from "../types/dashboard";
import {
  normalizeDashboardData,
  normalizeRecord,
  sanitizeConfigName,
  sanitizeSupplementName,
} from "../utils/dashboard";

export function useDashboardData() {
  const [data, setData] = useState<DashboardData>(normalizeDashboardData());
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const dataRef = useRef(data);

  useEffect(() => {
    dataRef.current = data;
  }, [data]);

  const persistData = async (nextData: DashboardData) => {
    setSaving(true);

    try {
      await invoke("save_dashboard_data", { payload: nextData });
      setError(null);
    } catch (saveError) {
      const message = saveError instanceof Error ? saveError.message : "保存失败";
      setError(message);
    } finally {
      setSaving(false);
    }
  };

  useEffect(() => {
    let cancelled = false;

    async function bootstrap() {
      setLoading(true);

      try {
        const loaded = await invoke<DashboardData>("load_dashboard_data");
        if (!cancelled) {
          setData(normalizeDashboardData(loaded));
          setError(null);
        }
      } catch (loadError) {
        if (!cancelled) {
          const message = loadError instanceof Error ? loadError.message : "读取失败";
          setError(message);
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    }

    void bootstrap();

    return () => {
      cancelled = true;
    };
  }, []);

  const updateRecord = (date: string, updater: (record: DailyRecord) => DailyRecord) => {
    try {
      const current = dataRef.current;
      const currentRecord = normalizeRecord(
        date,
        current.recordsByDate?.[date],
        current.supplementsConfig,
      );
      const updatedRecord = updater(currentRecord);
      const nextRecord = normalizeRecord(
        date,
        {
          ...updatedRecord,
          updatedAt: new Date().toISOString(),
        },
        current.supplementsConfig,
      );
      const nextData = normalizeDashboardData({
        ...current,
        recordsByDate: {
          ...(current.recordsByDate ?? {}),
          [date]: nextRecord,
        },
      });

      dataRef.current = nextData;
      setData(nextData);
      setError(null);
      void persistData(nextData);
    } catch (updateError) {
      const message = updateError instanceof Error ? updateError.message : "更新记录失败";
      setError(message);
    }
  };

  const addSupplement = (name: string) => {
    const normalizedName = sanitizeSupplementName(name);

    if (!normalizedName) {
      setError("请输入补剂名称");
      return false;
    }

    if (data.supplementsConfig?.includes(normalizedName)) {
      setError("该补剂已存在");
      return false;
    }

    try {
      const current = dataRef.current;
      const supplementsConfig = [...(current.supplementsConfig ?? []), normalizedName];
      const recordsByDate = Object.fromEntries(
        Object.entries(current.recordsByDate ?? {}).map(([date, record]) => [
          date,
          normalizeRecord(
            date,
            {
              ...record,
              supplements: {
                ...(record.supplements ?? {}),
                [normalizedName]: false,
              },
            },
            supplementsConfig,
          ),
        ]),
      );
      const nextData = normalizeDashboardData({
        ...current,
        supplementsConfig,
        recordsByDate,
      });

      dataRef.current = nextData;
      setData(nextData);
      setError(null);
      void persistData(nextData);
      return true;
    } catch (addError) {
      const message = addError instanceof Error ? addError.message : "新增补剂失败";
      setError(message);
      return false;
    }
  };

  const addTrainingType = (name: string) => {
    const normalizedName = sanitizeConfigName(name);

    if (!normalizedName) {
      setError("请输入训练类型名称");
      return false;
    }

    if (data.trainingTypesConfig?.includes(normalizedName)) {
      setError("该训练类型已存在");
      return false;
    }

    try {
      const current = dataRef.current;
      const nextData = normalizeDashboardData({
        ...current,
        trainingTypesConfig: [...(current.trainingTypesConfig ?? []), normalizedName],
      });

      dataRef.current = nextData;
      setData(nextData);
      setError(null);
      void persistData(nextData);
      return true;
    } catch (addError) {
      const message = addError instanceof Error ? addError.message : "新增训练类型失败";
      setError(message);
      return false;
    }
  };

  const removeTrainingType = (name: string) => {
    const normalizedName = sanitizeConfigName(name);

    if (!normalizedName) {
      setError("训练类型名称无效");
      return false;
    }

    const current = dataRef.current;
    const currentConfig = Array.isArray(current.trainingTypesConfig)
      ? current.trainingTypesConfig
      : [];

    if (!currentConfig.includes(normalizedName)) {
      setError("训练类型不存在");
      return false;
    }

    if (currentConfig.length <= 1) {
      setError("至少保留一项训练类型");
      return false;
    }

    try {
      const trainingTypesConfig = currentConfig.filter((item) => item !== normalizedName);
      const recordsByDate = Object.fromEntries(
        Object.entries(current.recordsByDate ?? {}).map(([date, record]) => [
          date,
          normalizeRecord(
            date,
            {
              ...record,
              trainingTypes: (record.trainingTypes ?? []).filter((item) => item !== normalizedName),
            },
            current.supplementsConfig,
          ),
        ]),
      );
      const nextData = normalizeDashboardData({
        ...current,
        trainingTypesConfig,
        recordsByDate,
      });

      dataRef.current = nextData;
      setData(nextData);
      setError(null);
      void persistData(nextData);
      return true;
    } catch (removeError) {
      const message = removeError instanceof Error ? removeError.message : "删除训练类型失败";
      setError(message);
      return false;
    }
  };

  const removeSupplement = (name: string) => {
    const normalizedName = sanitizeSupplementName(name);

    if (!normalizedName) {
      setError("补剂名称无效");
      return false;
    }

    const current = dataRef.current;
    const currentConfig = Array.isArray(current.supplementsConfig)
      ? current.supplementsConfig
      : [];

    if (!currentConfig.includes(normalizedName)) {
      setError("补剂不存在");
      return false;
    }

    if (currentConfig.length <= 1) {
      setError("至少保留一项补剂");
      return false;
    }

    try {
      const supplementsConfig = currentConfig.filter((item) => item !== normalizedName);
      const recordsByDate = Object.fromEntries(
        Object.entries(current.recordsByDate ?? {}).map(([date, record]) => {
          const nextSupplements = { ...(record.supplements ?? {}) };
          delete nextSupplements[normalizedName];

          return [
            date,
            normalizeRecord(
              date,
              {
                ...record,
                supplements: nextSupplements,
              },
              supplementsConfig,
            ),
          ];
        }),
      );
      const nextData = normalizeDashboardData({
        ...current,
        supplementsConfig,
        recordsByDate,
      });

      dataRef.current = nextData;
      setData(nextData);
      setError(null);
      void persistData(nextData);
      return true;
    } catch (removeError) {
      const message = removeError instanceof Error ? removeError.message : "删除补剂失败";
      setError(message);
      return false;
    }
  };

  const importData = async (filePath: string) => {
    setSaving(true);

    try {
      const imported = await invoke<DashboardData>("import_dashboard_data", { filePath });
      const nextData = normalizeDashboardData(imported);
      dataRef.current = nextData;
      setData(nextData);
      setError(null);
      return true;
    } catch (importError) {
      const message = importError instanceof Error ? importError.message : "导入失败";
      setError(message);
      return false;
    } finally {
      setSaving(false);
    }
  };

  const exportData = async (filePath: string) => {
    setSaving(true);

    try {
      await invoke("export_dashboard_data", { payload: dataRef.current, filePath });
      setError(null);
      return true;
    } catch (exportError) {
      const message = exportError instanceof Error ? exportError.message : "导出失败";
      setError(message);
      return false;
    } finally {
      setSaving(false);
    }
  };

  return {
    data,
    loading,
    saving,
    error,
    updateRecord,
    addTrainingType,
    removeTrainingType,
    addSupplement,
    removeSupplement,
    importData,
    exportData,
  };
}
