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

    private final Color Blue_Dark  = new Color(0x1A1A2E);
    private final Color Blue_Darker = new Color(0x141420);
    private final Color Mauve = new Color(0x2A1420);
    private final Color Dark_Green = new Color(0x1A240A);
    private final Color White = new Color(0xF0F0FF);
    private final Color Lilac = new Color(0x8888AA);
    private final Color Blueish_Purple = new Color(0x7C3AED);
    private final Color Mint = new Color(0x00C897);
    private final Color Orange = new Color(0xF5A623);
    private final Color Pink = new Color(0xFF4D6D);
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
        JPanel Title_Row = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        Title_Row.setOpaque(false);

        JLabel Title_Label = new JLabel(task.getTitle());
        Title_Label.setFont(new Font("Segue UI", Font.BOLD, 14));
        if (task.getCompleted()) {
            // Show strikethrough if completed
            Title_Label.setText("<html><s>" + task.getTitle() + "</s></html>");
            Title_Label.setForeground(Lilac);
        } else {
            Title_Label.setForeground(White);
        }

        JLabel Type_Badge = makeBadge(task.getTaskType(), Blueish_Purple); //darkens the color
        JLabel Diff_Badge = makeBadge(task.getDifficulty(), Difficulty_Color());

        Title_Row.add(Title_Label);
        Title_Row.add(Type_Badge);
        Title_Row.add(Diff_Badge);

        centre.add(Title_Row); //adds the title row to the central panel

        // Subject + extra info
        JLabel subjectLabel = new JLabel(task.getSubject() + "  ·  " + task.getExtraInfo());
        subjectLabel.setFont(new Font("Segue UI", Font.PLAIN, 11));
        subjectLabel.setForeground(Lilac);
        centre.add(subjectLabel);
        centre.add(Box.createVerticalStrut(4));

        //if description is there then get it
        if (!task.getDescription().isEmpty()) {
            String description = task.getDescription();
            if (description.length() > 70)
                description = description.substring(0, 67) + "..."; //only get the first 70 characters
            //replace the rest with ... so total length will be 70+3
            JLabel Desc_Label = new JLabel(description);
            Desc_Label.setFont(new Font("Segue UI", Font.ITALIC, 11));
            Desc_Label.setForeground(Lilac);//subtle is the color purple
            centre.add(Desc_Label);
            centre.add(Box.createVerticalStrut(4));//forces certain spaces (4) between 2 components
        }

        //Creates the panel for date with status and due date
        JPanel dateRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        dateRow.setOpaque(false);

        JLabel dueLabel = new JLabel("Due: " + task.getSubmissionDate().format(Task.DISPLAY_FORMAT));
        dueLabel.setFont(new Font("Segue UI", Font.PLAIN, 11));
        dueLabel.setForeground(Lilac);

        JLabel statusLabel = new JLabel(task.getStatus()); //jlabel will print out the message if completed
        statusLabel.setFont(new Font("Segue UI", Font.BOLD, 11));
        statusLabel.setForeground(Status_Color());

        dateRow.add(dueLabel);
        dateRow.add(statusLabel);
        centre.add(dateRow);

        add(centre, BorderLayout.CENTER);

        //delete button
        JButton deleteBtn = new JButton("X");
        deleteBtn.setFont(new Font("Segue UI", Font.BOLD, 12));
        deleteBtn.setForeground(Lilac);
        deleteBtn.setOpaque(false);
        deleteBtn.setContentAreaFilled(false);
        deleteBtn.setBorderPainted(false);
        deleteBtn.setFocusPainted(false);
        deleteBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        deleteBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                deleteBtn.setForeground(Pink);
            }
            @Override
            public void mouseExited(MouseEvent e)  {
                deleteBtn.setForeground(Lilac);
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
            return Mauve;
        if (task.isDueSoon(1))
            return Dark_Green;
        else
            return Blue_Dark;
    }

    private Color Border_Color() {
        if (task.getCompleted())
            return new Color(0x333348);
        if (task.isOverdue())
            return Pink.darker();
        if (task.isDueSoon(1))
            return new Color(0x4A6A10);
        else
            return new Color(0x2D2D4A);
    }

    private Color Status_Color() {
        if (task.getCompleted())
            return Mint;
        if (task.isOverdue())
            return Pink;
        if (task.isDueSoon(1))
            return Orange;
        return Mint;
    }

    private Color Difficulty_Color() {
        if (task.getDifficulty().equals("Easy"))
            return Mint;
        if (task.getDifficulty().equals("Hard"))
            return Pink;

        return Orange; // Medium
    }

    
    private JLabel makeBadge(String text, Color color) {
        JLabel lab = new JLabel("  " + text + "  ");
        lab.setFont(new Font("Segue UI", Font.BOLD, 10));
        lab.setForeground(color);
        lab.setBackground(color.darker().darker()); //darkens the color twice
        lab.setOpaque(true);
        lab.setBorder(new LineBorder(color.darker(), 1, true));
        return lab;
    }
}
