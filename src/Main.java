import tasks.*;

public class Main {

    public static void main(String[] args) {
        TaskManager taskManager = new TaskManager();

        // Задачи
        Task task1 = new Task("Task 1", "Description 1", TaskStatus.NEW);
        Task task2 = new Task("Task 2", "Description 2", TaskStatus.NEW);
        taskManager.createTask(task1);
        taskManager.createTask(task2);

        // Удаление задач
        taskManager.deleteTask();
        System.out.println(taskManager.getTasks());

        /* Эпик с двумя подзадачами
        Epic epic1 = new Epic("Epic 1", "Two subtasks");
        taskManager.createEpic(epic1);

        Subtask subtask1 = new Subtask("Subtask 1", "Description sub1", TaskStatus.NEW, epic1.getId());
        Subtask subtask2 = new Subtask("Subtask 2", "Description sub2", TaskStatus.NEW, epic1.getId());
        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);

        // Эпик с одной подзадачей
        Epic epic2 = new Epic("Epic 2", "One subtask");
        taskManager.createEpic(epic2);

        Subtask subtask3 = new Subtask("Subtask 3", "Description sub3", TaskStatus.NEW, epic2.getId());
        taskManager.createSubtask(subtask3);

        // Все объекты
        System.out.println("All Tasks:");
        System.out.println(taskManager.getTasks());

        System.out.println("All Epics:");
        System.out.println(taskManager.getEpics());

        System.out.println("All Subtasks:");
        System.out.println(taskManager.getSubtasks());

        // Изменение статуса
        task1.setTaskStatus(TaskStatus.DONE);
        taskManager.updateTask(task1);

        subtask1.setTaskStatus(TaskStatus.DONE);
        taskManager.updateSubtask(subtask1);

        subtask3.setTaskStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateSubtask(subtask3);

        // После изменений статусов
        System.out.println("After status updates:");
        System.out.println("Task 1: " + taskManager.getTasksById(task1.getId()));
        System.out.println("Subtask 1: " + taskManager.getSubtaskById(subtask1.getId()));
        System.out.println("Subtask 3: " + taskManager.getSubtaskById(subtask3.getId()));

        // Обновление статусов эпиков
        System.out.println("Epic 1 status (should be DONE or IN_PROGRESS): " + taskManager.getEpicById(epic1.getId()).getTaskStatus());
        System.out.println("Epic 2 status (should be IN_PROGRESS): " + taskManager.getEpicById(epic2.getId()).getTaskStatus());

        // Удаление одной задачи и одного эпика
        taskManager.deleteTaskById(task2.getId());
        taskManager.deleteEpicById(epic1.getId());

        // Все объекты после удаления

        System.out.println("All Tasks:");
        System.out.println(taskManager.getTasks());

        System.out.println("All Epics:");
        System.out.println(taskManager.getEpics());

        System.out.println("All Subtasks:");
        System.out.println(taskManager.getSubtasks());*/

    }
}
