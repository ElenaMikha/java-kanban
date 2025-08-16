package manager;

public class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    @Override
    protected InMemoryTaskManager makeManager() {
        return new InMemoryTaskManager();
    }
}
