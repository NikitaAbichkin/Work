import { useState, useEffect, useCallback, type FormEvent } from 'react';
import { useParams, Link } from 'react-router-dom';
import {
  goalsService,
  extractApiError,
  type AiPlan,
  type Goal,
  type Stage,
  type Result,
  type UpdateGoalInput,
  type UpdateStageInput,
} from '../services/goals.service';
import '../styles/goals.css';

function statusBadge(status?: string) {
  switch (status?.toUpperCase()) {
    case 'IN_PROGRESS': return 'badge badge-progress';
    case 'COMPLETED':   return 'badge badge-done';
    case 'FROZEN':      return 'badge badge-frozen';
    case 'ARCHIVED':    return 'badge badge-frozen';
    default:            return 'badge badge-todo';
  }
}

function statusLabel(status?: string) {
  switch (status?.toUpperCase()) {
    case 'IN_PROGRESS': return 'В процессе';
    case 'COMPLETED':   return 'Завершено';
    case 'FROZEN':      return 'Заморожено';
    case 'ARCHIVED':    return 'Архив';
    default:            return 'Не начато';
  }
}

function parseAiPlan(raw: Record<string, unknown> | string | null): AiPlan | null {
  if (!raw) return null;

  const parsed = typeof raw === 'string'
    ? JSON.parse(raw) as Record<string, unknown>
    : raw;

  if (
    typeof parsed.title !== 'string'
    || typeof parsed.priority !== 'string'
    || typeof parsed.start_date !== 'string'
    || !Array.isArray(parsed.stages)
  ) {
    return null;
  }

  return {
    title: parsed.title,
    description: typeof parsed.description === 'string' ? parsed.description : undefined,
    priority: parsed.priority,
    start_date: parsed.start_date,
    deadline: typeof parsed.deadline === 'string' ? parsed.deadline : undefined,
    daily_time_minutes: typeof parsed.daily_time_minutes === 'number' ? parsed.daily_time_minutes : undefined,
    stages: parsed.stages.map((stage, index) => {
      const item = (stage ?? {}) as Record<string, unknown>;
      return {
        title: typeof item.title === 'string' ? item.title : `Задача ${index + 1}`,
        description: typeof item.description === 'string' ? item.description : undefined,
        priority: typeof item.priority === 'string' ? item.priority : 'MEDIUM',
        estimatedMinutes: typeof item.estimatedMinutes === 'number' ? item.estimatedMinutes : undefined,
        deadline: typeof item.deadline === 'string' ? item.deadline : undefined,
        startsAt: typeof item.startsAt === 'string' ? item.startsAt : undefined,
        sortOrder: typeof item.sortOrder === 'number' ? item.sortOrder : index,
      };
    }),
  };
}

export default function GoalDetailPage() {
  const { id } = useParams<{ id: string }>();
  const goalId = Number(id);

  const [goal, setGoal] = useState<Goal | null>(null);
  const [results, setResults] = useState<Result[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Add task form
  const [showTaskForm, setShowTaskForm] = useState(false);
  const [taskTitle, setTaskTitle] = useState('');
  const [taskDesc, setTaskDesc] = useState('');
  const [taskPriority, setTaskPriority] = useState('MEDIUM');
  const [taskMinutes, setTaskMinutes] = useState('');
  const [taskDeadline, setTaskDeadline] = useState('');
  const [creatingTask, setCreatingTask] = useState(false);
  const [editingGoal, setEditingGoal] = useState(false);
  const [savingGoal, setSavingGoal] = useState(false);
  const [goalTitle, setGoalTitle] = useState('');
  const [goalDescription, setGoalDescription] = useState('');
  const [goalPriority, setGoalPriority] = useState('MEDIUM');
  const [goalStartDate, setGoalStartDate] = useState('');
  const [goalDeadline, setGoalDeadline] = useState('');
  const [goalDailyMinutes, setGoalDailyMinutes] = useState('');
  const [editingStageId, setEditingStageId] = useState<number | null>(null);
  const [savingStage, setSavingStage] = useState(false);
  const [stageEditTitle, setStageEditTitle] = useState('');
  const [stageEditDescription, setStageEditDescription] = useState('');
  const [stageEditPriority, setStageEditPriority] = useState('MEDIUM');
  const [stageEditMinutes, setStageEditMinutes] = useState('');
  const [stageEditStartsAt, setStageEditStartsAt] = useState('');
  const [stageEditDeadline, setStageEditDeadline] = useState('');
  const [stageEditSortOrder, setStageEditSortOrder] = useState('');
  const [changingStageId, setChangingStageId] = useState<number | null>(null);

  // Result form
  const [showResultForm, setShowResultForm] = useState(false);
  const [resultDesc, setResultDesc] = useState('');
  const [creatingResult, setCreatingResult] = useState(false);
  const [editingResultId, setEditingResultId] = useState<number | null>(null);
  const [editingResultDesc, setEditingResultDesc] = useState('');
  const [savingResult, setSavingResult] = useState(false);

  const [showAiHelp, setShowAiHelp] = useState(false);
  const [aiHelpPrompt, setAiHelpPrompt] = useState('');
  const [aiHelpResponse, setAiHelpResponse] = useState<Record<string, unknown> | string | null>(null);
  const [aiHelpLoading, setAiHelpLoading] = useState(false);
  const [applyingAiHelp, setApplyingAiHelp] = useState(false);

  const flash = (msg: string, type: 'success' | 'error' = 'success') => {
    if (type === 'success') { setSuccess(msg); setTimeout(() => setSuccess(''), 3000); }
    else setError(msg);
  };

  const fetchGoal = useCallback(async () => {
    try {
      const g = await goalsService.getGoal(goalId);
      setGoal(g);
      setGoalTitle(g.title ?? '');
      setGoalDescription(g.description ?? '');
      setGoalPriority(g.priority ?? 'MEDIUM');
      setGoalStartDate(g.startdate ?? '');
      setGoalDeadline(g.deadline ?? '');
      setGoalDailyMinutes(g.daily_time_minutes != null ? String(g.daily_time_minutes) : '');
    } catch (err) {
      flash(extractApiError(err), 'error');
    }
  }, [goalId]);

  const fetchResults = useCallback(async () => {
    try {
      const r = await goalsService.getResults(goalId);
      setResults(Array.isArray(r) ? r : []);
    } catch {
      // non-critical
    }
  }, [goalId]);

  useEffect(() => {
    if (!goalId || isNaN(goalId)) return;
    setLoading(true);
    Promise.all([fetchGoal(), fetchResults()]).finally(() => setLoading(false));
  }, [goalId, fetchGoal, fetchResults]);

  // ===== Add task =====
  const handleAddTask = async (e: FormEvent) => {
    e.preventDefault();
    if (!taskTitle.trim()) return;
    setCreatingTask(true);
    setError('');
    try {
      await goalsService.addStage(goalId, {
        title: taskTitle.trim(),
        description: taskDesc.trim() || undefined,
        priority: taskPriority,
        estimatedMinutes: taskMinutes ? Number(taskMinutes) : undefined,
        deadline: taskDeadline || undefined,
      });
      setTaskTitle(''); setTaskDesc(''); setTaskPriority('MEDIUM');
      setTaskMinutes(''); setTaskDeadline('');
      setShowTaskForm(false);
      flash('Задача добавлена');
      fetchGoal();
    } catch (err) {
      flash(extractApiError(err), 'error');
    } finally {
      setCreatingTask(false);
    }
  };

  // ===== Update stage status =====
  const handleStatusChange = async (stage: Stage, newStatus: string) => {
    if (stage.status?.toUpperCase() === newStatus.toUpperCase()) return;
    setError('');
    setChangingStageId(stage.id);
    try {
      await goalsService.updateStage(goalId, stage.id, { status: newStatus });
      flash('Статус обновлён');
      fetchGoal();
    } catch (err) {
      flash(extractApiError(err), 'error');
    } finally {
      setChangingStageId(null);
    }
  };

  const startEditStage = (stage: Stage) => {
    setEditingStageId(stage.id);
    setStageEditTitle(stage.title);
    setStageEditDescription(stage.description ?? '');
    setStageEditPriority(stage.priority ?? 'MEDIUM');
    setStageEditMinutes(stage.estimatedMinutes != null ? String(stage.estimatedMinutes) : '');
    setStageEditStartsAt(stage.startsAt ?? '');
    setStageEditDeadline(stage.deadline ?? '');
    setStageEditSortOrder(stage.sortOrder != null ? String(stage.sortOrder) : '');
  };

  const resetEditStage = () => {
    setEditingStageId(null);
    setStageEditTitle('');
    setStageEditDescription('');
    setStageEditPriority('MEDIUM');
    setStageEditMinutes('');
    setStageEditStartsAt('');
    setStageEditDeadline('');
    setStageEditSortOrder('');
  };

  // ===== Delete stage =====
  const handleDeleteStage = async (stageId: number) => {
    if (!confirm('Удалить задачу?')) return;
    setError('');
    try {
      await goalsService.deleteStage(goalId, stageId);
      flash('Задача удалена');
      fetchGoal();
    } catch (err) {
      flash(extractApiError(err), 'error');
    }
  };

  // ===== Add result =====
  const handleAddResult = async (e: FormEvent) => {
    e.preventDefault();
    if (!resultDesc.trim()) { setError('Описание результата обязательно'); return; }
    setCreatingResult(true);
    setError('');
    try {
      await goalsService.createResult(goalId, { description: resultDesc.trim() });
      setResultDesc('');
      setShowResultForm(false);
      flash('Результат добавлен');
      fetchResults();
    } catch (err) {
      flash(extractApiError(err), 'error');
    } finally {
      setCreatingResult(false);
    }
  };

  // ===== Delete result =====
  const handleDeleteResult = async (resultId: number) => {
    if (!confirm('Удалить результат?')) return;
    try {
      await goalsService.deleteResult(goalId, resultId);
      flash('Результат удалён');
      fetchResults();
    } catch (err) {
      flash(extractApiError(err), 'error');
    }
  };

  const startEditResult = (result: Result) => {
    setEditingResultId(result.id);
    setEditingResultDesc(result.description ?? '');
  };

  const cancelEditResult = () => {
    setEditingResultId(null);
    setEditingResultDesc('');
  };

  const handleUpdateResult = async (e: FormEvent, resultId: number) => {
    e.preventDefault();
    if (!editingResultDesc.trim()) {
      setError('Описание результата обязательно');
      return;
    }

    setSavingResult(true);
    setError('');
    try {
      await goalsService.updateResult(goalId, resultId, { description: editingResultDesc.trim() });
      flash('Результат обновлён');
      cancelEditResult();
      fetchResults();
    } catch (err) {
      flash(extractApiError(err), 'error');
    } finally {
      setSavingResult(false);
    }
  };

  const handleUpdateGoal = async (e: FormEvent) => {
    e.preventDefault();
    setSavingGoal(true);
    setError('');
    try {
      const payload: UpdateGoalInput = {
        title: goalTitle.trim(),
        description: goalDescription.trim() || undefined,
        priority: goalPriority,
        startdate: goalStartDate || undefined,
        deadline: goalDeadline || undefined,
        daily_time_minutes: goalDailyMinutes ? Number(goalDailyMinutes) : undefined,
      };
      await goalsService.updateGoal(goalId, payload);
      setEditingGoal(false);
      flash('Цель обновлена');
      fetchGoal();
    } catch (err) {
      flash(extractApiError(err), 'error');
    } finally {
      setSavingGoal(false);
    }
  };

  const handleUpdateStage = async (e: FormEvent, stageId: number) => {
    e.preventDefault();
    setSavingStage(true);
    setError('');
    try {
      const payload: UpdateStageInput = {
        title: stageEditTitle.trim(),
        description: stageEditDescription.trim() || undefined,
        priority: stageEditPriority,
        estimatedMinutes: stageEditMinutes ? Number(stageEditMinutes) : undefined,
        startsAt: stageEditStartsAt || undefined,
        deadline: stageEditDeadline || undefined,
        sortOrder: stageEditSortOrder ? Number(stageEditSortOrder) : undefined,
      };
      await goalsService.updateStage(goalId, stageId, payload);
      resetEditStage();
      flash('Задача обновлена');
      fetchGoal();
    } catch (err) {
      flash(extractApiError(err), 'error');
    } finally {
      setSavingStage(false);
    }
  };

  const handleAiHelp = async (e: FormEvent) => {
    e.preventDefault();
    if (!aiHelpPrompt.trim()) return;
    setAiHelpLoading(true);
    setAiHelpResponse(null);
    setError('');
    try {
      const resp = await goalsService.aiHelp(goalId, aiHelpPrompt.trim());
      setAiHelpResponse(resp);
    } catch (err) {
      flash(extractApiError(err), 'error');
    } finally {
      setAiHelpLoading(false);
    }
  };

  const handleApplyAiHelp = async () => {
    const plan = parseAiPlan(aiHelpResponse);
    if (!plan) {
      flash('AI вернул ответ, который нельзя применить к текущей цели', 'error');
      return;
    }

    setApplyingAiHelp(true);
    setError('');
    try {
      await goalsService.updateGoal(goalId, {
        title: plan.title,
        description: plan.description,
        priority: plan.priority,
        startdate: plan.start_date,
        deadline: plan.deadline,
        daily_time_minutes: plan.daily_time_minutes,
        stages: plan.stages.map((stage, index) => ({
          ...stage,
          sortOrder: stage.sortOrder ?? index,
        })),
      });
      setShowAiHelp(false);
      setAiHelpResponse(null);
      flash('AI-изменения применены к цели');
      fetchGoal();
    } catch (err) {
      flash(extractApiError(err), 'error');
    } finally {
      setApplyingAiHelp(false);
    }
  };

  if (loading) {
    return (
      <div className="goal-detail">
        <div className="loading">Загрузка цели...</div>
      </div>
    );
  }

  const stages = (goal?.stages ?? [])
    .slice()
    .sort((a, b) => {
      const aOrder = a.sortOrder ?? Number.MAX_SAFE_INTEGER;
      const bOrder = b.sortOrder ?? Number.MAX_SAFE_INTEGER;
      if (aOrder !== bOrder) return aOrder - bOrder;
      return a.id - b.id;
    });
  const aiHelpPlan = (() => {
    try {
      return parseAiPlan(aiHelpResponse);
    } catch {
      return null;
    }
  })();

  return (
    <div className="goal-detail">
      <Link to="/goals" className="back-link">← Назад к целям</Link>

      {error && <div className="alert alert-error">{error}</div>}
      {success && <div className="alert alert-success">{success}</div>}

      {!goal ? (
        <div className="empty-state">Цель не найдена</div>
      ) : (
        <>
          {/* HEADER */}
          <div style={{ marginBottom: '24px' }}>
            <div className="section-header">
              <h1>{goal.title}</h1>
              <button className="btn btn-secondary" onClick={() => setEditingGoal((value) => !value)}>
                {editingGoal ? 'Скрыть форму' : 'Редактировать цель'}
              </button>
            </div>
            <div style={{ display: 'flex', gap: '12px', alignItems: 'center', marginTop: '8px', flexWrap: 'wrap' }}>
              {goal.status && <span className={statusBadge(goal.status)}>{statusLabel(goal.status)}</span>}
              {goal.priority && <span className="badge badge-todo">{goal.priority}</span>}
              {goal.progress != null && (
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                  <div style={{ width: '120px', height: '6px', background: 'var(--color-border)', borderRadius: '3px' }}>
                    <div style={{ width: `${goal.progress}%`, height: '100%', background: 'var(--color-primary)', borderRadius: '3px' }} />
                  </div>
                  <span style={{ fontSize: '0.8rem', color: 'var(--color-text-secondary)' }}>{goal.progress}%</span>
                </div>
              )}
            </div>
            {goal.description && <p className="description">{goal.description}</p>}
            <div style={{ display: 'flex', gap: '16px', fontSize: '0.8rem', color: 'var(--color-text-secondary)', marginTop: '8px', flexWrap: 'wrap' }}>
              {goal.startdate && <span>Начало: {goal.startdate}</span>}
              {goal.deadline && <span>Дедлайн: {goal.deadline}</span>}
              {goal.daily_time_minutes && <span>{goal.daily_time_minutes} мин/день</span>}
            </div>
            {editingGoal && (
              <div className="create-form" style={{ marginTop: '16px' }}>
                <form className="form-inline" onSubmit={handleUpdateGoal}>
                  <input value={goalTitle} onChange={(e) => setGoalTitle(e.target.value)} placeholder="Название цели" required />
                  <textarea value={goalDescription} onChange={(e) => setGoalDescription(e.target.value)} placeholder="Описание" />
                  <div className="form-row">
                    <select value={goalPriority} onChange={(e) => setGoalPriority(e.target.value)}>
                      <option value="LOW">LOW</option>
                      <option value="MEDIUM">MEDIUM</option>
                      <option value="HIGH">HIGH</option>
                    </select>
                    <input type="number" min="1" value={goalDailyMinutes} onChange={(e) => setGoalDailyMinutes(e.target.value)} placeholder="Минут в день" />
                  </div>
                  <div className="form-row">
                    <input type="date" value={goalStartDate} onChange={(e) => setGoalStartDate(e.target.value)} />
                    <input type="date" value={goalDeadline} onChange={(e) => setGoalDeadline(e.target.value)} />
                  </div>
                  <button className="btn btn-primary" type="submit" disabled={savingGoal}>
                    {savingGoal ? 'Сохраняем...' : 'Сохранить цель'}
                  </button>
                </form>
              </div>
            )}
          </div>

          {/* ===== TASKS ===== */}
          <div className="section">
            <div className="section-header">
              <h2>Задачи ({stages.length})</h2>
              <div className="goal-card-actions">
                <button className="btn btn-secondary" onClick={() => { setShowAiHelp(!showAiHelp); setAiHelpResponse(null); }}>
                  {showAiHelp ? '✕ Скрыть AI' : '✦ AI-улучшение'}
                </button>
                <button className="btn btn-primary" onClick={() => setShowTaskForm(!showTaskForm)}>
                  {showTaskForm ? '✕ Отмена' : '+ Добавить задачу'}
                </button>
              </div>
            </div>

            {showAiHelp && (
              <div className="create-form">
                <form className="form-inline" onSubmit={handleAiHelp}>
                  <textarea
                    placeholder={`Опиши, что нужно изменить в текущей цели:\n• усилить план\n• сократить сроки\n• добавить больше задач\n• перестроить порядок этапов`}
                    value={aiHelpPrompt}
                    onChange={(e) => setAiHelpPrompt(e.target.value)}
                    style={{ minHeight: '100px' }}
                  />
                  <button className="btn btn-primary" type="submit" disabled={aiHelpLoading || !aiHelpPrompt.trim()}>
                    {aiHelpLoading ? '⏳ AI думает...' : '✦ Изменить текущую цель'}
                  </button>
                </form>

                {aiHelpResponse && (
                  <div className="ai-response">
                    <h4>Обновлённый план от AI</h4>
                    {aiHelpPlan ? (
                      <div className="ai-preview">
                        <div className="ai-preview-header">
                          <div>
                            <h3>{aiHelpPlan.title}</h3>
                            {aiHelpPlan.description && <p>{aiHelpPlan.description}</p>}
                          </div>
                          <div className="ai-preview-badges">
                            <span className="badge badge-todo">{aiHelpPlan.priority}</span>
                            {aiHelpPlan.daily_time_minutes != null && (
                              <span className="badge badge-progress">{aiHelpPlan.daily_time_minutes} мин/день</span>
                            )}
                          </div>
                        </div>

                        <div className="ai-preview-meta">
                          <span>Старт: {aiHelpPlan.start_date}</span>
                          {aiHelpPlan.deadline && <span>Дедлайн: {aiHelpPlan.deadline}</span>}
                          <span>Задач: {aiHelpPlan.stages.length}</span>
                        </div>

                        <div className="ai-preview-list">
                          {aiHelpPlan.stages.map((stage, index) => (
                            <div key={`${stage.title}-${index}`} className="ai-stage-card">
                              <div className="ai-stage-head">
                                <strong>{index + 1}. {stage.title}</strong>
                                <span className="badge badge-todo">{stage.priority ?? 'MEDIUM'}</span>
                              </div>
                              {stage.description && <p>{stage.description}</p>}
                              <div className="ai-preview-meta">
                                {stage.estimatedMinutes != null && <span>{stage.estimatedMinutes} мин</span>}
                                {stage.startsAt && <span>Старт: {stage.startsAt}</span>}
                                {stage.deadline && <span>До: {stage.deadline}</span>}
                              </div>
                            </div>
                          ))}
                        </div>

                        <div className="ai-preview-actions">
                          <button className="btn btn-primary" type="button" onClick={handleApplyAiHelp} disabled={applyingAiHelp}>
                            {applyingAiHelp ? 'Применяем...' : 'Применить к текущей цели'}
                          </button>
                          <button className="btn btn-secondary" type="button" onClick={() => setAiHelpResponse(null)} disabled={applyingAiHelp}>
                            Очистить ответ
                          </button>
                        </div>
                      </div>
                    ) : (
                      <pre>
                        {typeof aiHelpResponse === 'string'
                          ? aiHelpResponse
                          : JSON.stringify(aiHelpResponse, null, 2)}
                      </pre>
                    )}
                  </div>
                )}
              </div>
            )}

            {showTaskForm && (
              <div className="create-form">
                <form className="form-inline" onSubmit={handleAddTask}>
                  <input
                    type="text"
                    placeholder="Название задачи *"
                    value={taskTitle}
                    onChange={(e) => setTaskTitle(e.target.value)}
                    required
                  />
                  <textarea
                    placeholder="Описание задачи (необязательно)"
                    value={taskDesc}
                    onChange={(e) => setTaskDesc(e.target.value)}
                    style={{ minHeight: '60px' }}
                  />
                  <div className="form-row">
                    <select value={taskPriority} onChange={(e) => setTaskPriority(e.target.value)}>
                      <option value="LOW">Приоритет: Низкий</option>
                      <option value="MEDIUM">Приоритет: Средний</option>
                      <option value="HIGH">Приоритет: Высокий</option>
                    </select>
                    <input
                      type="number"
                      placeholder="Минут (план)"
                      value={taskMinutes}
                      onChange={(e) => setTaskMinutes(e.target.value)}
                      min="1"
                    />
                  </div>
                  <div>
                    <label style={{ fontSize: '0.8rem', color: 'var(--color-text-secondary)', display: 'block', marginBottom: '4px' }}>
                      Дедлайн задачи
                    </label>
                    <input
                      type="date"
                      value={taskDeadline}
                      onChange={(e) => setTaskDeadline(e.target.value)}
                    />
                  </div>
                  <button className="btn btn-primary" type="submit" disabled={creatingTask || !taskTitle.trim()}>
                    {creatingTask ? 'Добавляется...' : 'Добавить задачу'}
                  </button>
                </form>
              </div>
            )}

            {stages.length === 0 ? (
              <div className="empty-state" style={{ padding: '24px' }}>
                Задач пока нет. Добавьте первую задачу выше.
              </div>
            ) : (
              stages.map((stage) => (
                <div
                  key={stage.id}
                  className={`stage-card ${editingStageId === stage.id ? '' : 'stage-card-clickable'}`}
                  onClick={editingStageId === stage.id ? undefined : () => startEditStage(stage)}
                >
                  {editingStageId === stage.id ? (
                    <form className="form-inline" onSubmit={(e) => handleUpdateStage(e, stage.id)}>
                      <div className="stage-card-header">
                        <p className="stage-title">Редактирование задачи</p>
                        <span className={statusBadge(stage.status)}>{statusLabel(stage.status)}</span>
                      </div>
                      <input value={stageEditTitle} onChange={(e) => setStageEditTitle(e.target.value)} placeholder="Название задачи" required />
                      <textarea value={stageEditDescription} onChange={(e) => setStageEditDescription(e.target.value)} placeholder="Описание задачи" />
                      <div className="form-row">
                        <select value={stageEditPriority} onChange={(e) => setStageEditPriority(e.target.value)}>
                          <option value="LOW">LOW</option>
                          <option value="MEDIUM">MEDIUM</option>
                          <option value="HIGH">HIGH</option>
                        </select>
                        <input type="number" min="0" value={stageEditMinutes} onChange={(e) => setStageEditMinutes(e.target.value)} placeholder="Минуты" />
                      </div>
                      <div className="form-row">
                        <input type="date" value={stageEditStartsAt} onChange={(e) => setStageEditStartsAt(e.target.value)} />
                        <input type="date" value={stageEditDeadline} onChange={(e) => setStageEditDeadline(e.target.value)} />
                      </div>
                      <input type="number" min="0" value={stageEditSortOrder} onChange={(e) => setStageEditSortOrder(e.target.value)} placeholder="Порядок" />
                      <div className="stage-actions">
                        <button className="btn btn-primary" type="submit" disabled={savingStage}>
                          {savingStage ? 'Сохраняем...' : 'Сохранить задачу'}
                        </button>
                        <button className="btn btn-secondary" type="button" onClick={resetEditStage} disabled={savingStage}>
                          Отмена
                        </button>
                      </div>
                    </form>
                  ) : (
                    <>
                      <div className="stage-card-header">
                        <p className="stage-title">{stage.title}</p>
                        <span className={statusBadge(stage.status)}>{statusLabel(stage.status)}</span>
                      </div>

                      {stage.description && (
                        <p style={{ fontSize: '0.875rem', color: 'var(--color-text-secondary)', margin: '4px 0' }}>
                          {stage.description}
                        </p>
                      )}

                      <div className="stage-meta">
                        {stage.priority && <span>Приоритет: {stage.priority}</span>}
                        {stage.estimatedMinutes && <span> · ~{stage.estimatedMinutes} мин</span>}
                        {stage.startsAt && <span> · старт {stage.startsAt}</span>}
                        {stage.deadline && <span> · до {stage.deadline}</span>}
                        {stage.sortOrder != null && <span> · порядок {stage.sortOrder}</span>}
                        {stage.progress != null && <span> · Прогресс: {stage.progress}%</span>}
                      </div>

                      <div className="stage-actions">
                        <button
                          className={`btn ${stage.status === 'IN_PROGRESS' ? 'btn-primary' : 'btn-secondary'}`}
                          style={{ fontSize: '0.8rem' }}
                          type="button"
                          disabled={changingStageId === stage.id}
                          onClick={(e) => { e.stopPropagation(); handleStatusChange(stage, 'IN_PROGRESS'); }}
                        >
                          ▶ В процессе
                        </button>
                        <button
                          className={`btn ${stage.status === 'COMPLETED' ? 'btn-success' : 'btn-secondary'}`}
                          style={{ fontSize: '0.8rem' }}
                          type="button"
                          disabled={changingStageId === stage.id}
                          onClick={(e) => { e.stopPropagation(); handleStatusChange(stage, 'COMPLETED'); }}
                        >
                          ✓ Завершить
                        </button>
                        <button
                          className={`btn ${stage.status === 'FROZEN' ? 'btn-frozen-active' : 'btn-secondary'}`}
                          style={{ fontSize: '0.8rem' }}
                          type="button"
                          disabled={changingStageId === stage.id}
                          onClick={(e) => { e.stopPropagation(); handleStatusChange(stage, 'FROZEN'); }}
                        >
                          ❄ Заморозить
                        </button>
                        <button className="btn btn-danger" style={{ fontSize: '0.8rem' }}
                          type="button"
                          onClick={(e) => { e.stopPropagation(); handleDeleteStage(stage.id); }}>
                          Удалить
                        </button>
                      </div>
                    </>
                  )}
                </div>
              ))
            )}
          </div>

          {/* ===== RESULTS ===== */}
          <div className="section">
            <div className="section-header">
              <h2>Результаты ({results.length})</h2>
              <button className="btn btn-primary" onClick={() => setShowResultForm(!showResultForm)}>
                {showResultForm ? '✕ Отмена' : '+ Добавить результат'}
              </button>
            </div>

            {showResultForm && (
              <div className="create-form">
                <form className="form-inline" onSubmit={handleAddResult}>
                  <textarea
                    placeholder="Описание результата *"
                    value={resultDesc}
                    onChange={(e) => setResultDesc(e.target.value)}
                    required
                    style={{ minHeight: '80px' }}
                  />
                  <button className="btn btn-primary" type="submit" disabled={creatingResult || !resultDesc.trim()}>
                    {creatingResult ? 'Сохраняется...' : 'Сохранить результат'}
                  </button>
                </form>
              </div>
            )}

            {results.length === 0 ? (
              <div className="empty-state" style={{ padding: '24px' }}>Результатов пока нет</div>
            ) : (
              results.map((r) => (
                <div key={r.id} className="result-card">
                  <div className="result-card-header">
                    {editingResultId === r.id ? (
                      <form className="form-inline" style={{ width: '100%', margin: 0 }} onSubmit={(e) => handleUpdateResult(e, r.id)}>
                        <textarea
                          value={editingResultDesc}
                          onChange={(e) => setEditingResultDesc(e.target.value)}
                          placeholder="Описание результата"
                          required
                          style={{ minHeight: '80px' }}
                        />
                        <div className="stage-actions" style={{ marginTop: 0 }}>
                          <button className="btn btn-primary" type="submit" disabled={savingResult || !editingResultDesc.trim()}>
                            {savingResult ? 'Сохраняется...' : 'Сохранить'}
                          </button>
                          <button className="btn btn-secondary" type="button" onClick={cancelEditResult} disabled={savingResult}>
                            Отмена
                          </button>
                        </div>
                      </form>
                    ) : (
                      <>
                        <p style={{ margin: 0, flex: 1 }}>{r.description}</p>
                        <button className="btn btn-secondary" style={{ fontSize: '0.8rem', marginLeft: '8px' }}
                          onClick={() => startEditResult(r)}>
                          Изменить
                        </button>
                        <button className="btn btn-danger" style={{ fontSize: '0.8rem', marginLeft: '8px' }}
                          onClick={() => handleDeleteResult(r.id)}>
                          Удалить
                        </button>
                      </>
                    )}
                  </div>
                  {r.createdAt && (
                    <p style={{ margin: '4px 0 0', fontSize: '0.75rem', color: 'var(--color-text-secondary)' }}>
                      {new Date(r.createdAt).toLocaleString('ru')}
                    </p>
                  )}
                </div>
              ))
            )}
          </div>

        </>
      )}
    </div>
  );
}
