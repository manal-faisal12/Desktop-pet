import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;

    public class AddTaskDialog extends JDialog {


        static final Color DarBlue = new Color(0x0F0F1A);
        static final Color Navy = new Color(0x1A1A2E);
        static final Color BlueishPurle = new Color(0x7C3AED);
        static final Color Purple = new Color(0xE040FB);
        static final Color White = new Color(0xF0F0FF);
        static final Color Grey = new Color(0x8888AA);

        //if task not created then null, else created, reference since task is abstract
        private Task result = null;
        private TaskManager manager;

        private JComboBox typeBox;
        private JComboBox difficultyBox; //part of swing, combines dropdown and text field
        private JTextField titleField;
        private JTextField subjectField;
        private JTextField descField;
        //sets time
        private JSpinner yearSpinner;
        private JSpinner monthSpinner;
        private JSpinner daySpinner;
        private JSpinner hourSpinner;
        private JSpinner minuteSpinner;

        // handles extra per task
        private JPanel extraPanel;
        // for assignment
        private JTextField marksF, formatF;
        // for quiz
        private JTextField durationF, quizType;
        // for project
        private JTextField teamSize, presentation;
        // for lab report
        private JTextField labNum, experiment;

        //for add new task, constructor
        public AddTaskDialog(Frame owner, TaskManager manager) {
            super(owner, "Add New Task", true);
            //model makes sure that can go nowhere without entering data

            this.manager = manager; //manager is a class ref object
            addTaskBox(); //method creates panel for input data
            setSize(560, 660);
            setLocationRelativeTo(owner);
            setResizable(false);
        }

        private void addTaskBox() {
            getContentPane().setBackground(DarBlue);
            setLayout(new BorderLayout());
            JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 12));
            header.setBackground(Navy);
            header.setBorder(new MatteBorder(0, 0, 1, 0, BlueishPurle));
            JLabel title = new JLabel(" Add New Task");
            title.setFont(new Font("Segue UI", Font.BOLD, 18));
            title.setForeground(White);
            header.add(title);
            add(header, BorderLayout.NORTH);
            JPanel form = new JPanel();
            form.setBackground(DarBlue);
            form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
            form.setBorder(new EmptyBorder(16, 22, 12, 22));
            JPanel row1 = rowPanel();

            typeBox = makeCombo(new String[]{"Assignment", "Quiz", "Project", "Lab Report"});
            difficultyBox = makeCombo(new String[]{"Easy", "Medium", "Hard"});

            //panel named row1 adds task type
            row1.add(labeledField("Task Type", typeBox, 200));
            row1.add(Box.createHorizontalStrut(12));

            row1.add(labeledField("Difficulty", difficultyBox, 160));
            form.add(row1);

            form.add(Box.createVerticalStrut(10));
            titleField = makeTextField();
            form.add(labeledField("Task Title", titleField, 460));
            form.add(Box.createVerticalStrut(10));
            subjectField = makeTextField();
            form.add(labeledField("Subject", subjectField, 460));
            form.add(Box.createVerticalStrut(10));
            descField = makeTextField();
            form.add(labeledField("Description (optional)", descField, 460));
            form.add(Box.createVerticalStrut(14));
            JLabel dateLabel = new JLabel(" Submission Date & Time");
            dateLabel.setFont(new Font("Segue UI", Font.BOLD, 13));
            dateLabel.setForeground(Purple);
            dateLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            form.add(dateLabel);
            form.add(Box.createVerticalStrut(6));
            form.add(buildDateRow());
            form.add(Box.createVerticalStrut(14));

            // Extra fields section label
            JLabel extraLabel = new JLabel(" Task Details");
            extraLabel.setFont(new Font("Segue UI", Font.BOLD, 13));
            extraLabel.setForeground(Purple);
            extraLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            form.add(extraLabel);
            form.add(Box.createVerticalStrut(6));

            // Extra fields panel (swapped when type changes)
            extraPanel = new JPanel();
            extraPanel.setBackground(DarBlue);
            extraPanel.setLayout(new BoxLayout(extraPanel, BoxLayout.Y_AXIS));
            extraPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            buildExtraFields("Assignment"); // show Assignment fields by default
            form.add(extraPanel);

            // When the type dropdown changes, rebuild the extra fields
            typeBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String selected = (String) typeBox.getSelectedItem();
                    extraPanel.removeAll(); //removes everything in panel and rewrites the selected one again
                    buildExtraFields(selected);
                    extraPanel.revalidate();//updates the scroll bar
                    extraPanel.repaint();
                }
            });

            JScrollPane scroll = new JScrollPane(form);
            scroll.setBorder(null);
            scroll.getViewport().setBackground(DarBlue);
            scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            add(scroll, BorderLayout.CENTER);

            JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
            footer.setBackground(Navy);
            footer.setBorder(new MatteBorder(1, 0, 0, 0, BlueishPurle));

            JButton cancelBtn = makeButton("x Cancel", Grey);
            JButton addBtn    = makeButton("+ Add Task  ", BlueishPurle);

            cancelBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) { dispose(); }
            });

            addBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) { onAdd(); }
            });

            footer.add(cancelBtn);
            footer.add(addBtn);
            add(footer, BorderLayout.SOUTH);
        }

        private JPanel buildDateRow() {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            p.setBackground(DarBlue);
            p.setAlignmentX(Component.LEFT_ALIGNMENT);

            LocalDateTime now = LocalDateTime.now().plusDays(3);

            yearSpinner   = makeSpinner(now.getYear(), 2025, 2035);//includes min
            monthSpinner  = makeSpinner(now.getMonthValue(), 1, 12);
            daySpinner    = makeSpinner(now.getDayOfMonth(), 1, 31);
            hourSpinner   = makeSpinner(now.getHour(), 0, 23); //24 hr clock
            minuteSpinner = makeSpinner(now.getMinute(), 0, 59);

            p.add(smallLabel("Year"));   p.add(yearSpinner);
            p.add(Box.createHorizontalStrut(4));
            p.add(smallLabel("Month"));  p.add(monthSpinner);
            p.add(Box.createHorizontalStrut(4));
            p.add(smallLabel("Day"));    p.add(daySpinner);
            p.add(Box.createHorizontalStrut(10));
            p.add(smallLabel("Hour"));   p.add(hourSpinner);
            p.add(smallLabel(":"));      p.add(minuteSpinner);

            return p;
        }

        private void buildExtraFields(String type) {
            switch (type) {
                case "Assignment": {
                    marksF = makeTextField();
                    formatF = makeTextField();
                    marksF.setText("100");
                    formatF.setText("PDF/ Handwritten");
                    JPanel r = rowPanel();
                    r.add(labeledField("Total Marks", marksF, 200));
                    r.add(Box.createHorizontalStrut(12));
                    r.add(labeledField("Submission Format", formatF, 200));
                    extraPanel.add(r);
                    break;
                }
                case "Quiz": {
                    durationF = makeTextField();
                    quizType = makeTextField();
                    durationF.setText("30");
                    quizType.setText("MCQ/theory/lab");
                    JPanel r = rowPanel();
                    r.add(labeledField("Duration in minutes", durationF, 200));
                    r.add(Box.createHorizontalStrut(12));
                    r.add(labeledField("Quiz Type", quizType, 200));
                    extraPanel.add(r);
                    break;
                }
                case "Project": {
                    teamSize = makeTextField();
                    presentation = makeTextField();
                    teamSize.setText("1");
                    presentation.setText("No");
                    JPanel r = rowPanel();
                    r.add(labeledField("Team Size", teamSize, 200));
                    r.add(Box.createHorizontalStrut(12));
                    r.add(labeledField("Presentation Required?", presentation, 200));
                    extraPanel.add(r);
                    break;
                }
                case "Lab Report": {
                    labNum = makeTextField();
                    experiment = makeTextField();
                    labNum.setText("1");
                    JPanel r = rowPanel();
                    r.add(labeledField("Lab Number:", labNum, 200));
                    r.add(Box.createHorizontalStrut(12));
                    r.add(labeledField("Experiment no:", experiment, 200));
                    extraPanel.add(r);
                    break;
                }
            }
        }

        private void onAdd() {
            String title   = titleField.getText(); //text component so gets the text there
            String subject = subjectField.getText();
            String desc    = descField.getText();
            String type    = (String) typeBox.getSelectedItem();
            String diff    = (String) difficultyBox.getSelectedItem();

            //if title or subject is empty
            if (title.isEmpty() || subject.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in Title and Subject.",
                        "Missing Fields",JOptionPane.WARNING_MESSAGE);
                return;
            }


            // Reads date for time
            int year = (int) yearSpinner.getValue();
            int month = (int) monthSpinner.getValue();
            int day = (int) daySpinner.getValue();
            int hour = (int) hourSpinner.getValue();
            int minute = (int) minuteSpinner.getValue();

            LocalDateTime dueDate;
            try {
                dueDate = LocalDateTime.of(year, month, day, hour, minute);
                if (dueDate.isBefore(LocalDateTime.now())) {
                    JOptionPane.showMessageDialog(this,
                            "Due date cannot be in the past!", "Invalid Date",JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Invalid date. Please check the date fields.", "Date Error",JOptionPane.WARNING_MESSAGE);
                return;
            }

            String id = manager.generateId(); //generates unique id to help in finding and deleting

            // Task is null
            try {
                if (type.equals("Assignment")) {
                    int marks = Integer.parseInt(marksF.getText().trim()); //marks is
                    String fmt = formatF.getText().trim();
                    result = new Assignment(id, title, subject, desc, diff, dueDate, marks, fmt);
                    //creates a new task, so pointer no longer points to null

                } else if (type.equals("Quiz")) {
                    int duration = Integer.parseInt(durationF.getText().trim());
                    String qtype = quizType.getText().trim();
                    result = new Quiz(id, title, subject, desc, diff, dueDate, duration, qtype);

                } else if (type.equals("Project")) {
                    int teamSize = Integer.parseInt(this.teamSize.getText().trim());
                    String pres  = presentation.getText().trim();
                    result = new Project(id, title, subject, desc, diff, dueDate, teamSize, pres);

                } else if (type.equals("Lab Report")) {
                    String labNum = this.labNum.getText().trim();
                    String exp    = experiment.getText().trim();
                    result = new LabReport(id, title, subject, desc, diff, dueDate, labNum, exp);
                }

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this,
                        "Please enter a valid number in the numeric fields.", "Number Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 2. TELL THE ALERT SERVICE TO RE-CHECK EVERYTHING RIGHT NOW
            // This updates 'latestTaskCount' so the Fox hears it
            new AlertService((JFrame)getOwner(), manager).checkAndAlert(); //added by minahil
            dispose(); // close the dialog
        }

        // Returns the task that was created (or null if canceled)
        public Task getResult() {
            return result; //task object that checks if null
        }

        //building ui Components helper methods

        private JPanel rowPanel() {
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
            p.setBackground(DarBlue);
            p.setAlignmentX(Component.LEFT_ALIGNMENT);
            return p;
        }

        private JPanel labeledField(String label, JComponent field, int width) {
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            p.setBackground(DarBlue);

            JLabel lb = new JLabel(label);
            lb.setFont(new Font("Segue UI", Font.BOLD, 10));
            lb.setForeground(Grey);
            lb.setAlignmentX(Component.LEFT_ALIGNMENT);

            field.setAlignmentX(Component.LEFT_ALIGNMENT);
            field.setMaximumSize(new Dimension(width, 32));

            p.add(lb);
            p.add(Box.createVerticalStrut(3));
            p.add(field);
            return p;
        }

        private JTextField makeTextField() {
            JTextField tf = new JTextField();
            tf.setBackground(Navy);
            tf.setForeground(White);
            tf.setCaretColor(White);
            tf.setFont(new Font("Segue UI", Font.PLAIN, 12));
            tf.setBorder(new CompoundBorder(
                    new LineBorder(BlueishPurle.darker(), 1, true),
                    new EmptyBorder(4, 8, 4, 8)));
            return tf;
        }

        private JComboBox makeCombo(String[] options) {
            JComboBox cb = new JComboBox(options);
            cb.setBackground(Navy);
            cb.setForeground(White);
            cb.setFont(new Font("Segue UI", Font.PLAIN, 12));
            return cb;
        }

        private JSpinner makeSpinner(int value, int min, int max) {
            JSpinner sp = new JSpinner(new SpinnerNumberModel(value, min, max, 1));
            sp.setPreferredSize(new Dimension(64, 30));
            sp.setBackground(Navy);
            sp.setFont(new Font("Segue UI", Font.PLAIN, 12));
            // Fix text visibility inside the spinner
            JComponent editor = sp.getEditor();
            if (editor instanceof JSpinner.DefaultEditor) {
                JSpinner.DefaultEditor de = (JSpinner.DefaultEditor) editor;
                de.getTextField().setBackground(Navy);
                de.getTextField().setForeground(White);
                de.getTextField().setFont(new Font("Segue UI", Font.PLAIN, 12));
                de.getTextField().setCaretColor(White);
                de.getTextField().setOpaque(true);
                de.getTextField().setUI(new javax.swing.plaf.basic.BasicTextFieldUI());
                de.getTextField().setBackground(Navy);
                de.getTextField().setForeground(White);
            }
            sp.setBorder(new LineBorder(BlueishPurle.darker(), 1, true));
            return sp;
        }

        private JButton makeButton(String text, Color bg) {
            JButton b = new JButton(text);
            b.setFont(new Font("Segue UI", Font.BOLD, 12));
            b.setBackground(bg);
            b.setForeground(White);
            b.setFocusPainted(false);
            b.setOpaque(true);
            b.setBorder(new EmptyBorder(7, 16, 7, 16));
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return b;
        }

        private JLabel smallLabel(String text) {
            JLabel l = new JLabel(text);
            l.setFont(new Font("Segue UI", Font.PLAIN, 10));
            l.setForeground(Grey);
            return l;
        }
    }
