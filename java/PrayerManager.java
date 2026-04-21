import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalTime;
import java.time.LocalDate; // ADDED IMPORT
import java.time.format.DateTimeFormatter;
import javax.swing.Timer;
import java.util.prefs.Preferences;
import java.util.Properties;
import java.awt.font.TextAttribute;
import java.util.Map;
import java.util.HashMap;

public class PrayerManager {
    private final String[] pNames = {"Fajr", "Dhuhr", "Asr", "Maghrib", "Isha"};
    public static String[] pTimes = new String[5];
    public static JCheckBox[] checkboxes = new JCheckBox[5];
    private int checkedCount = 0;
    private final JFrame frame;
    private final JPanel panel;
    private final JLabel[] times = new JLabel[5];
    private final JLabel[] nameLabel = new JLabel[5]; // ← add this
    private final JLabel status;
    private String lastPlayedTime = "";
    private Font bluewinter;
    private boolean isLoading = false;
    Preferences prefs = Preferences.userNodeForPackage(PrayerManager.class);
    Properties props = new Properties();
    String prayerRecordsFile = "config.properties";

    private static final String LAST_RESET_DATE_KEY = "lastResetDate"; // ADDED

    public PrayerManager() {
        // LOAD FONT
        try {
            InputStream bluewinterIS = getClass().getClassLoader().getResourceAsStream("Resources- PP/bluewinter.ttf");
            bluewinter = Font.createFont(Font.TRUETYPE_FONT, bluewinterIS).deriveFont(20f);

        } catch (Exception ex) {
            bluewinter = new Font("Arial", Font.PLAIN, 20);
        }


        // FRAME
        frame = new JFrame("Prayer Manager");
        frame.setSize(500, 500);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // PANEL
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(245, 235, 255));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // TITLE
        JLabel title = new JLabel(" Prayer Times");
        title.setFont(bluewinter.deriveFont(36f));
        title.setForeground(new Color(100, 60, 140));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Prayer score label
        JLabel scoreLabel = new JLabel("Prayers today: 0 / 5");
        scoreLabel.setFont(bluewinter.deriveFont(16f));
        scoreLabel.setForeground(new Color(100, 60, 140));
        scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);


        // Prayer rows, having name,time and checkbox
        for (int i = 0; i < pNames.length; i++) {
            final int index = i;
            JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
            row.setBackground(new Color(245, 235, 255));
            row.setAlignmentX(Component.CENTER_ALIGNMENT);

            // prayers' names
            nameLabel[i] = new JLabel(pNames[i]);
            nameLabel[i].setFont(bluewinter.deriveFont(22f));
            nameLabel[i].setForeground(new Color(80, 50, 120));
            nameLabel[i].setPreferredSize(new Dimension(100, 30));

            // prayer times for each prayer...
            times[i] = new JLabel("loading...");
            times[i].setFont(bluewinter.deriveFont(22f));
            times[i].setForeground(new Color(140, 90, 180));

            // checkboxes for each prayer...
            checkboxes[i] = new JCheckBox("✓ Prayed");
            checkboxes[i].setFont(bluewinter.deriveFont(14f));
            checkboxes[i].setBackground(new Color(245, 235, 255));
            checkboxes[i].setForeground(new Color(100, 60, 140));
            checkboxes[i].addItemListener(e -> {
                if (isLoading) return; // ignore during loading

                // Create the strikethrough font attributes
                Map<TextAttribute, Object> attributes = new HashMap<>();
                attributes.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
                Font strikeThroughFont = bluewinter.deriveFont(attributes).deriveFont(22f);
                Font regularFont = bluewinter.deriveFont(22f);

                if (e.getStateChange() == ItemEvent.SELECTED) {
                    checkedCount++;
                    // Remove HTML tags, just use the normal text and apply the strikethrough font
                    nameLabel[index].setText(pNames[index]);
                    nameLabel[index].setFont(strikeThroughFont);
                    props.setProperty(String.valueOf(index), "true");
                } else {
                    checkedCount--;
                    // Revert to normal text and normal font
                    nameLabel[index].setText(pNames[index]);
                    nameLabel[index].setFont(regularFont);
                    props.setProperty(String.valueOf(index), "false");
                }
                scoreLabel.setText("Prayers today: " + checkedCount + " / 5");
                PrayerManager.globalPrayedCount = checkedCount;
                saveRecords();
            });
            row.add(nameLabel[i]);
            row.add(times[i]);
            row.add(checkboxes[i]);
            panel.add(row);
        }
        // to check the boxes already checked from earlier in the day

        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(scoreLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        resetIfNewDay(); // ADDED - Check and reset if it's a new day
        checkRecords();
        scoreLabel.setText("Prayers today: " + checkedCount + " / 5");

        // STATUS LABEL
        status = new JLabel("Fetching prayer times...");
        status.setFont(bluewinter.deriveFont(16f));
        status.setForeground(new Color(120, 80, 160));
        status.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(status);

        frame.add(panel);
        frame.setVisible(true);

        fetchpTimes();
        startPrayerChecker();
        startMidnightReset(); //
    }

    public void checkRecords() {
        isLoading = true;
        try (FileInputStream in = new FileInputStream(prayerRecordsFile)) {
            props.load(in);
            for (int i = 0; i < pNames.length; i++) {
                boolean wasPrayed = Boolean.parseBoolean(props.getProperty(String.valueOf(i), "false"));
                checkboxes[i].setSelected(wasPrayed);
                if (wasPrayed) {
                    checkedCount++;

                    Map<TextAttribute, Object> attributes = new HashMap<>();
                    attributes.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);

                    nameLabel[i].setText(pNames[i]);
                    nameLabel[i].setFont(bluewinter.deriveFont(attributes).deriveFont(22f));
                }
            }
        } catch (IOException e) {
            System.out.println("No records file yet.");
        }
        isLoading = false;
        PrayerManager.globalPrayedCount = checkedCount;
    }

    private void saveRecords() {
        try (FileOutputStream out = new FileOutputStream(prayerRecordsFile)) {
            props.store(out, "Prayer Records");
        } catch (IOException e) {
            System.out.println("Could not save records: " + e.getMessage());
        }
    }

    // ADDED METHOD: Reset checklist if the stored date is not today
    private void resetIfNewDay() {
        LocalDate today = LocalDate.now();
        String lastReset = prefs.get(LAST_RESET_DATE_KEY, "");
        if (!lastReset.equals(today.toString())) {
            isLoading = true;
            for (int i = 0; i < pNames.length; i++) {
                checkboxes[i].setSelected(false);
                nameLabel[i].setText(pNames[i]);
                nameLabel[i].setFont(bluewinter.deriveFont(22f));
                props.setProperty(String.valueOf(i), "false");
            }
            checkedCount = 0;
            saveRecords();
            isLoading = false;
            prefs.put(LAST_RESET_DATE_KEY, today.toString());
        }
    }

    private boolean resetDonetoday = false;
    public static int globalPrayedCount = 0;
    // resets all checkboxes at midnight (new day commences)
    private void startMidnightReset() {
        Timer midnightChecker = new Timer(60000, e -> {
            LocalTime now = LocalTime.now();

            // Reset the flag at 00:01 so tomorrow works
            if (now.getHour() == 0 && now.getMinute() == 1) {
                resetDonetoday = false;
            }

            if (now.getHour() == 0 && now.getMinute() == 0 && !resetDonetoday) {
                resetDonetoday = true;
                int missed = 5 - getPrayerCount();
                if (missed > 0 && FoxDesktopPet.currentFox != null) {
                    FoxDesktopPet.currentFox.friendshipManager.decrease(missed * 0.5);
                    FoxDesktopPet.currentFox.speak(new FoxDesktopPet.FoxPrayersMissed(missed));
                    FoxDesktopPet.currentFox.prayerReminderCount = 0;
                }
                resetChecklist();
                prefs.put(LAST_RESET_DATE_KEY, LocalDate.now().toString()); // ADDED
            }
        });
        midnightChecker.start();
    }

    // unchecks everything and resets count
    private void resetChecklist() { // used at midnight
        SwingUtilities.invokeLater(() -> {
            for (int i = 0; i < checkboxes.length; i++) {
                checkboxes[i].setSelected(false);
            }
            for (int i = 0; i < pNames.length; i++) {
                checkboxes[i].setSelected(false);
            }
            checkedCount = 0;
            saveRecords();
        });
    }

    // for the fox to evaluate you
    public int getPrayerCount() {
        return checkedCount; // this count affetcs the friendship bar of fox
    }

    private void fetchpTimes() {
        new Thread(() -> {
            try {
                String city = prefs.get("city", null);
                String country = prefs.get("country", null);
                if (city == null) {
                    city = JOptionPane.showInputDialog(null, "Enter your city for Prayer Times:",
                            "Location Setup", JOptionPane.QUESTION_MESSAGE);
                    country = JOptionPane.showInputDialog(null, "Enter your country:",
                            "Location Setup", JOptionPane.QUESTION_MESSAGE);
                    prefs.put("city", city);
                    prefs.put("country", country);
                }

                URL url = new URL("http://api.aladhan.com/v1/timingsByCity" +
                        "?city=" + city + "&country=" + country + "&method=1&school=1");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                String json = response.toString();
                for (int i = 0; i < pNames.length; i++) {
                    int index = json.indexOf("\"" + pNames[i] + "\":\"")
                            + pNames[i].length() + 4;
                    pTimes[i] = json.substring(index, index + 5);
                }

                SwingUtilities.invokeLater(() -> {
                    for (int i = 0; i < pNames.length; i++) {
                        times[i].setText(pTimes[i]);
                    }
                    status.setText("Times loaded! Next prayer check active ✓");
                });

            } catch (Exception ex) {
                SwingUtilities.invokeLater(() ->
                        status.setText("Failed to load times: " + ex.getMessage()));
            }
        }).start();
    }
    // visual notification alert at specific prayer time
    private void startPrayerChecker() {
        Timer checker = new Timer(10000, e -> {
            String currentTime = LocalTime.now()
                    .format(DateTimeFormatter.ofPattern("HH:mm"));
            for (int i = 0; i < pNames.length; i++) {
                if (pTimes[i] != null && pTimes[i].trim().equals(currentTime) && !currentTime.equals(lastPlayedTime)) {
                    lastPlayedTime = currentTime;
                    final String prayerName = pNames[i];
                    playPrayerAlert();
                    SwingUtilities.invokeLater(() ->
                            JOptionPane.showMessageDialog(frame,
                                    "It's time for " + prayerName + " prayer! ",
                                    "Prayer Reminder",
                                    JOptionPane.INFORMATION_MESSAGE));
                }
            }
        });
        checker.start();
    }
    // audio notification alert at specific prayer time
    private void playPrayerAlert() {
        try {
            URL alertURL = getClass().getResource("Resources- PP/bell.wav");
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(alertURL);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new PrayerManager();
    }
}