package manager;

import tasks.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.time.LocalDateTime;
import java.util.TreeSet;

public class InMemoryTaskManager implements TaskManager {
    protected final HashMap<Integer, Task> tasks = new HashMap<>();
    protected final HashMap<Integer, Subtask> subtasks = new HashMap<>();
    protected final HashMap<Integer, Epic> epics = new HashMap<>();
    protected final HistoryManager historyManager = Managers.getDefaultHistory();
    protected int generatorId = 1;

    @Override
    public Task getTasksById(int id) {
        Task task = tasks.get(id);
        historyManager.add(task);
        return task;
    }

    @Override
    public List<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public int createTask(Task task) {
        if (task == null) {
            return -1;
        }
        if (hasAnyOverlap(task)) return -1;
        task.setId(getNextId());
        tasks.put(task.getId(), task);
        addToPrioritizedIfNeeded(task);
        return task.getId();
    }

    @Override
    public void updateTask(Task task) {
        if (task == null) {
            return;
        }
        if (!tasks.containsKey(task.getId())) return;
        if (hasAnyOverlap(task)) return;

        Task old = tasks.put(task.getId(), task);
        removeFromPrioritized(old);
        addToPrioritizedIfNeeded(task);
    }

    @Override
    public void deleteTaskById(int id) {
        Task old = tasks.remove(id);
        if (old != null) {
            removeFromPrioritized(old);
            historyManager.remove(id);
        }
    }

    @Override
    public void deleteTask() {
        tasks.values().forEach(task -> {
            removeFromPrioritized(task);
            historyManager.remove(task.getId());
        });
        tasks.clear();
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        historyManager.add(subtask);
        return subtask;
    }

    @Override
    public List<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public int createSubtask(Subtask subtask) {
        if (subtask == null) {
            return -1;
        }
        Integer epicId = subtask.getEpicId();
        if (epicId == null) {
            return -1;
        }
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return -1;
        }
        Integer subtaskId = subtask.getId();
        if (subtaskId != null && subtaskId.equals(epicId)) {
            return -1;
        }
        if (hasAnyOverlap(subtask)) return -1;

        int newId = getNextId();
        if (newId == epicId) {
            return -1;
        }
        subtask.setId(newId);
        subtasks.put(newId, subtask);
        epic.getSubtasks().add(subtask);
        addToPrioritizedIfNeeded(subtask);
        updateEpicStatusAndTime(epic);

        return newId;
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtask == null) {
            return;
        }
        int id = subtask.getId();
        if (!subtasks.containsKey(id)) {
            return;
        }
        if (hasAnyOverlap(subtask)) return;

        Subtask old = subtasks.put(id, subtask);
        removeFromPrioritized(old);
        addToPrioritizedIfNeeded(subtask);
        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            epic.updateSubtask(subtask);
            updateEpicStatusAndTime(epic);
        }
    }

    @Override
    public void deleteSubtaskById(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            removeFromPrioritized(subtask);
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.getSubtasks().remove(subtask);
                updateEpicStatusAndTime(epic);
            }
            historyManager.remove(id);
        }
    }

    @Override
    public void deleteSubtasks() {
        subtasks.values().forEach(subtask -> {
            removeFromPrioritized(subtask);
            historyManager.remove(subtask.getId());
        });
        epics.values().forEach(epic -> {
            epic.getSubtasks().clear();
            updateEpicStatusAndTime(epic);
        });
        subtasks.clear();
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = epics.get(id);
        historyManager.add(epic);
        return epic;
    }

    @Override
    public List<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public int createEpic(Epic epic) {
        if (epic == null) {
            return -1;
        }
        epic.setId(getNextId());
        epics.put(epic.getId(), epic);
        updateEpicStatusAndTime(epic);
        return epic.getId();
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epic == null) {
            return;
        }
        Epic storedEpic = epics.get(epic.getId());
        if (storedEpic != null) {
            storedEpic.setName(epic.getName());
            storedEpic.setDescription(epic.getDescription());
            updateEpicStatusAndTime(storedEpic);
        }
    }

    @Override
    public void deleteEpicById(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            epic.getSubtasks().forEach(subtask -> {
                subtasks.remove(subtask.getId());
                removeFromPrioritized(subtask);
                historyManager.remove(subtask.getId());
            });
            historyManager.remove(id);
        }
    }

    @Override
    public void deleteEpics() {
        epics.values().forEach(epic -> {
            historyManager.remove(epic.getId());
            epic.getSubtasks().forEach(subtask -> {
                historyManager.remove(subtask.getId());
                removeFromPrioritized(subtask);
            });
        });
        epics.clear();
        subtasks.clear();
    }

    @Override
    public List<Subtask> getSubtaskFromEpic(int epicId) {
        Epic epic = epics.get(epicId);
        return epic == null
                ? List.of()
                : epic.getSubtasks().stream().toList();
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    private int getNextId() {
        return generatorId++;
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritized);
    }

    protected final TreeSet<Task> prioritized = new TreeSet<>((a, b) -> {
        LocalDateTime taskAStart = a.getStartTime();
        LocalDateTime taskBStart = b.getStartTime();

        int byStart;
        if (taskAStart == null && taskBStart == null) {
            byStart = 0;
        } else if (taskAStart == null) {
            byStart = 1;
        } else if (taskBStart == null) {
            byStart = -1;
        } else {
            byStart = taskAStart.compareTo(taskBStart);
        }
        if (byStart != 0) return byStart;

        Integer taskAId = a.getId();
        Integer taskBid = b.getId();
        if (taskAId == null && taskBid == null) return System.identityHashCode(a) - System.identityHashCode(b);
        if (taskAId == null) return -1;
        if (taskBid == null) return 1;
        return taskAId.compareTo(taskBid);
    });

    protected void addToPrioritizedIfNeeded(Task task) {
        if (task != null && task.getStartTime() != null) {

            if (task.getType() != TaskType.EPIC) {
                prioritized.add(task);
            }
        }
    }

    protected void removeFromPrioritized(Task task) {
        if (task != null) {
            prioritized.remove(task);
        }
    }

    private boolean isOverlapping(Task a, Task b) {
        if (a == null || b == null) return false;
        if (a.getStartTime() == null || a.getEndTime() == null) return false;
        if (b.getStartTime() == null || b.getEndTime() == null) return false;

        boolean cond1 = a.getStartTime().isBefore(b.getEndTime());
        boolean cond2 = b.getStartTime().isBefore(a.getEndTime());
        return cond1 && cond2;
    }

    public boolean hasAnyOverlap(Task candidate) {
        if (candidate == null) return false;
        if (candidate.getStartTime() == null || candidate.getEndTime() == null) return false;

        return prioritized.stream()
                .filter(task -> candidate.getId() == null || !task.getId().equals(candidate.getId()))
                .takeWhile(task -> task.getStartTime().isBefore(candidate.getEndTime()))
                .anyMatch(task -> isOverlapping(candidate, task));
    }

    protected void updateEpicStatusAndTime(Epic epic) {
        if (epic == null) return;

        ArrayList<Subtask> list = epic.getSubtasks();
        if (list.isEmpty()) {
            epic.setTaskStatus(TaskStatus.NEW);
            epic.recalculateTimeFromSubtasks();
            return;
        }

        boolean allNew = true;
        boolean allDone = true;
        for (Subtask subtask : list) {
            if (subtask.getTaskStatus() != TaskStatus.NEW) allNew = false;
            if (subtask.getTaskStatus() != TaskStatus.DONE) allDone = false;
        }
        if (allNew) epic.setTaskStatus(TaskStatus.NEW);
        else if (allDone) epic.setTaskStatus(TaskStatus.DONE);
        else epic.setTaskStatus(TaskStatus.IN_PROGRESS);

        epic.recalculateTimeFromSubtasks();

    }
}


