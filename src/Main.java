import manager.Managers;
import manager.TaskManager;
import tasks.*;

public class Main {

    public static void main(String[] args) {
        TaskManager taskManager = Managers.getDefault();

        // Создание 4 задач
        Task task1 = new Task("Task 1", "Description 1", TaskStatus.NEW);
        Task task2 = new Task("Task 2", "Description 2", TaskStatus.NEW);
        Task task3 = new Task("Task 3", "Description 3", TaskStatus.NEW);
        Task task4 = new Task("Task 4", "Description 4", TaskStatus.NEW);
        taskManager.createTask(task1);
        taskManager.createTask(task2);
        taskManager.createTask(task3);
        taskManager.createTask(task4);
        System.out.println();

        // Создание эпика с 4 подзадачами
        Epic epic1 = new Epic("Epic 1", "Two subtasks");
        taskManager.createEpic(epic1);
        int epic1Id = epic1.getId();

        Subtask subtask1 = new Subtask("Subtask 1", "Description sub1", TaskStatus.NEW, epic1Id);
        Subtask subtask2 = new Subtask("Subtask 2", "Description sub2", TaskStatus.NEW, epic1Id);
        Subtask subtask3 = new Subtask("Subtask 3", "Description sub3", TaskStatus.NEW, epic1Id);
        Subtask subtask4 = new Subtask("Subtask 4", "Description sub4", TaskStatus.NEW, epic1Id);
        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);
        taskManager.createSubtask(subtask3);
        taskManager.createSubtask(subtask4);
        System.out.println();

        // Создание эпика с одной подзадачей
        Epic epic2 = new Epic("Epic 2", "One subtask");
        taskManager.createEpic(epic2);
        int epic2Id = epic2.getId();

        Subtask subtask5 = new Subtask("Subtask 5", "Description sub5", TaskStatus.NEW, epic2Id);
        taskManager.createSubtask(subtask5);
        System.out.println();

        // Печать эпиков, задач и подзадач
        System.out.println("Печать эпиков, задач и подзадач");
        System.out.println(taskManager.getTasks());
        System.out.println(taskManager.getEpics());
        System.out.println(taskManager.getSubtasks());
        System.out.println();

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
        System.out.println("Task 2: " + taskManager.getTasksById(task2.getId()));
        System.out.println("Task 3: " + taskManager.getTasksById(task3.getId()));
        System.out.println("Task 4: " + taskManager.getTasksById(task4.getId()));
        System.out.println("Subtask 1: " + taskManager.getSubtaskById(subtask1.getId()));
        System.out.println("Subtask 2: " + taskManager.getSubtaskById(subtask2.getId()));
        System.out.println("Subtask 3: " + taskManager.getSubtaskById(subtask3.getId()));
        System.out.println("Subtask 4: " + taskManager.getSubtaskById(subtask4.getId()));
        System.out.println("Subtask 5: " + taskManager.getSubtaskById(subtask5.getId()));
        System.out.println("Epic 1: " + taskManager.getEpicById(epic1Id));
        System.out.println("Epic 2: " + taskManager.getEpicById(epic2Id));

        // Удаление одной задачи и одного эпика
        System.out.println("Удаление одной задачи и одного эпика");
        taskManager.deleteTaskById(task2.getId());
        taskManager.deleteEpicById(epic1Id);

        //История
        System.out.println("Проверяем историю");
        System.out.println(taskManager.getHistory());
        System.out.println();
    }
}
