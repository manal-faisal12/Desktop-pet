import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.net.URL;
public class Dashboard extends JFrame {

    private TaskManager  manager;
    private StatsPanel   statsPanel;
    private AlertService alertService;
    private JPanel TaskListBox;        //panel holds list of all task cards
    private String ActiveFilter = "All";        //which filter is on
    private JLabel clockLabel;
    private JLabel dateLabel;
    //clock labels in the header

    private static final Color DarkBlue = new Color(0x0F0F1A);
    private static final Color Navy = new Color(0x1A1A2E);
    private static final Color PurpleBlue = new Color(0x7C3AED);
    private static final Color Purple = new Color(0xE040FB);
    private static final Color White = new Color(0xF0F0FF);
    private static final Color GreyPurple = new Color(0x8888AA);

    public Dashboard(TaskManager manager) {
        super("Task Manager"); //gives title to the JFrame
        this.manager      = manager;
        this.statsPanel   = new StatsPanel();
        this.alertService = new AlertService(this, manager);

        buildUI();
        refresh();       //refreshes to show the updated tasks
        startClock();    //start the live clock
        URL iconUrl = getClass().getResource("/Main_taskManager/resources/icon_fox.png"); // path to image
        if (iconUrl != null) {
            ImageIcon icon = new ImageIcon(iconUrl);
            setIconImage(icon.getImage());
        } else {
            System.out.println("Could not find the icon file!");
        }

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(880, 660);
        setMinimumSize(new Dimension(750, 500));
        setLocationRelativeTo(null);
        setVisible(true);

        //UI updates must happen on Event Dispatch Thread
        //shows alert
        Timer alertTimer = new Timer(true);//won't keep the program alive if everything else finishes
        alertTimer.schedule(new TimerTask() {
            public void run() {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        alertService.checkAndAlert(); //after 800ms runs this command
                    }
                });
            }
        }, 800);
    }


    //builds UI
    private void buildUI() {
        getContentPane().setBackground(DarkBlue);
        setLayout(new BorderLayout());
        add(buildHeader(),  BorderLayout.NORTH);
        add(buildBody(),    BorderLayout.CENTER);
    }

    //builds header
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Navy);
        header.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 1, 0, PurpleBlue),
            new EmptyBorder(12, 22, 12, 22)));

        // App name on the left
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        JLabel appName = new JLabel("Task Manager");
        appName.setFont(new Font("Segue UI", Font.BOLD, 22));
        appName.setForeground(White);
        left.add(appName);
        header.add(left, BorderLayout.WEST);

        //Creates ClockPanel
        JPanel clockPanel = new JPanel();
        clockPanel.setLayout(new BoxLayout(clockPanel, BoxLayout.Y_AXIS));
        clockPanel.setOpaque(false);

        clockLabel = new JLabel("--:--:--", SwingConstants.RIGHT);
        clockLabel.setFont(new Font("Segue UI", Font.BOLD, 18));
        clockLabel.setForeground(Purple);
        clockLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        dateLabel = new JLabel("---", SwingConstants.RIGHT);
        dateLabel.setFont(new Font("Segue UI", Font.PLAIN, 11));
        dateLabel.setForeground(GreyPurple);
        dateLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        clockPanel.add(clockLabel);
        clockPanel.add(dateLabel);
        header.add(clockPanel, BorderLayout.EAST);

        return header;
    }

    //adds scroll filter and sets border the lower panel set
    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setBackground(DarkBlue);
        body.setBorder(new EmptyBorder(14, 18, 14, 18));

        body.add(statsPanel, BorderLayout.NORTH);

        JPanel centre = new JPanel(new BorderLayout(0, 10));
        centre.setBackground(DarkBlue);
        centre.add(buildFilterBar(), BorderLayout.NORTH);
        centre.add(buildTaskScroll(), BorderLayout.CENTER);
        body.add(centre, BorderLayout.CENTER);

        body.add(buildAddButton(), BorderLayout.SOUTH);
        return body;
    }

    //filter between assignment, project, quiz and etc
    private JPanel buildFilterBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        bar.setBackground(DarkBlue);

        String[] filters = {"All", "Assignment", "Quiz", "Project", "Lab Report", "Completed", "Overdue"};
        ButtonGroup group = new ButtonGroup();

        for (final String filter : filters) {
            JToggleButton btn = new JToggleButton(filter);
            btn.setFont(new Font("Segue UI", Font.BOLD, 11));
            btn.setForeground(White);
            btn.setBackground(Navy);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setBorder(new CompoundBorder(
                    new LineBorder(GreyPurple.darker(), 1, true),
                    new EmptyBorder(5, 12, 5, 12)));
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            if (filter.equals("All")) btn.setSelected(true);

            btn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    ActiveFilter = filter;
                    refresh();
                }
            });

            group.add(btn);
            bar.add(btn);
        }

        return bar;
    }

    //TaskList on a Scrolling Panel
    private JScrollPane buildTaskScroll() {
        TaskListBox = new JPanel(); //attribute of class
        TaskListBox.setLayout(new BoxLayout(TaskListBox, BoxLayout.Y_AXIS));
        TaskListBox.setBackground(DarkBlue);

        JScrollPane scroll = new JScrollPane(TaskListBox);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(DarkBlue);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    // Creates the add button which triggers prompt screen
    private JPanel buildAddButton() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        bar.setBackground(DarkBlue);

        JButton addBtn = new JButton("+") {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillOval(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
            }
        };

        addBtn.setFont(new Font("Segue UI", Font.BOLD, 34));
        addBtn.setForeground(White);
        addBtn.setBackground(PurpleBlue);
        addBtn.setFocusPainted(false);
        addBtn.setContentAreaFilled(false);
        addBtn.setBorderPainted(false);
        addBtn.setOpaque(false);
        addBtn.setPreferredSize(new Dimension(55, 55));
        addBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                OpenAddTask();
            }
        });

        addBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { addBtn.setBackground(Purple); }
            public void mouseExited(MouseEvent e)  { addBtn.setBackground(PurpleBlue); }
        });

        bar.add(addBtn);
        return bar;
    }

    //refresh deletes everything in panel and loads array list based on filter
    public void refresh() {
        TaskListBox.removeAll();//part of awt that removes all components from container
        ArrayList<Task> New_Array;  //creates arraylist

        if (ActiveFilter.equals("All")) {
            New_Array = manager.getTasks();
        }
        else if (ActiveFilter.equals("Completed")) {
            New_Array = manager.filterCompleted();
        }
        else if (ActiveFilter.equals("Overdue")) {
            New_Array = manager.filterOverdue();
        }
        else {
            New_Array = manager.filterByType(ActiveFilter);
        }
        //filters by type and creates arraylist of type

        //if list is empty display msg, sets labels look and feel
        if (New_Array.isEmpty()) {
            JLabel empty = new JLabel("No tasks added yet", SwingConstants.CENTER);
            empty.setFont(new Font("Segue UI", Font.ITALIC, 14));
            empty.setForeground(GreyPurple);
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            TaskListBox.add(Box.createVerticalGlue());
            TaskListBox.add(empty); //empty is the label added to the panel that holds text
            TaskListBox.add(Box.createVerticalGlue());

        } else {
            // Add one TaskCard for each task
            for (final Task t : New_Array) {
                //everytime onClick is called run this instruction
                // Runnable is like a simple action it tells the card what to do when clicked/deleted

                Runnable onClick = new Runnable() {
                    //operation does not return result
                    public void run() {//part of the runnable that allows multiple threads to work at the same time
                        manager.Completed_Flip(t.getId());
                        refresh(); //refreshes the dashboard
                    }
                };
                //used in taskcard creation in Dashboard

                Runnable onDelete = new Runnable() {
                    public void run() {
                        int choice = JOptionPane.showConfirmDialog(
                                Dashboard.this,
                                "Delete \"" + t.getTitle() + "\"?",
                                "Confirm Delete",
                                JOptionPane.YES_NO_OPTION);
                        if (choice == JOptionPane.YES_OPTION) {
                            manager.removeTask(t.getId());
                            refresh();
                        }
                    }
                };

                TaskCard card = new TaskCard(t, onClick, onDelete); //runnable used
                card.setAlignmentX(Component.LEFT_ALIGNMENT);
                TaskListBox.add(card);
                TaskListBox.add(Box.createRigidArea(new Dimension(0, 8)));
            }
        }

        // Update the stats panel
        statsPanel.update(
            manager.totalTasks(),
            manager.Completed_Number(),
            manager.countPending(),
            manager.countOverdue()
        );

        TaskListBox.revalidate();
        TaskListBox.repaint();
    }

    //opens add task pop up
    private void OpenAddTask() {
        AddTaskDialog dialog = new AddTaskDialog(this, manager);
        dialog.setVisible(true);

        Task newTask = dialog.getResult(); // if exits the panel and doesnot change the null of abstract class
        if (newTask != null) {
            manager.addTask(newTask);
            refresh();
        }
    }

    //creates and starts clock
    private void startClock() {
        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("hh:mm:ss a");
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy");

        Timer timer = new Timer(true); //stops program if everything else finishes
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        LocalDateTime now = LocalDateTime.now();
                        clockLabel.setText(now.format(timeFormat));
                        dateLabel.setText(now.format(dateFormat));
                    }
                });
            }
        }, 0, 1000); //run immediately and then every 1000ms/1s
    }
}
