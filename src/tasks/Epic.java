package tasks;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.time.Duration;

public class Epic extends Task {
    private ArrayList<Subtask> subtasks = new ArrayList<>();
    protected LocalDateTime endTime;

    public Epic(String name, String description) {
        super(name, description, TaskStatus.NEW);
        this.duration = null;
        this.startTime = null;
        this.endTime = null;
    }

    public ArrayList<Subtask> getSubtasks() {
        if (subtasks == null) {
            subtasks = new ArrayList<>();
        }
        return subtasks;
    }

    public void setSubtasks(ArrayList<Subtask> subtasks) {
        this.subtasks = (subtasks == null) ? new ArrayList<>() : subtasks;
    }

    public void updateSubtask(Subtask subtask) {
        ArrayList<Subtask> list = getSubtasks();
        list.remove(subtask);
        list.add(subtask);

    }

    @Override
    public TaskType getType() {
        return TaskType.EPIC;
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void recalculateTimeFromSubtasks() {
        ArrayList<Subtask> list = getSubtasks();
        if (list.isEmpty()) {
            this.duration = Duration.ZERO;
            this.startTime = null;
            this.endTime = null;
            return;
        }

        Duration sum = Duration.ZERO;
        LocalDateTime minStart = null;
        LocalDateTime maxEnd = null;

        for (Subtask s : list) {
            if (s.getDuration() != null) {
                sum = sum.plus(s.getDuration());
            }
            LocalDateTime st = s.getStartTime();
            LocalDateTime en = s.getEndTime();

            if (st != null) {
                if (minStart == null || st.isBefore(minStart)) {
                    minStart = st;
                }
            }
            if (en != null) {
                if (maxEnd == null || en.isAfter(maxEnd)) {
                    maxEnd = en;
                }
            }
        }

        this.duration = sum;
        this.startTime = minStart;
        this.endTime = maxEnd;
    }

    @Override
    public String toString() {
        return "Epic{" +
                "subtasks=" + getSubtasks() +
                ", id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", taskStatus=" + taskStatus +
                '}';
    }
}
