import java.util.ArrayList;
public class TaskManager {
    private ArrayList<Task> tasks;
    private static final int ALERT_DAYS = 1;

    public TaskManager() {
        tasks = new ArrayList<Task>();
    }

    public void load() {
        tasks = TaskStorage.loadTasks();
    }

    public void save() {
        TaskStorage.saveTasks(tasks);
    }

    public void addTask(Task t) {
        tasks.add(t);
        save();
    }

    //this code removes task based on the id
    void removeTask(String id) {
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).getId().equals(id)) {
                tasks.remove(i);
                break; // stop looping once found
            }
        }
        save();
    }

    //finds task based on id
    Task Task_Id(String id) {
        for (Task task : tasks) {
            if (task.getId().equals(id)) {
                return task;
            }
        }
        return null;
    }

    //if task found by id then flip the task to opposite of what it is
    void Completed_Flip(String id) {
        Task t = Task_Id(id);
        if (t != null) {
            t.setCompleted(!t.getCompleted());
            save();
        }
    }

    ArrayList<Task> getTasks() {
        return tasks;
    }

    int Completed_Number() {
        int count = 0;
        for (Task task : tasks) {
            if (task.getCompleted()) {
                count++;
            }
        }
        return count;
    }

    int countPending() {
        int count = 0;
        for (Task task : tasks) {
            if (!task.getCompleted()) {
                count++;
            }
        }
        return count;
    }
    int countOverdue() {
        int count = 0;
        for (Task task : tasks) {
            if (task.isOverdue()) {
                count++;
            }
        }
        return count;
    }

    ArrayList<Task> getAlertTasks() {
        ArrayList<Task> alertList = new ArrayList<Task>();
        for (Task task : tasks) {
            if (task.isDueSoon(ALERT_DAYS)) {
                alertList.add(task);
            }
        }
        return alertList;
    }

    ArrayList<Task> filterByType(String type) {
        ArrayList<Task> result = new ArrayList<Task>();
        for (Task task : tasks) {
            if (task.getTaskType().equals(type)) {
                result.add(task);
            }
        }
        return result;
    }

    ArrayList<Task> filterCompleted() { //completed tasks
        ArrayList<Task> result = new ArrayList<Task>();
        for (Task task : tasks) {
            if (task.getCompleted()) {
                result.add(task);
            }
        }
        return result;
    }

    ArrayList<Task> filterOverdue() { //overdue results
        ArrayList<Task> result = new ArrayList<Task>();
        for (Task task : tasks) {
            if (task.isOverdue()) {
                result.add(task);
            }
        }
        return result;
    }
//this part needs cropping cutting since no need for id
    String generateId() {
        // Uses the current time in milliseconds — guaranteed to be unique
        return "T" + System.currentTimeMillis();
    }
    int totalTasks() {
        return tasks.size();
    }
}
