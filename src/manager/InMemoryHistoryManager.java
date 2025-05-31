package manager;

import tasks.Task;
import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private final List<Task> history = new ArrayList<>();

    @Override
    public void add(Task task) {
        if (task == null) return;
        history.add(task);
        if (history.size() > 10) {
            history.remove(0);

        }
    }

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(history);
    }

}
