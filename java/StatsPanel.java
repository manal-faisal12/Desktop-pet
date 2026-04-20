import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class StatsPanel extends JPanel {

    private static final Color DarkBlue = new Color(0x1A1A2E);
    private static final Color PurpleishBlue = new Color(0x7C3AED);
    private static final Color Purple = new Color(0xE040FB);
    private static final Color White = new Color(0xF0F0FF);
    private static final Color Grey = new Color(0x8888AA);
    private static final Color Mint = new Color(0x00C897);
    private static final Color Orange = new Color(0xF5A623);
    private static final Color LightRed = new Color(0xFF4D6D);

    // Labels we will update when tasks change
    private JLabel totalL;
    private JLabel doneL;
    private JLabel pendingL;
    private JLabel overdueL;
    private JLabel percentL;
    private JProgressBar progressB;

    public StatsPanel() {
        setLayout(new BorderLayout(0, 8));
        setBackground(DarkBlue);
        setBorder(new CompoundBorder(
            new LineBorder(PurpleishBlue.darker(), 1, true),
            new EmptyBorder(12, 16, 12, 16)));

        // Section title
        JLabel heading = new JLabel("Progress Overview");
        heading.setFont(new Font("Segue UI", Font.BOLD, 14));
        heading.setForeground(Purple);
        add(heading, BorderLayout.NORTH);

        // Four stat boxes side by side
        JPanel boxes = new JPanel(new GridLayout(1, 4, 10, 0));
        boxes.setOpaque(false);
        boxes.setBorder(new EmptyBorder(8, 0, 8, 0));

        totalL = makeStatLabel("0", PurpleishBlue);
        doneL = makeStatLabel("0", Mint);
        pendingL = makeStatLabel("0", Orange);
        overdueL = makeStatLabel("0", LightRed);

        boxes.add(wrapBox("Total",     totalL, PurpleishBlue));
        boxes.add(wrapBox("Completed", doneL, Mint));
        boxes.add(wrapBox("Pending",   pendingL, Orange));
        boxes.add(wrapBox("Overdue",   overdueL, LightRed));
        add(boxes, BorderLayout.CENTER);

        // Progress bar row
        JPanel barRow = new JPanel(new BorderLayout(6, 0));
        barRow.setOpaque(false);

        progressB = new JProgressBar(0, 100);
        progressB.setValue(0);
        progressB.setBackground(new Color(0x2D2D4A));
        progressB.setForeground(Mint);
        progressB.setPreferredSize(new Dimension(0, 12));
        progressB.setBorder(new LineBorder(PurpleishBlue.darker(), 1, true));

        percentL = new JLabel("0%");
        percentL.setFont(new Font("Segue UI", Font.BOLD, 12));
        percentL.setForeground(Mint);
        percentL.setPreferredSize(new Dimension(36, 14));

        barRow.add(progressB, BorderLayout.CENTER);
        barRow.add(percentL, BorderLayout.EAST);
        add(barRow, BorderLayout.SOUTH);
    }

    // Called by Dashboard every time the task list changes
    public void update(int total, int done, int pending, int overdue) {
        totalL.setText(String.valueOf(total));
        doneL.setText(String.valueOf(done));
        pendingL.setText(String.valueOf(pending));
        overdueL.setText(String.valueOf(overdue));

        int percent = 0;
        if (total > 0) {
            percent = (done * 100) / total;
        }
        progressB.setValue(percent);
        percentL.setText(percent + "%");

        if (percent >= 75) {
            progressB.setForeground(Mint);
        } else if (percent >= 40) {
            progressB.setForeground(Orange);
        } else {
            progressB.setForeground(LightRed);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private JLabel makeStatLabel(String value, Color color) {
        JLabel lb = new JLabel(value, SwingConstants.CENTER);
        lb.setFont(new Font("Segue UI", Font.BOLD, 26));
        lb.setForeground(color);
        return lb;
    }

    private JPanel wrapBox(String name, JLabel valueLabel, Color color) {
        JPanel p = new JPanel(new BorderLayout(0, 2));
        p.setBackground(color.darker().darker().darker());
        p.setBorder(new CompoundBorder(
            new LineBorder(color.darker(), 1, true),
            new EmptyBorder(6, 10, 6, 10)));

        JLabel nameLb = new JLabel(name, SwingConstants.CENTER);
        nameLb.setFont(new Font("Segue UI", Font.PLAIN, 10));
        nameLb.setForeground(color.brighter());

        p.add(valueLabel, BorderLayout.CENTER);
        p.add(nameLb,     BorderLayout.SOUTH);
        return p;
    }
}
