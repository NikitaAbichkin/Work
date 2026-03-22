import { useState, useEffect, useCallback, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { goalsService, extractApiError, type AiPlan, type Goal } from '../services/goals.service';
import '../styles/goals.css';

const TODAY = new Date().toISOString().split('T')[0];

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
        status: typeof item.status === 'string' ? item.status : undefined,
      };
    }),
  };
}

export default function GoalsPage() {
  const navigate = useNavigate();
  const [goals, setGoals] = useState<Goal[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const [showForm, setShowForm] = useState(false);
  const [showAI, setShowAI] = useState(false);
  const [aiPrompt, setAiPrompt] = useState('');
  const [aiResponse, setAiResponse] = useState<Record<string, unknown> | string | null>(null);
  const [aiLoading, setAiLoading] = useState(false);
  const [savingAiPlan, setSavingAiPlan] = useState(false);

  // Поля цели
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [priority, setPriority] = useState('MEDIUM');
  const [startDate, setStartDate] = useState(TODAY);
  const [deadline, setDeadline] = useState('');
  const [dailyMinutes, setDailyMinutes] = useState('');

  // Первая задача (обязательна, бэкенд требует непустой список задач)
  const [taskTitle, setTaskTitle] = useState('');
  const [taskDesc, setTaskDesc] = useState('');
  const [taskPriority, setTaskPriority] = useState('MEDIUM');

  const [creating, setCreating] = useState(false);

  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const flash = (msg: string, type: 'success' | 'error' = 'success') => {
    if (type === 'success') { setSuccess(msg); setTimeout(() => setSuccess(''), 3000); }
    else setError(msg);
  };

  const fetchGoals = useCallback(async (p = 0) => {
    setLoading(true);
    setError('');
    try {
      const data = await goalsService.getGoals(p, 10);
      setGoals(data.content ?? []);
      setTotalPages(data.totalPages ?? 0);
      setPage(data.number ?? 0);
    } catch (err) {
      flash(extractApiError(err), 'error');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { fetchGoals(); }, [fetchGoals]);

  const resetForm = () => {
    setTitle(''); setDescription(''); setPriority('MEDIUM');
    setStartDate(TODAY); setDeadline(''); setDailyMinutes('');
    setTaskTitle(''); setTaskDesc(''); setTaskPriority('MEDIUM');
    setShowForm(false);
  };

  const handleCreate = async (e: FormEvent) => {
    e.preventDefault();
    setError('');

    if (!taskTitle.trim()) {
      setError('Добавьте хотя бы одну задачу (поле "Название задачи")');
      return;
    }

    setCreating(true);
    try {
      await goalsService.createGoal({
        title: title.trim(),
        description: description.trim() || undefined,
        priority,
        start_date: startDate,
        deadline: deadline || undefined,
        daily_time_minutes: dailyMinutes ? Number(dailyMinutes) : undefined,
        stages: [
          {
            title: taskTitle.trim(),
            description: taskDesc.trim() || undefined,
            priority: taskPriority,
            sortOrder: 0,
          },
        ],
      });
      resetForm();
      flash('Цель создана!');
      fetchGoals(page);
    } catch (err) {
      flash(extractApiError(err), 'error');
    } finally {
      setCreating(false);
    }
  };

  const handleDelete = async (id: number) => {
    if (!confirm('Удалить цель и все её задачи?')) return;
    try {
      await goalsService.deleteGoal(id);
      flash('Цель удалена');
      fetchGoals(page);
    } catch (err) {
      flash(extractApiError(err), 'error');
    }
  };

  const handleAiDecompose = async (e: FormEvent) => {
    e.preventDefault();
    if (!aiPrompt.trim()) return;
    setAiLoading(true);
    setAiResponse(null);
    setError('');
    try {
      const resp = await goalsService.aiDecompose(aiPrompt.trim());
      setAiResponse(resp);
    } catch (err) {
      flash(extractApiError(err), 'error');
    } finally {
      setAiLoading(false);
    }
  };

  const handleSaveAiPlan = async () => {
    const plan = parseAiPlan(aiResponse);
    if (!plan) {
      flash('AI вернул ответ, который нельзя сохранить как цель', 'error');
      return;
    }
    setSavingAiPlan(true);
    setError('');
    try {
      const createdGoal = await goalsService.createGoal({
        title: plan.title,
        description: plan.description,
        priority: plan.priority,
        start_date: plan.start_date,
        deadline: plan.deadline,
        daily_time_minutes: plan.daily_time_minutes,
        stages: plan.stages.map((stage, index) => ({
          ...stage,
          sortOrder: stage.sortOrder ?? index,
        })),
      });
      navigate(`/goals/${createdGoal.id}`);
    } catch (err) {
      flash(extractApiError(err), 'error');
    } finally {
      setSavingAiPlan(false);
    }
  };

  const aiPlan = (() => {
    try {
      return parseAiPlan(aiResponse);
    } catch {
      return null;
    }
  })();

  return (
    <div className="goals-page">
      <div className="goals-header">
        <h1>Мои цели</h1>
        <div className="goal-card-actions">
          <button className="btn btn-secondary" onClick={() => setShowAI(!showAI)}>
            {showAI ? '✕ Скрыть AI' : '✦ AI-цель'}
          </button>
          <button className="btn btn-primary" onClick={() => setShowForm(!showForm)}>
            {showForm ? '✕ Отмена' : '+ Новая цель'}
          </button>
        </div>
      </div>

      {error && <div className="alert alert-error">{error}</div>}
      {success && <div className="alert alert-success">{success}</div>}

      {showAI && (
        <div className="create-form">
          <h3>Создать цель через AI</h3>
          <form className="form-inline" onSubmit={handleAiDecompose}>
            <textarea
              placeholder="Опиши, какую цель хочешь получить от AI, и какие задачи она должна сгенерировать"
              value={aiPrompt}
              onChange={(e) => setAiPrompt(e.target.value)}
              style={{ minHeight: '100px' }}
            />
            <button className="btn btn-primary" type="submit" disabled={aiLoading || !aiPrompt.trim()}>
              {aiLoading ? 'AI думает...' : 'Сгенерировать план'}
            </button>
          </form>

          {aiResponse && (
            <div className="ai-response">
              <h4>Предпросмотр AI-плана</h4>
              {aiPlan ? (
                <div className="ai-preview">
                  <div className="ai-preview-header">
                    <div>
                      <h3>{aiPlan.title}</h3>
                      {aiPlan.description && <p>{aiPlan.description}</p>}
                    </div>
                    <div className="ai-preview-badges">
                      <span className="badge badge-todo">{aiPlan.priority}</span>
                      {aiPlan.daily_time_minutes != null && (
                        <span className="badge badge-progress">{aiPlan.daily_time_minutes} мин/день</span>
                      )}
                    </div>
                  </div>

                  <div className="ai-preview-meta">
                    <span>Старт: {aiPlan.start_date}</span>
                    {aiPlan.deadline && <span>Дедлайн: {aiPlan.deadline}</span>}
                    <span>Задач: {aiPlan.stages.length}</span>
                  </div>

                  <div className="ai-preview-list">
                    {aiPlan.stages.map((stage, index) => (
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
                    <button className="btn btn-primary" type="button" onClick={handleSaveAiPlan} disabled={savingAiPlan}>
                      {savingAiPlan ? 'Сохраняем...' : 'Подтвердить и создать цель'}
                    </button>
                    <button className="btn btn-secondary" type="button" onClick={() => setAiResponse(null)} disabled={savingAiPlan}>
                      Очистить
                    </button>
                  </div>
                </div>
              ) : (
                <pre>{typeof aiResponse === 'string' ? aiResponse : JSON.stringify(aiResponse, null, 2)}</pre>
              )}
            </div>
          )}
        </div>
      )}

      {showForm && (
        <div className="create-form">
          <h3>Создать цель</h3>
          <form className="form-inline" onSubmit={handleCreate}>

            <div style={{ borderBottom: '1px solid var(--color-border)', paddingBottom: '16px', marginBottom: '8px' }}>
              <p style={{ fontWeight: 600, marginBottom: '8px', fontSize: '0.9rem' }}>О цели</p>

              <input
                type="text"
                placeholder="Название цели *"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                required
              />
              <textarea
                placeholder="Описание (необязательно)"
                value={description}
                onChange={(e) => setDescription(e.target.value)}
              />
              <div className="form-row">
                <select value={priority} onChange={(e) => setPriority(e.target.value)} required>
                  <option value="LOW">Приоритет: Низкий</option>
                  <option value="MEDIUM">Приоритет: Средний</option>
                  <option value="HIGH">Приоритет: Высокий</option>
                </select>
                <input
                  type="number"
                  placeholder="Минут в день"
                  value={dailyMinutes}
                  onChange={(e) => setDailyMinutes(e.target.value)}
                  min="1"
                />
              </div>
              <div className="form-row">
                <div>
                  <label style={{ fontSize: '0.8rem', color: 'var(--color-text-secondary)', display: 'block', marginBottom: '4px' }}>
                    Дата начала *
                  </label>
                  <input
                    type="date"
                    value={startDate}
                    onChange={(e) => setStartDate(e.target.value)}
                    required
                    style={{ width: '100%' }}
                  />
                </div>
                <div>
                  <label style={{ fontSize: '0.8rem', color: 'var(--color-text-secondary)', display: 'block', marginBottom: '4px' }}>
                    Дедлайн
                  </label>
                  <input
                    type="date"
                    value={deadline}
                    onChange={(e) => setDeadline(e.target.value)}
                    style={{ width: '100%' }}
                  />
                </div>
              </div>
            </div>

            <div>
              <p style={{ fontWeight: 600, marginBottom: '8px', fontSize: '0.9rem' }}>
                Первая задача <span style={{ color: 'var(--color-error)', fontSize: '0.8rem' }}>(обязательна)</span>
              </p>
              <input
                type="text"
                placeholder="Название задачи *"
                value={taskTitle}
                onChange={(e) => setTaskTitle(e.target.value)}
              />
              <textarea
                placeholder="Описание задачи (необязательно)"
                value={taskDesc}
                onChange={(e) => setTaskDesc(e.target.value)}
                style={{ minHeight: '60px' }}
              />
              <select value={taskPriority} onChange={(e) => setTaskPriority(e.target.value)}>
                <option value="LOW">Приоритет задачи: Низкий</option>
                <option value="MEDIUM">Приоритет задачи: Средний</option>
                <option value="HIGH">Приоритет задачи: Высокий</option>
              </select>
            </div>

            <button
              className="btn btn-primary"
              type="submit"
              disabled={creating || !title.trim() || !taskTitle.trim()}
            >
              {creating ? 'Создаётся...' : 'Создать цель'}
            </button>
          </form>
        </div>
      )}

      {loading ? (
        <div className="loading">Загрузка...</div>
      ) : goals.length === 0 ? (
        <div className="empty-state">
          <p style={{ fontSize: '1.1rem', marginBottom: '8px' }}>Целей пока нет</p>
          <p>Нажмите «+ Новая цель», чтобы начать</p>
        </div>
      ) : (
        <>
          <div className="goals-list">
            {goals.map((goal) => (
              <div key={goal.id} className="goal-card">
                <div className="goal-card-header">
                  <div>
                    <h3>{goal.title}</h3>
                    {goal.priority && (
                      <span className={`badge badge-${goal.priority.toLowerCase()}`} style={{ fontSize: '0.75rem' }}>
                        {goal.priority}
                      </span>
                    )}
                  </div>
                  <div className="goal-card-actions">
                    <Link to={`/goals/${goal.id}`} className="btn btn-primary">
                      Открыть →
                    </Link>
                    <button className="btn btn-danger" onClick={() => handleDelete(goal.id)}>
                      Удалить
                    </button>
                  </div>
                </div>
                {goal.description && <p>{goal.description}</p>}
                <div className="goal-meta">
                  {goal.status && <span>Статус: {goal.status}</span>}
                  {goal.progress != null && <span>Прогресс: {goal.progress}%</span>}
                  {goal.deadline && <span>Дедлайн: {goal.deadline}</span>}
                </div>
              </div>
            ))}
          </div>

          {totalPages > 1 && (
            <div style={{ display: 'flex', gap: '8px', marginTop: '24px', justifyContent: 'center', alignItems: 'center' }}>
              <button className="btn btn-secondary" disabled={page === 0} onClick={() => fetchGoals(page - 1)}>← Назад</button>
              <span style={{ color: 'var(--color-text-secondary)' }}>{page + 1} / {totalPages}</span>
              <button className="btn btn-secondary" disabled={page >= totalPages - 1} onClick={() => fetchGoals(page + 1)}>Вперёд →</button>
            </div>
          )}
        </>
      )}
    </div>
  );
}
