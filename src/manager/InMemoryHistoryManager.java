package manager;

import tasks.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {
    private Node head;
    private Node tail;
    private final Map<Integer, Node> nodeMap = new HashMap<>();


    @Override
    public void add(Task task) {
        if (task == null) return;
        remove(task.getId());
        linkLast(task);
    }

    @Override
    public List<Task> getHistory() {
        return getTasks();
    }

    @Override
    public void remove(int id) {
        Node node = nodeMap.remove(id);
        if (node != null) {
            removeNode(node);
        }
    }

    private void linkLast(Task task) {
        Node newNode = new Node(tail, task, null);
        if (tail != null) {
            tail.next = newNode;
        }
        tail = newNode;

        if (head == null) {
            head = newNode;
        }

        nodeMap.put(task.getId(), newNode);
    }

    private List<Task> getTasks() {
        List<Task> tasks = new ArrayList<>();
        for (Node current = head; current != null; current = current.next) {
            tasks.add(current.data);
        }
        return tasks;
    }

    private void removeNode(Node node) {
        Node prev = node.prev;
        Node next = node.next;

        if (prev != null) {
            prev.next = next;
        } else {
            head = next;
        }

        if (next != null) {
            next.prev = prev;
        } else {
            tail = prev;
        }
    }

    private static class Node {
        private Node prev;
        private Task data;
        private Node next;

        public Node(Node prev, Task data, Node next) {
            this.prev = prev;
            this.data = data;
            this.next = next;
        }
    }

}

