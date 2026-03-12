export type TrainingType = string;

export type SupplementName = string;

export type DailyRecord = {
  date: string;
  trainingCompleted: boolean;
  trainingTypes: TrainingType[];
  note: string;
  supplements: Record<SupplementName, boolean>;
  updatedAt: string;
};

export type DashboardData = {
  version: number;
  trainingTypesConfig: TrainingType[];
  supplementsConfig: SupplementName[];
  recordsByDate: Record<string, DailyRecord>;
};

export type DashboardStats = {
  weeklyTrainingCount: number;
  monthlyTrainingCount: number;
  currentStreak: number;
  completionRate7d: number;
  completionRate30d: number;
};

export type HeatmapCellData = {
  date: string;
  score: 0 | 1 | 2 | 3;
  isInRange: boolean;
  record?: DailyRecord;
};
