import api from './api';

export interface Goal {
  id: number;
  title: string;
  description?: string;
  priority?: string;
  status?: string;
  startdate?: string;
  deadline?: string;
  daily_time_minutes?: number;
  progress?: number;
  stages?: Stage[];
  results?: Result[];
  createdAt?: string;
}

export interface Stage {
  id: number;
  title: string;
  description?: string;
  status?: string;
  priority?: string;
  progress?: number;
  estimatedMinutes?: number;
  deadline?: string;
  startsAt?: string;
  sortOrder?: number;
  isCompleted?: boolean;
  createdAt?: string;
}

export interface Result {
  id: number;
  description?: string;
  images?: string[];
  createdAt?: string;
}

export interface GoalsPage {
  content: Goal[];
  totalPages: number;
  totalElements: number;
  number: number;
}

export interface StageInput {
  title: string;
  description?: string;
  priority?: string;
  estimatedMinutes?: number;
  deadline?: string;
  startsAt?: string;
  sortOrder?: number;
  status?: string;
}

export interface CreateGoalInput {
  title: string;
  description?: string;
  priority: string;
  start_date: string;
  deadline?: string;
  daily_time_minutes?: number;
  stages?: StageInput[];
}

export interface AiPlan {
  title: string;
  description?: string;
  priority: string;
  start_date: string;
  deadline?: string;
  daily_time_minutes?: number;
  stages: StageInput[];
}

export interface UpdateGoalInput {
  title?: string;
  description?: string;
  priority?: string;
  startdate?: string;
  deadline?: string;
  daily_time_minutes?: number;
  stages?: StageInput[];
}

export interface UpdateStageInput {
  status?: string;
  progress?: number;
  title?: string;
  description?: string;
  priority?: string;
  estimatedMinutes?: number;
  deadline?: string;
  startsAt?: string;
  sortOrder?: number;
}

// Извлекает сообщение из ApiResponse error (формат: { code, message })
export function extractApiError(err: unknown): string {
  const apiErr = (err as { response?: { data?: { error?: { message?: string } | string } } })
    ?.response?.data?.error;
  if (apiErr && typeof apiErr === 'object' && 'message' in apiErr) {
    return (apiErr as { message: string }).message;
  }
  if (typeof apiErr === 'string') return apiErr;
  return err instanceof Error ? err.message : 'Произошла ошибка';
}

const BASE = '/api/v1/goals';

export const goalsService = {
  async getGoals(page = 0, size = 10): Promise<GoalsPage> {
    const res = await api.get(`${BASE}?page=${page}&size=${size}`);
    return res.data.data;
  },

  async getGoal(id: number): Promise<Goal> {
    const res = await api.get(`${BASE}/${id}`);
    return res.data.data;
  },

  async createGoal(data: CreateGoalInput): Promise<Goal> {
    const res = await api.post(BASE, data);
    return res.data.data;
  },

  async updateGoal(id: number, data: UpdateGoalInput): Promise<Goal> {
    const res = await api.put(`${BASE}/${id}`, { goalId: id, ...data });
    return res.data.data;
  },

  async deleteGoal(id: number): Promise<void> {
    await api.delete(`${BASE}/${id}`);
  },

  async addStage(goalId: number, data: StageInput): Promise<Stage> {
    const res = await api.post(`${BASE}/${goalId}/tasks`, data);
    return res.data.data;
  },

  async updateStage(goalId: number, stageId: number, data: UpdateStageInput): Promise<Stage> {
    const res = await api.patch(`${BASE}/${goalId}/tasks/${stageId}`, data);
    return res.data.data;
  },

  async deleteStage(goalId: number, stageId: number): Promise<void> {
    await api.delete(`${BASE}/${goalId}/tasks/${stageId}`);
  },

  async getResults(goalId: number): Promise<Result[]> {
    const res = await api.get(`${BASE}/${goalId}/results`);
    return res.data.data;
  },

  async createResult(goalId: number, data: { description: string }): Promise<Result> {
    const res = await api.post(`${BASE}/${goalId}/results`, data);
    return res.data.data;
  },

  async updateResult(goalId: number, resultId: number, data: { description: string }): Promise<Result> {
    const res = await api.patch(`${BASE}/${goalId}/results/${resultId}`, data);
    return res.data.data;
  },

  async deleteResult(goalId: number, resultId: number): Promise<void> {
    await api.delete(`${BASE}/${goalId}/results/${resultId}`);
  },

  async aiDecompose(prompt: string): Promise<Record<string, unknown> | string> {
    const res = await api.post(`${BASE}/ai-decompose`, { prompt });
    return res.data.data;
  },

  async aiHelp(goalId: number, prompt: string): Promise<Record<string, unknown> | string> {
    const res = await api.post(`${BASE}/ai-help`, { promt: prompt, goalId });
    return res.data.data;
  },
};
