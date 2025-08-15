package manager;

import tasks.Epic;
import tasks.Subtask;
import tasks.Task;

import java.util.List;

public interface TaskManager {

    Task getTasksById(int id);

    List<Task> getTasks();

    int createTask(Task task);

    void updateTask(Task task);

    void deleteTaskById(int id);

    void deleteTask();

    Subtask getSubtaskById(int id);

    List<Subtask> getSubtasks();

    int createSubtask(Subtask subtask);

    void updateSubtask(Subtask subtask);

    void deleteSubtaskById(int id);

    void deleteSubtasks();

    Epic getEpicById(int id);

    List<Epic> getEpics();

    int createEpic(Epic epic);

    void updateEpic(Epic epic);

    void deleteEpicById(int id);

    void deleteEpics();

    List<Subtask> getSubtaskFromEpic(int epicId);

    List<Task> getHistory();

    List<Task> getPrioritizedTasks();
}
