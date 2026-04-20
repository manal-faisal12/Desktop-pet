import javax.swing.*;
import java.util.ArrayList;
//due within 1 day, and not yet completed, sends prompt

public class AlertService {

    private TaskManager manager;
    private JFrame      owner;

    public AlertService(JFrame owner, TaskManager manager) {
        this.owner   = owner;
        this.manager = manager;
    }
    private int maxtimes = 0;
    public static int latestTaskCount = 0;
    // Called once when the app starts
        public void checkAndAlert() {
            ArrayList<Task> alertTasks = manager.getAlertTasks();
            latestTaskCount = alertTasks.size();
            int overdueCount = manager.countOverdue();
            if (overdueCount > 0 && FoxDesktopPet.currentFox != null && maxtimes<3) {
                FoxDesktopPet.currentFox.friendshipManager.decrease(overdueCount * 0.2);//each task missed means less friendship/trust each time u open the fox without completing overdue tasks it will lose trust in u
                FoxDesktopPet.currentFox.speak(new FoxDesktopPet.FoxOverdue(overdueCount));
                maxtimes++;
            }
            if (alertTasks.size() == 0) return;

        // Build the message to show
        String message = "⏰  Tasks due within 1 day:\n\n";

        for (int i = 0; i < alertTasks.size(); i++) {
            Task t = alertTasks.get(i);
            message = message + t.getTitle() + "  (" + t.getSubject() + ")\n" + "    Due: " + t.getSubmissionDate().format(Task.DISPLAY_FORMAT)
                    + "\n\n";
        }

        JOptionPane.showMessageDialog(owner, message,
                "Due Soon — " + alertTasks.size() + " task(s)",
                JOptionPane.WARNING_MESSAGE);
    }
}
