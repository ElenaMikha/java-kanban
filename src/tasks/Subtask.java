package tasks;

public class Subtask extends Task {
    private long epicId;

    public Subtask(String name, String description, TaskStatus taskStatus, long epicId) {
        super(name, description, taskStatus);
        this.epicId = epicId;
    }

    public long getEpicId() {
        return epicId;
    }

    public void setEpicId(long epicId) {
        this.epicId = epicId;
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "epicId=" + epicId +
                '}';
    }
}
