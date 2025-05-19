import manager.TaskManager;
import tasks.*;

public class Main {

    public static void main(String[] args) {
        TaskManager taskManager = new TaskManager();

        // Создание двух задач
        Task task1 = new Task("Task 1", "Description 1", TaskStatus.NEW);
        Task task2 = new Task("Task 2", "Description 2", TaskStatus.NEW);
        taskManager.createTask(task1);
        taskManager.createTask(task2);

        // Создание эпика с двумя подзадачами
        Epic epic1 = new Epic("Epic 1", "Two subtasks");
        taskManager.createEpic(epic1);
        int epic1Id = epic1.getId();

        Subtask subtask1 = new Subtask("Subtask 1", "Description sub1", TaskStatus.NEW, epic1Id);
        Subtask subtask2 = new Subtask("Subtask 2", "Description sub2", TaskStatus.NEW, epic1Id);
        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);

        // Создание эпика с одной подзадачей
        Epic epic2 = new Epic("Epic 2", "One subtask");
        taskManager.createEpic(epic2);
        int epic2Id = epic2.getId();

        Subtask subtask3 = new Subtask("Subtask 3", "Description sub3", TaskStatus.NEW, epic2Id);
        taskManager.createSubtask(subtask3);

        // Печать эпиков, задач и подзадач
        System.out.println("Печать эпиков, задач и подзадач");
        System.out.println(taskManager.getTasks());
        System.out.println(taskManager.getEpics());
        System.out.println(taskManager.getSubtasks());

        // Изменение статусов
        task1.setTaskStatus(TaskStatus.DONE);
        taskManager.updateTask(task1);

        subtask1.setTaskStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateSubtask(subtask1);

        subtask2.setTaskStatus(TaskStatus.DONE);
        taskManager.updateSubtask(subtask2);

        subtask3.setTaskStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateSubtask(subtask3);

        // Проверка сохранения и пересчёта статусов
        System.out.println("Проверка сохранения и пересчёта статусов");
        System.out.println("Task 1: " + taskManager.getTasksById(task1.getId()));
        System.out.println("Subtask 1: " + taskManager.getSubtaskById(subtask1.getId()));
        System.out.println("Subtask 2: " + taskManager.getSubtaskById(subtask2.getId()));
        System.out.println("Subtask 3: " + taskManager.getSubtaskById(subtask3.getId()));
        System.out.println("Epic 1: " + taskManager.getEpicById(epic1Id));
        System.out.println("Epic 2: " + taskManager.getEpicById(epic2Id));

        // Удаление одной задачи и одного эпика
        System.out.println("Удаление одной задачи и одного эпика");
        taskManager.deleteTaskById(task2.getId());
        taskManager.deleteEpicById(epic1Id);


    }
}
