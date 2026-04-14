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


    private static final Color BG      = new Color(0x0F0F1A);
    private static final Color CARD    = new Color(0x1A1A2E);
    private static final Color ACCENT  = new Color(0x7C3AED);
    private static final Color ACCENT2 = new Color(0xE040FB);
    private static final Color TEXT    = new Color(0xF0F0FF);
    private static final Color SUBTLE  = new Color(0x8888AA);

    private TaskManager  manager;
    private StatsPanel   statsPanel;
    private AlertService alertService;

    // The panel that holds all the task cards
    private JPanel taskListPanel;

    // Which filter is currently active
    private String activeFilter = "All";

    // Clock labels in the header
    private JLabel clockLabel;
    private JLabel dateLabel;

    // ── Constructor ───────────────────────────────────────────────────────────
    public Dashboard(TaskManager manager) {
        super("Task Manager");
        this.manager      = manager;
        this.statsPanel   = new StatsPanel();
        this.alertService = new AlertService(this, manager);

        buildUI();
        refresh();       // show existing tasks
        startClock();    // start the live clock
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

        // Show alerts 800ms after window opens
        Timer alertTimer = new Timer(true);
        alertTimer.schedule(new TimerTask() {
            public void run() {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        alertService.checkAndAlert();
                    }
                });
            }
        }, 800);
    }


    private void buildUI() {
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout());

        add(buildHeader(),  BorderLayout.NORTH);
        add(buildBody(),    BorderLayout.CENTER);
    }

    //builds header
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(CARD);
        header.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 1, 0, ACCENT),
            new EmptyBorder(12, 22, 12, 22)));

        // App name on the left
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        JLabel appName = new JLabel("Task Manager");
        appName.setFont(new Font("Segue UI", Font.BOLD, 22));
        appName.setForeground(TEXT);
        /*JLabel subtitle = new JLabel("  Academic Task Manager");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(SUBTLE);
        */
        left.add(appName);
        //left.add(subtitle);
        header.add(left, BorderLayout.WEST);

        // Live clock on the right
        JPanel clockPanel = new JPanel();
        clockPanel.setLayout(new BoxLayout(clockPanel, BoxLayout.Y_AXIS));
        clockPanel.setOpaque(false);

        clockLabel = new JLabel("--:--:--", SwingConstants.RIGHT);
        clockLabel.setFont(new Font("Segue UI", Font.BOLD, 18));
        clockLabel.setForeground(ACCENT2);
        clockLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        dateLabel = new JLabel("---", SwingConstants.RIGHT);
        dateLabel.setFont(new Font("Segue UI", Font.PLAIN, 11));
        dateLabel.setForeground(SUBTLE);
        dateLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        clockPanel.add(clockLabel);
        clockPanel.add(dateLabel);
        header.add(clockPanel, BorderLayout.EAST);

        return header;
    }

    //adds scroll filter and sets border the lower panel set
    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setBackground(BG);
        body.setBorder(new EmptyBorder(14, 18, 14, 18));

        body.add(statsPanel, BorderLayout.NORTH);

        JPanel centre = new JPanel(new BorderLayout(0, 10));
        centre.setBackground(BG);
        centre.add(buildFilterBar(), BorderLayout.NORTH);
        centre.add(buildTaskScroll(), BorderLayout.CENTER);
        body.add(centre, BorderLayout.CENTER);

        body.add(buildAddButton(), BorderLayout.SOUTH);
        return body;
    }

    //filter between assignment, project, quiz and etc
    private JPanel buildFilterBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        bar.setBackground(BG);

        String[] filters = {"All", "Assignment", "Quiz", "Project", "Lab Report", "Completed", "Overdue"};
        ButtonGroup group = new ButtonGroup();

        for (final String filter : filters) {
            JToggleButton btn = new JToggleButton(filter);
            btn.setFont(new Font("Segue UI", Font.BOLD, 11));
            btn.setForeground(TEXT);
            btn.setBackground(CARD);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setBorder(new CompoundBorder(
                    new LineBorder(SUBTLE.darker(), 1, true),
                    new EmptyBorder(5, 12, 5, 12)));
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            if (filter.equals("All")) btn.setSelected(true);

            btn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    activeFilter = filter;
                    refresh();
                }
            });

            group.add(btn);
            bar.add(btn);
        }

        return bar;
    }

    // ── Scrollable task list ──────────────────────────────────────────────────
    private JScrollPane buildTaskScroll() {
        taskListPanel = new JPanel();
        taskListPanel.setLayout(new BoxLayout(taskListPanel, BoxLayout.Y_AXIS));
        taskListPanel.setBackground(BG);

        JScrollPane scroll = new JScrollPane(taskListPanel);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    // ── Add Task button at the bottom ─────────────────────────────────────────
    private JPanel buildAddButton() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        bar.setBackground(BG);

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
        addBtn.setForeground(TEXT);
        addBtn.setBackground(ACCENT);
        addBtn.setFocusPainted(false);
        addBtn.setContentAreaFilled(false);
        addBtn.setBorderPainted(false);
        addBtn.setOpaque(false);
        addBtn.setPreferredSize(new Dimension(55, 55));
        addBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openAddDialog();
            }
        });

        addBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { addBtn.setBackground(ACCENT2); }
            public void mouseExited(MouseEvent e)  { addBtn.setBackground(ACCENT); }
        });

        bar.add(addBtn);
        return bar;
    }

    //refresh deletes everything in panel and loads array list based on filter
    public void refresh() {
        taskListPanel.removeAll();
        ArrayList<Task> toShow;  //creates arraylist

        if (activeFilter.equals("All")) {
            toShow = manager.getAllTasks();
        }
        else if (activeFilter.equals("Completed")) {
            toShow = manager.filterCompleted();
        }
        else if (activeFilter.equals("Overdue")) {
            toShow = manager.filterOverdue();
        }
        else {
            toShow = manager.filterByType(activeFilter);
        }
        //filters by type and creates arraylist of type

        // Show a message if the list is empty
        if (toShow.isEmpty()) {
            JLabel empty = new JLabel("No tasks added yet", SwingConstants.CENTER);
            empty.setFont(new Font("Segue UI", Font.ITALIC, 14));
            empty.setForeground(SUBTLE);
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            taskListPanel.add(Box.createVerticalGlue());
            taskListPanel.add(empty); //empty is the label added to the panel that holds text
            taskListPanel.add(Box.createVerticalGlue());

        } else {
            // Add one TaskCard for each task
            for (final Task t : toShow) {
                // Runnable is like a simple action it tells the card what to do when toggled/deleted
                Runnable onToggle = new Runnable() { //operation does not return result
                    public void run() {
                        manager.toggleCompletion(t.getId());
                        refresh();
                    }
                };

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

                TaskCard card = new TaskCard(t, onToggle, onDelete);
                card.setAlignmentX(Component.LEFT_ALIGNMENT);
                taskListPanel.add(card);
                taskListPanel.add(Box.createRigidArea(new Dimension(0, 8)));
            }
        }

        // Update the stats panel
        statsPanel.update(
            manager.totalTasks(),
            manager.countCompleted(),
            manager.countPending(),
            manager.countOverdue()
        );

        taskListPanel.revalidate();
        taskListPanel.repaint();
    }

    // ── Open the Add Task dialog ──────────────────────────────────────────────
    private void openAddDialog() {
        AddTaskDialog dialog = new AddTaskDialog(this, manager);
        dialog.setVisible(true);

        Task newTask = dialog.getResult();
        if (newTask != null) {
            manager.addTask(newTask);
            refresh();
        }
    }

    // ── Live clock: updates every second ─────────────────────────────────────
    private void startClock() {
        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("hh:mm:ss a");
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy");

        Timer timer = new Timer(true); // daemon timer — stops when app closes
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                // Always update Swing components on the Event Dispatch Thread
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        LocalDateTime now = LocalDateTime.now();
                        clockLabel.setText(now.format(timeFormat));
                        dateLabel.setText(now.format(dateFormat));
                    }
                });
            }
        }, 0, 1000); // run immediately, then every 1000ms (1 second)
    }
}
