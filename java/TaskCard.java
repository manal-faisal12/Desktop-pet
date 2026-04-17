import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

/*
    Shows completion checkbox

   One card panel displayed in the task list for each task.
   Shows the task's icon, title, subject, due date, status,
   a completion checkbox, and a delete button.
*/

public class TaskCard extends JPanel {

    private static final Color Blue_Dark  = new Color(0x1A1A2E);
    private static final Color Blue_Darker = new Color(0x141420);
    private static final Color Overdue_Color = new Color(0x2A1420);
    private static final Color BG_SOON    = new Color(0x1A240A);
    private static final Color TEXT       = new Color(0xF0F0FF);
    private static final Color SUBTLE     = new Color(0x8888AA);
    private static final Color ACCENT     = new Color(0x7C3AED);
    private static final Color SUCCESS    = new Color(0x00C897);
    private static final Color WARNING    = new Color(0xF5A623);
    private static final Color DANGER     = new Color(0xFF4D6D);
    //colors for the panel


    private Task     task;
    private Runnable onToggle;
    private Runnable onDelete;
    //threadable will allow multiple part of the program to work together
    //runnable extends thread so still call and override run without extending 2 classes

    public TaskCard(Task task, Runnable onToggle, Runnable onDelete) {
        this.task     = task;
        this.onToggle = onToggle;
        this.onDelete = onDelete;

        setLayout(new BorderLayout(10, 0));
        setBackground(Card_Color());
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 105));
        setBorder(new CompoundBorder(
            new LineBorder(Border_Color(), 1),
            new EmptyBorder(10, 12, 10, 12)));

        // checkbox for completed
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);
        JCheckBox checkBox = new JCheckBox(); //creates checker box
        checkBox.setSelected(task.getCompleted()); //sets state of button
        checkBox.setOpaque(false); //the box is not opaque so can see not all border
        checkBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        //component is check box, button anything that is interactable
        checkBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onToggle.run(); //functional interface thats functional method is run
                //operation doesnot return value
            }
        });
        left.add(Box.createVerticalGlue());
        left.add(checkBox);
        left.add(Box.createVerticalGlue()); //creates vertical glue component
        add(left, BorderLayout.WEST);

        // ── Centre: task info ─────────────────────────────────────────────────
        JPanel centre = new JPanel();
        centre.setLayout(new BoxLayout(centre, BoxLayout.Y_AXIS));
        centre.setOpaque(false);
        centre.setBorder(new EmptyBorder(0, 6, 0, 0));

        // Title + type badge + difficulty badge
        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        titleRow.setOpaque(false);

        JLabel titleLabel = new JLabel(task.getTitle());
        titleLabel.setFont(new Font("Segue UI", Font.BOLD, 14));
        if (task.getCompleted()) {
            // Show strikethrough if completed
            titleLabel.setText("<html><s>" + task.getTitle() + "</s></html>");
            titleLabel.setForeground(SUBTLE);
        } else {
            titleLabel.setForeground(TEXT);
        }

        JLabel typeBadge = makeBadge(task.getTaskType(), ACCENT);
        JLabel diffBadge = makeBadge(task.getDifficulty(), Difficulty_Color());

        titleRow.add(titleLabel);
        titleRow.add(typeBadge);
        titleRow.add(diffBadge);
        centre.add(titleRow);

        // Subject + extra info
        JLabel subjectLabel = new JLabel(task.getSubject() + "  ·  " + task.getExtraInfo());
        subjectLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subjectLabel.setForeground(SUBTLE);
        centre.add(subjectLabel);
        centre.add(Box.createVerticalStrut(4));

        // Description
        if (!task.getDescription().isEmpty()) {
            String desc = task.getDescription();
            if (desc.length() > 70) desc = desc.substring(0, 67) + "...";
            JLabel descLabel = new JLabel(desc);
            descLabel.setFont(new Font("Segue UI", Font.ITALIC, 11));
            descLabel.setForeground(SUBTLE);
            centre.add(descLabel);
            centre.add(Box.createVerticalStrut(4));
        }

        // Due date + status
        JPanel dateRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        dateRow.setOpaque(false);

        JLabel dueLabel = new JLabel("Due: " + task.getSubmissionDate().format(Task.DISPLAY_FORMAT));
        dueLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        dueLabel.setForeground(SUBTLE);

        JLabel statusLabel = new JLabel(task.getStatus()); //jlabel will print out the message if completed
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        statusLabel.setForeground(Status_Color());

        dateRow.add(dueLabel);
        dateRow.add(statusLabel);
        centre.add(dateRow);

        add(centre, BorderLayout.CENTER);

        // ── Right: delete button ──────────────────────────────────────────────
        JButton deleteBtn = new JButton("x");
        deleteBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        deleteBtn.setForeground(SUBTLE);
        deleteBtn.setOpaque(false);
        deleteBtn.setContentAreaFilled(false);
        deleteBtn.setBorderPainted(false);
        deleteBtn.setFocusPainted(false);
        deleteBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        deleteBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                deleteBtn.setForeground(DANGER);
            }
            public void mouseExited(MouseEvent e)  {
                deleteBtn.setForeground(SUBTLE);
            }
        });
        deleteBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { onDelete.run(); }
        });

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        rightPanel.setOpaque(false);
        rightPanel.add(deleteBtn);
        add(rightPanel, BorderLayout.EAST);
    }

    //color depends on the function
    private Color Card_Color() {
        if (task.getCompleted())
            return Blue_Darker;
        if (task.isOverdue())
            return Overdue_Color;
        if (task.isDueSoon(1))
            return BG_SOON;
        else
            return Blue_Dark;
    }

    private Color Border_Color() {
        if (task.getCompleted())
            return new Color(0x333348);
        if (task.isOverdue())
            return DANGER.darker();
        if (task.isDueSoon(1))
            return new Color(0x4A6A10);
        else
            return new Color(0x2D2D4A);
    }

    private Color Status_Color() {
        if (task.getCompleted())  return SUCCESS;
        if (task.isOverdue())    return DANGER;
        if (task.isDueSoon(1))   return WARNING;
        return SUCCESS;
    }

    private Color Difficulty_Color() {
        if (task.getDifficulty().equals("Easy"))   return SUCCESS;
        if (task.getDifficulty().equals("Hard"))   return DANGER;
        return WARNING; // Medium
    }

    private JLabel makeBadge(String text, Color color) {
        JLabel lb = new JLabel("  " + text + "  ");
        lb.setFont(new Font("Segue UI", Font.BOLD, 10));
        lb.setForeground(color);
        lb.setBackground(color.darker().darker());
        lb.setOpaque(true);
        lb.setBorder(new LineBorder(color.darker(), 1, true));
        return lb;
    }
}
