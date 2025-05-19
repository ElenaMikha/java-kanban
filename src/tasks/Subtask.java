package tasks;

public class Subtask extends Task {
    private final long epicId;

    public Subtask(String name, String description, TaskStatus taskStatus, long epicId) {
        super(name, description, taskStatus);
        this.epicId = epicId;
    }

    public long getEpicId() {
        return epicId;
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "epicId=" + epicId +
                ", id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", taskStatus=" + taskStatus +
                '}';
    }
}
