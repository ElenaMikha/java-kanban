package manager;

import exception.ManagerSaveException;
import exception.ManagerLoadException;
import tasks.*;

import java.io.*;
import java.nio.file.Files;
import java.util.List;
import java.time.Duration;
import java.time.LocalDateTime;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;
    private static final String HEADER = "id,type,name,status,description,epic,startTime,durationMinutes\n";

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    @Override
    public int createTask(Task task) {
        int id = super.createTask(task);
        save();
        return id;
    }

    @Override
    public int createEpic(Epic epic) {
        int id = super.createEpic(epic);
        save();
        return id;
    }

    @Override
    public int createSubtask(Subtask subtask) {
        int id = super.createSubtask(subtask);
        save();
        return id;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void deleteTaskById(int id) {
        super.deleteTaskById(id);
        save();
    }

    @Override
    public void deleteEpicById(int id) {
        super.deleteEpicById(id);
        save();
    }

    @Override
    public void deleteSubtaskById(int id) {
        super.deleteSubtaskById(id);
        save();
    }

    @Override
    public void deleteTask() {
        super.deleteTask();
        save();
    }

    @Override
    public void deleteEpics() {
        super.deleteEpics();
        save();
    }

    @Override
    public void deleteSubtasks() {
        super.deleteSubtasks();
        save();
    }

    protected void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(HEADER);

            for (Task task : getTasks()) {
                writer.write(toString(task) + "\n");
            }
            for (Epic epic : getEpics()) {
                writer.write(toString(epic) + "\n");
            }
            for (Subtask subtask : getSubtasks()) {
                writer.write(toString(subtask) + "\n");
            }

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при сохранении в файл", e);
        }
    }

    private String toString(Task task) {
        String epicId = "";
        if (task instanceof Subtask) {
            epicId = String.valueOf(((Subtask) task).getEpicId());
        }

        String start = task.getStartTime() != null ? task.getStartTime().toString() : "";
        String durationMin = task.getDuration() != null ? String.valueOf(task.getDuration().toMinutes()) : "";

        return String.join(",",
                String.valueOf(task.getId()),
                task.getType().name(),
                task.getName(),
                task.getTaskStatus().name(),
                task.getDescription(),
                epicId,
                start,
                durationMin
        );
    }

    private static Task fromString(String value) {
        String[] fields = value.split(",", -1);
        int id = Integer.parseInt(fields[0]);
        TaskType type = TaskType.valueOf(fields[1]);
        String name = fields[2];
        TaskStatus status = TaskStatus.valueOf(fields[3]);
        String description = fields[4];
        String epicField = fields.length > 5 ? fields[5] : "";
        String startField = fields.length > 5 ? fields[6] : "";
        String durationField = fields.length > 5 ? fields[7] : "";

        LocalDateTime start = startField.isEmpty() ? null : LocalDateTime.parse(startField);
        Duration dur = durationField.isEmpty() ? null : Duration.ofMinutes(Long.parseLong(durationField));


        switch (type) {
            case TASK:
                Task task = new Task(name, description, status);
                task.setId(id);
                task.setStartTime(start);
                task.setDuration(dur);
                return task;
            case EPIC:
                Epic epic = new Epic(name, description);
                epic.setId(id);
                epic.setTaskStatus(status);
                return epic;
            case SUBTASK:
                int epicId = Integer.parseInt(epicField);
                Subtask subtask = new Subtask(name, description, status, epicId);
                subtask.setId(id);
                subtask.setStartTime(start);
                subtask.setDuration(dur);
                return subtask;
            default:
                throw new IllegalArgumentException("Неизвестный тип задачи: " + type);
        }
    }


    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        if (!file.exists()) return manager;

        try {
            List<String> lines = Files.readAllLines(file.toPath());
            int maxId = 0;

            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (line.isEmpty()) continue;

                Task task = fromString(line);
                int id = task.getId();
                if (id > maxId) {
                    maxId = id;
                }

                if (task instanceof Epic) {
                    manager.epics.put(id, (Epic) task);
                } else if (task instanceof Subtask) {
                    manager.subtasks.put(id, (Subtask) task);
                } else {
                    manager.tasks.put(id, task);
                }
            }

            for (Subtask subtask : manager.subtasks.values()) {
                Epic epic = manager.epics.get(subtask.getEpicId());
                if (epic != null) {
                    epic.getSubtasks().add(subtask);
                }
            }

            for (Epic epic : manager.epics.values()) {

                if (epic.getSubtasks().isEmpty()) {
                    epic.setTaskStatus(TaskStatus.NEW);
                } else {
                    boolean allNew = true;
                    boolean allDone = true;
                    for (Subtask subtask : epic.getSubtasks()) {
                        if (subtask.getTaskStatus() != TaskStatus.NEW) allNew = false;
                        if (subtask.getTaskStatus() != TaskStatus.DONE) allDone = false;
                    }
                    if (allNew) epic.setTaskStatus(TaskStatus.NEW);
                    else if (allDone) epic.setTaskStatus(TaskStatus.DONE);
                    else epic.setTaskStatus(TaskStatus.IN_PROGRESS);
                }
                epic.recalculateTimeFromSubtasks();
            }

            for (Task task : manager.tasks.values()) {
                manager.addToPrioritizedIfNeeded(task);
            }
            for (Subtask subtask : manager.subtasks.values()) {
                manager.addToPrioritizedIfNeeded(subtask);
            }

            manager.generatorId = maxId + 1;

        } catch (IOException e) {
            throw new ManagerLoadException("Ошибка при загрузке из файла", e);
        }

        return manager;
    }
}