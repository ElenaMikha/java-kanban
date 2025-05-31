package manager;

import tasks.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InMemoryTaskManager implements TaskManager {
    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final HashMap<Integer, Subtask> subtasks = new HashMap<>();
    private final HashMap<Integer, Epic> epics = new HashMap<>();
    private final HistoryManager historyManager = Managers.getDefaultHistory();

    private int generatorId = 1;

    @Override
    public Task getTasksById(int id) {
        Task task = tasks.get(id);
        historyManager.add(task);
        return task;
    }

    @Override
    public ArrayList<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public int createTask(Task task) {
        if (task == null) {
            return -1;
        }
        task.setId(getNextId());
        tasks.put(task.getId(), task);
        return task.getId();
    }

    @Override
    public void updateTask(Task task) {
        if (task == null) {
            return;
        }
        if (tasks.containsKey(task.getId())) {
            tasks.put(task.getId(), task);
        }
    }

    @Override
    public void deleteTaskById(int id) {
        tasks.remove(id);
    }

    @Override
    public void deleteTask() {
        tasks.clear();
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        historyManager.add(subtask);
        return subtask;
    }

    @Override
    public ArrayList<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public int createSubtask(Subtask subtask) {
        if (subtask == null) {
            return -1;
        }
        Integer epicId = (int) subtask.getEpicId();
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
        int newId = getNextId();
        if (newId == epicId) {
            return -1;
        }
        subtask.setId(newId);
        subtasks.put(newId, subtask);
        epic.getSubtasks().add(subtask);
        updateEpicStatus(epic);

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
        subtasks.put(id, subtask);
        Epic epic = epics.get((int) subtask.getEpicId());
        if (epic != null) {
            epic.updateSubtask(subtask);
            updateEpicStatus(epic);
        }
    }

    @Override
    public void deleteSubtaskById(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            Epic epic = epics.get((int) subtask.getEpicId());
            if (epic != null) {
                epic.getSubtasks().remove(subtask);
                updateEpicStatus(epic);
            }
        }
    }

    @Override
    public void deleteSubtasks() {
        for (Epic epic : epics.values()) {
            epic.getSubtasks().clear();
            epic.setTaskStatus(TaskStatus.NEW);
        }
        subtasks.clear();
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = epics.get(id);
        historyManager.add(epic);
        return epic;
    }

    @Override
    public ArrayList<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public int createEpic(Epic epic) {
        if (epic == null) {
            return -1;
        }
        epic.setId(getNextId());
        epics.put(epic.getId(), epic);
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
        }
    }

    @Override
    public void deleteEpicById(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            for (Subtask subtask : epic.getSubtasks()) {
                subtasks.remove(subtask.getId());
            }
        }
    }

    @Override
    public void deleteEpics() {
        epics.clear();
        subtasks.clear();
    }

    @Override
    public ArrayList<Subtask> getSubtaskFromEpic(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(epic.getSubtasks());
    }

    private void updateEpicStatus(Epic epic) {
        if (epic == null) {
            return;
        }
        ArrayList<Subtask> subtaskList = epic.getSubtasks();

        if (subtaskList.isEmpty()) {
            epic.setTaskStatus(TaskStatus.NEW);
            return;
        }

        boolean subtaskNew = true;
        boolean subtaskDone = true;

        for (Subtask subtask : subtaskList) {
            TaskStatus status = subtask.getTaskStatus();
            if (status != TaskStatus.NEW) {
                subtaskNew = false;
            }
            if (status != TaskStatus.DONE) {
                subtaskDone = false;
            }
        }

        if (subtaskNew) {
            epic.setTaskStatus(TaskStatus.NEW);
        } else if (subtaskDone) {
            epic.setTaskStatus(TaskStatus.DONE);
        } else {
            epic.setTaskStatus(TaskStatus.IN_PROGRESS);
        }
    }

    public void add(Task task) {
        historyManager.add(task);
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    private int getNextId() {
        return generatorId++;
    }
}


