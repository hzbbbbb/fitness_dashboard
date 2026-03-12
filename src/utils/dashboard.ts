import type {
  DailyRecord,
  DashboardData,
  DashboardStats,
  HeatmapCellData,
  SupplementName,
  TrainingType,
} from "../types/dashboard";

export const TRAINING_TYPES: TrainingType[] = [
  "胸",
  "肩",
  "背",
  "腿",
  "手臂",
  "有氧",
  "休息",
];

export const DEFAULT_TRAINING_TYPES: TrainingType[] = [...TRAINING_TYPES];
export const DEFAULT_SUPPLEMENTS: SupplementName[] = ["蛋白粉", "肌酸", "咖啡因"];

const DAY_MS = 24 * 60 * 60 * 1000;

type LegacyDashboardData = {
  version?: number;
  trainingTypesConfig?: string[];
  supplementsConfig?: string[];
  recordsByDate?: Record<string, Partial<DailyRecord>>;
  records?: Record<string, Partial<DailyRecord> & { workoutCompleted?: boolean }>;
};

type DailyRecordLike = Partial<DailyRecord> & { workoutCompleted?: boolean };

function isObject(value: unknown): value is Record<string, unknown> {
  return typeof value === "object" && value !== null && !Array.isArray(value);
}

function toRecordMap(
  source: LegacyDashboardData["recordsByDate"] | LegacyDashboardData["records"],
): Record<string, DailyRecordLike> {
  if (!isObject(source)) {
    return {};
  }

  return Object.fromEntries(
    Object.entries(source).filter((entry): entry is [string, DailyRecordLike] => {
      const [date, value] = entry;
      return Boolean(date) && isObject(value);
    }),
  );
}

export function getDateKey(date: Date): string {
  const year = date.getFullYear();
  const month = `${date.getMonth() + 1}`.padStart(2, "0");
  const day = `${date.getDate()}`.padStart(2, "0");

  return `${year}-${month}-${day}`;
}

export function sanitizeSupplementName(name: string): string {
  return name.replace(/\s+/g, " ").trim();
}

export function sanitizeConfigName(name: string): string {
  return name.replace(/\s+/g, " ").trim();
}

export function normalizeTrainingTypesConfig(config?: string[]): TrainingType[] {
  const safeConfig = Array.isArray(config) ? config : undefined;
  const candidateList = (safeConfig?.length ? safeConfig : DEFAULT_TRAINING_TYPES).map(
    sanitizeConfigName,
  );
  const unique = Array.from(new Set(candidateList.filter(Boolean)));

  return unique.length ? unique : [...DEFAULT_TRAINING_TYPES];
}

export function normalizeSupplementsConfig(config?: string[]): SupplementName[] {
  const safeConfig = Array.isArray(config) ? config : undefined;
  const candidateList = (safeConfig?.length ? safeConfig : DEFAULT_SUPPLEMENTS).map(
    sanitizeConfigName,
  );
  const unique = Array.from(new Set(candidateList.filter(Boolean)));

  return unique.length ? unique : [...DEFAULT_SUPPLEMENTS];
}

export function createEmptySupplements(
  supplementsConfig: SupplementName[],
): Record<SupplementName, boolean> {
  return Object.fromEntries(supplementsConfig.map((item) => [item, false]));
}

export function createEmptyRecord(
  date: string,
  supplementsConfig: SupplementName[],
): DailyRecord {
  return {
    date,
    trainingCompleted: false,
    trainingTypes: [],
    note: "",
    supplements: createEmptySupplements(supplementsConfig),
    updatedAt: new Date().toISOString(),
  };
}

export function normalizeRecord(
  date: string,
  record: DailyRecordLike | undefined,
  supplementsConfig: SupplementName[],
): DailyRecord {
  const safeRecord = isObject(record) ? record : undefined;
  const trainingTypes = Array.isArray(safeRecord?.trainingTypes)
    ? safeRecord.trainingTypes
    : [];
  const supplements = isObject(safeRecord?.supplements) ? safeRecord.supplements : {};

  return {
    ...createEmptyRecord(date, supplementsConfig),
    ...safeRecord,
    date,
    trainingCompleted:
      safeRecord?.trainingCompleted ?? safeRecord?.workoutCompleted ?? false,
    trainingTypes: Array.from(
      new Set(
        trainingTypes
          .filter((type): type is string => typeof type === "string")
          .map(sanitizeConfigName)
          .filter(Boolean),
      ),
    ),
    supplements: {
      ...createEmptySupplements(supplementsConfig),
      ...Object.fromEntries(
        Object.entries(supplements).map(([key, value]) => [key, Boolean(value)]),
      ),
    },
    note: typeof safeRecord?.note === "string" ? safeRecord.note : "",
    updatedAt:
      typeof safeRecord?.updatedAt === "string" && safeRecord.updatedAt
        ? safeRecord.updatedAt
        : new Date().toISOString(),
  };
}

export function normalizeDashboardData(raw?: LegacyDashboardData): DashboardData {
  const safeRaw = isObject(raw) ? raw : undefined;
  const recordSource = toRecordMap(safeRaw?.recordsByDate ?? safeRaw?.records);
  const trainingNamesFromRecords = Object.values(recordSource).flatMap((record) =>
    Array.isArray(record.trainingTypes) ? record.trainingTypes : [],
  );
  const supplementNamesFromRecords = Object.values(recordSource).flatMap((record) =>
    Object.keys(isObject(record.supplements) ? record.supplements : {}),
  );
  const trainingTypesConfig = normalizeTrainingTypesConfig([
    ...(Array.isArray(safeRaw?.trainingTypesConfig) ? safeRaw.trainingTypesConfig : []),
    ...trainingNamesFromRecords,
  ]);
  const supplementsConfig = normalizeSupplementsConfig([
    ...(Array.isArray(safeRaw?.supplementsConfig) ? safeRaw.supplementsConfig : []),
    ...supplementNamesFromRecords,
  ]);
  const recordsByDate = Object.fromEntries(
    Object.entries(recordSource).map(([date, record]) => [
      date,
      normalizeRecord(date, record, supplementsConfig),
    ]),
  );
  const todayKey = getDateKey(new Date());

  return {
    version: typeof safeRaw?.version === "number" ? safeRaw.version : 3,
    trainingTypesConfig,
    supplementsConfig,
    recordsByDate: Object.keys(recordsByDate).length
      ? recordsByDate
      : { [todayKey]: createEmptyRecord(todayKey, supplementsConfig) },
  };
}

export function getSafeRecordForDate(
  date: string,
  recordsByDate: Record<string, DailyRecord> | undefined,
  supplementsConfig: SupplementName[],
): DailyRecord {
  return normalizeRecord(date, recordsByDate?.[date], supplementsConfig);
}

export function createDefaultDashboardData(): DashboardData {
  return normalizeDashboardData({
    version: 3,
    trainingTypesConfig: DEFAULT_TRAINING_TYPES,
    supplementsConfig: DEFAULT_SUPPLEMENTS,
    recordsByDate: {},
  });
}

export function countCheckedSupplements(
  record: DailyRecord | undefined,
  supplementsConfig: SupplementName[],
): number {
  if (!record) {
    return 0;
  }

  return supplementsConfig.filter((key) => record.supplements?.[key]).length;
}

export function calculateDailyScore(
  record: DailyRecord | undefined,
  supplementsConfig: SupplementName[],
): 0 | 1 | 2 | 3 {
  if (!record) {
    return 0;
  }

  const checkedSupplements = countCheckedSupplements(record, supplementsConfig);
  const hasSupplementsConfigured = supplementsConfig.length > 0;

  let score = record.trainingCompleted ? 1 : 0;

  if (checkedSupplements > 0) {
    score += 1;
  }

  if (hasSupplementsConfigured && checkedSupplements === supplementsConfig.length) {
    score += 1;
  }

  return Math.min(score, 3) as 0 | 1 | 2 | 3;
}

export function getScoreText(score: 0 | 1 | 2 | 3): string {
  switch (score) {
    case 0:
      return "未完成";
    case 1:
      return "完成一部分";
    case 2:
      return "完成较多";
    case 3:
      return "训练和补剂完整";
    default:
      return "未完成";
  }
}

export function getHeatmapLevelStyles(score: 0 | 1 | 2 | 3, isInRange: boolean): string {
  if (!isInRange) {
    return "bg-transparent opacity-30";
  }

  switch (score) {
    case 0:
      return "bg-[#edf2eb]";
    case 1:
      return "bg-[#cfe4cf]";
    case 2:
      return "bg-[#94c79b]";
    case 3:
      return "bg-[#5d9f6a]";
    default:
      return "bg-[#edf2eb]";
  }
}

export function buildHeatmapCells(
  recordsByDate: Record<string, DailyRecord>,
  supplementsConfig: SupplementName[],
  baseDate = new Date(),
): HeatmapCellData[] {
  const end = startOfDay(baseDate);
  const start = new Date(end.getFullYear(), end.getMonth() - 6, 1);
  const alignedStart = startOfWeek(start);
  const cells: HeatmapCellData[] = [];

  for (let time = alignedStart.getTime(); time <= end.getTime(); time += DAY_MS) {
    const currentDate = new Date(time);
    const key = getDateKey(currentDate);
    const isInRange = time >= start.getTime() && time <= end.getTime();
    const record = recordsByDate[key];

    cells.push({
      date: key,
      score: isInRange ? calculateDailyScore(record, supplementsConfig) : 0,
      isInRange,
      record,
    });
  }

  while (cells.length % 7 !== 0) {
    const lastDate = parseDateKey(cells[cells.length - 1].date);
    lastDate.setDate(lastDate.getDate() + 1);

    cells.push({
      date: getDateKey(lastDate),
      score: 0,
      isInRange: false,
    });
  }

  return cells;
}

export function getMonthAnchors(weeks: HeatmapCellData[][]) {
  const anchors: Array<{ month: string; column: number }> = [];
  let lastMonth = "";

  weeks.forEach((week, column) => {
    const firstInRange = week.find((cell) => cell.isInRange);
    if (!firstInRange) {
      return;
    }

    const month = new Intl.DateTimeFormat("zh-CN", { month: "short" }).format(
      parseDateKey(firstInRange.date),
    );

    if (month !== lastMonth) {
      anchors.push({ month, column });
      lastMonth = month;
    }
  });

  return anchors;
}

export function calculateDashboardStats(
  recordsByDate: Record<string, DailyRecord>,
  supplementsConfig: SupplementName[],
  baseDate = new Date(),
): DashboardStats {
  const today = startOfDay(baseDate);
  const dates7 = getRecentDates(today, 7);
  const dates30 = getRecentDates(today, 30);

  return {
    weeklyTrainingCount: countTrainingInRange(recordsByDate, startOfWeek(today), today),
    monthlyTrainingCount: countTrainingInRange(recordsByDate, startOfMonth(today), today),
    currentStreak: calculateCurrentStreak(recordsByDate, supplementsConfig, today),
    completionRate7d: calculateCompletionRate(recordsByDate, supplementsConfig, dates7),
    completionRate30d: calculateCompletionRate(recordsByDate, supplementsConfig, dates30),
  };
}

export function formatDateWithWeekday(dateKey: string): string {
  return new Intl.DateTimeFormat("zh-CN", {
    month: "long",
    day: "numeric",
    weekday: "short",
  }).format(parseDateKey(dateKey));
}

export function formatFullDate(date: Date): string {
  return new Intl.DateTimeFormat("zh-CN", {
    year: "numeric",
    month: "long",
    day: "numeric",
    weekday: "long",
  }).format(date);
}

export function formatSupplementCount(
  record: DailyRecord | undefined,
  supplementsConfig: SupplementName[],
): string {
  const count = countCheckedSupplements(record, supplementsConfig);
  return `已吃 ${count}/${supplementsConfig.length || 0} 项补剂`;
}

function startOfDay(date: Date): Date {
  return new Date(date.getFullYear(), date.getMonth(), date.getDate());
}

function startOfWeek(date: Date): Date {
  const normalized = startOfDay(date);
  const day = normalized.getDay();
  const diff = day === 0 ? -6 : 1 - day;
  normalized.setDate(normalized.getDate() + diff);

  return normalized;
}

function startOfMonth(date: Date): Date {
  return new Date(date.getFullYear(), date.getMonth(), 1);
}

export function parseDateKey(value: string): Date {
  const [year, month, day] = value.split("-").map(Number);
  return new Date(year, month - 1, day);
}

function getRecentDates(end: Date, days: number): string[] {
  return Array.from({ length: days }, (_, index) => {
    const current = new Date(end.getTime() - (days - index - 1) * DAY_MS);
    return getDateKey(current);
  });
}

function hasTrainingRecord(record?: DailyRecord): boolean {
  if (!record?.trainingCompleted) {
    return false;
  }

  return record.trainingTypes.some((type) => type !== "休息");
}

function countTrainingInRange(
  recordsByDate: Record<string, DailyRecord>,
  start: Date,
  end: Date,
): number {
  let total = 0;

  for (let time = start.getTime(); time <= end.getTime(); time += DAY_MS) {
    if (hasTrainingRecord(recordsByDate[getDateKey(new Date(time))])) {
      total += 1;
    }
  }

  return total;
}

function calculateCurrentStreak(
  recordsByDate: Record<string, DailyRecord>,
  supplementsConfig: SupplementName[],
  today: Date,
): number {
  let streak = 0;

  for (let time = today.getTime(); ; time -= DAY_MS) {
    const score = calculateDailyScore(
      recordsByDate[getDateKey(new Date(time))],
      supplementsConfig,
    );

    if (score === 0) {
      break;
    }

    streak += 1;
  }

  return streak;
}

function calculateCompletionRate(
  recordsByDate: Record<string, DailyRecord>,
  supplementsConfig: SupplementName[],
  dates: string[],
): number {
  const completed = dates.filter(
    (date) => calculateDailyScore(recordsByDate[date], supplementsConfig) > 0,
  ).length;
  return Math.round((completed / dates.length) * 100);
}
