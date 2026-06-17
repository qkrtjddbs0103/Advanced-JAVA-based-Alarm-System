package smartalarm.view;

import smartalarm.model.question.MissionType;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static java.awt.RenderingHints.*;

public class SettingsPanel extends JPanel {

    private static final int ITEM_X = 10;
    private static final int ITEM_W = 370;

    private static final Image TITLE_IMG   = new ImageIcon("assets/UI_Images/settings_title.png").getImage();
    private static final Image NAME_IMG    = new ImageIcon("assets/UI_Images/settings_name_item.png").getImage();
    private static final Image SAVE_IMG    = new ImageIcon("assets/UI_Images/settings_save_btn.png").getImage();
    private static final Image LIMIT_IMG   = new ImageIcon("assets/UI_Images/settings_wakelimit_item.png").getImage();
    private static final Image SET_IMG     = new ImageIcon("assets/UI_Images/settings_set_btn.png").getImage();
    private static final Image MISSION_IMG = new ImageIcon("assets/UI_Images/settings_missiontype_item.png").getImage();
    private static final Image BOTH_IMG    = new ImageIcon("assets/UI_Images/settings_both_btn.png").getImage();
    private static final Image MATH_IMG    = new ImageIcon("assets/UI_Images/settings_math_btn.png").getImage();
    private static final Image DICT_IMG    = new ImageIcon("assets/UI_Images/settings_dictation_btn.png").getImage();
    private static final Image SOUND_IMG   = new ImageIcon("assets/UI_Images/settings_sound_item.png").getImage();
    private static final Image CHANGE_IMG  = new ImageIcon("assets/UI_Images/settings_change_btn.png").getImage();

    private final JTextField nameField;

    private int    pendingWakeLimit = 0;
    private int    activeWakeLimit  = 0;
    private JLabel limitDisplay;

    private MissionType missionType = MissionType.BOTH;
    private JButton     missionBtn;

    private static final Map<String, String> SOUND_DISPLAY_NAMES = Map.of(
        "IU_moring_call",  "IU",
        "bell_ringing",    "Ringing Bell",
        "beaver_scream",   "Screaming Beaver",
        "iphone_alarm",    "Iphone"
    );

    private JLabel miniTimeLbl;
    private final List<File> soundFiles = new ArrayList<>();
    private int    soundIndex = 0;  // 0 = Beep, 1+ = soundFiles
    private JLabel soundNameLabel;

    private String           savedName = "";
    private Consumer<String> onNameSave;
    private Runnable         onWakeLimitSet;

    public SettingsPanel() {
        setLayout(null);
        setOpaque(false);

        miniTimeLbl = new JLabel("00:00", SwingConstants.LEFT);
        miniTimeLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        miniTimeLbl.setForeground(new Color(37, 99, 168));
        miniTimeLbl.setOpaque(false);
        miniTimeLbl.setBounds(25, 23, 80, 18);
        add(miniTimeLbl);

        // ── Name item (y=108, h=90) ──────────────────────────────────────────
        ItemPanel nameItem = new ItemPanel(NAME_IMG);
        nameItem.setBounds(ITEM_X, 118, ITEM_W, 90);

        nameField = new JTextField();
        nameField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        nameField.setForeground(new Color(90, 127, 168));
        nameField.setCaretColor(new Color(205, 214, 244));
        nameField.setOpaque(false);
        nameField.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
        nameField.setBounds(21, 53, 244, 28);
        nameItem.add(nameField);

        JButton saveBtn = imageButton(SAVE_IMG);
        saveBtn.setBounds(278, 15, 72, 30);
        saveBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (!name.isEmpty() && onNameSave != null) {
                savedName = name;
                onNameSave.accept(name);
            }
        });
        nameField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                if (saveBtn.equals(e.getOppositeComponent())) return;
                nameField.setText(savedName);
            }
        });
        nameField.addActionListener(e -> saveBtn.doClick());
        nameItem.add(saveBtn);
        add(nameItem);

        // ── Wake Limit item (y=202, h=64) ────────────────────────────────────
        ItemPanel limitItem = new ItemPanel(LIMIT_IMG);
        limitItem.setBounds(ITEM_X, 274, ITEM_W, 64);

        limitDisplay = new JLabel(String.valueOf(pendingWakeLimit), SwingConstants.CENTER);
        limitDisplay.setFont(new Font("Monospaced", Font.BOLD, 20));
        limitDisplay.setForeground(new Color(166, 173, 200));
        limitDisplay.setOpaque(false);
        limitDisplay.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        limitDisplay.setBounds(188, 18, 34, 28);
        limitDisplay.addMouseWheelListener(e -> {
            pendingWakeLimit -= (int) e.getWheelRotation();
            if (pendingWakeLimit < 0) pendingWakeLimit = 9;
            if (pendingWakeLimit > 9) pendingWakeLimit = 0;
            limitDisplay.setText(String.valueOf(pendingWakeLimit));
        });
        limitItem.add(limitDisplay);

        JButton setBtn = imageButton(SET_IMG);
        setBtn.setBounds(278, 17, 72, 30);
        setBtn.addActionListener(e -> {
            activeWakeLimit = pendingWakeLimit;
            if (onWakeLimitSet != null) onWakeLimitSet.run();
        });
        limitItem.add(setBtn);
        add(limitItem);

        // ── Mission Type item (y=270, h=64) ──────────────────────────────────
        ItemPanel missionItem = new ItemPanel(MISSION_IMG);
        missionItem.setBounds(ITEM_X, 404, ITEM_W, 64);

        missionBtn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(KEY_INTERPOLATION, VALUE_INTERPOLATION_BICUBIC);
                g2.setRenderingHint(KEY_RENDERING,     VALUE_RENDER_QUALITY);
                g2.drawImage(missionImage(), 0, 0, getWidth(), getHeight(), this);
            }
        };
        missionBtn.setBorderPainted(false);
        missionBtn.setContentAreaFilled(false);
        missionBtn.setFocusPainted(false);
        missionBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        missionBtn.setBounds(250, 17, 100, 30);
        missionBtn.addActionListener(e -> {
            missionType = missionType.next();
            missionBtn.repaint();
        });
        missionItem.add(missionBtn);
        add(missionItem);

        // ── Sound item (y=480, h=90) ─────────────────────────────────────────
        loadSoundFiles();
        ItemPanel soundItem = new ItemPanel(SOUND_IMG);
        soundItem.setBounds(ITEM_X, 480, ITEM_W, 90);

        soundNameLabel = new JLabel(currentSoundName(), SwingConstants.LEFT);
        soundNameLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        soundNameLabel.setForeground(new Color(166, 173, 200));
        soundNameLabel.setOpaque(false);
        soundNameLabel.setBounds(33, 54, 244, 28);
        soundItem.add(soundNameLabel);

        JButton changeBtn = imageButton(CHANGE_IMG);
        changeBtn.setBounds(278, 15, 72, 30);
        changeBtn.addActionListener(e -> {
            soundIndex = (soundIndex + 1) % (soundFiles.size() + 1);
            soundNameLabel.setText(currentSoundName());
        });
        soundItem.add(changeBtn);
        add(soundItem);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(KEY_INTERPOLATION, VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(KEY_RENDERING,     VALUE_RENDER_QUALITY);
        g2.setRenderingHint(KEY_ANTIALIASING,  VALUE_ANTIALIAS_ON);
        g2.drawImage(TITLE_IMG, 24, 39, 140, 56, this);
    }

    public void init(String initialName, Consumer<String> onNameSave, Runnable onWakeLimitSet) {
        savedName = initialName;
        nameField.setText(initialName);
        this.onNameSave     = onNameSave;
        this.onWakeLimitSet = onWakeLimitSet;
    }

    public void setMiniTime(String text)   { miniTimeLbl.setText(text); }
    public int         getWakeLimit()      { return activeWakeLimit; }
    public MissionType getMissionType()    { return missionType; }
    public File getAlarmSoundFile() {
        return (soundIndex == 0 || soundFiles.isEmpty()) ? null : soundFiles.get(soundIndex - 1);
    }

    private void loadSoundFiles() {
        File dir = new File("assets/sounds");
        if (!dir.exists() || !dir.isDirectory()) return;
        File[] files = dir.listFiles((d, n) -> n.toLowerCase().endsWith(".wav"));
        if (files != null) {
            Arrays.sort(files);
            soundFiles.addAll(Arrays.asList(files));
        }
    }

    private String currentSoundName() {
        if (soundIndex == 0) return "Beep";
        String filename = soundFiles.get(soundIndex - 1).getName();
        String key = filename.substring(0, filename.lastIndexOf('.'));
        return SOUND_DISPLAY_NAMES.getOrDefault(key, key);
    }

    private Image missionImage() {
        return switch (missionType) {
            case MATH      -> MATH_IMG;
            case DICTATION -> DICT_IMG;
            case BOTH      -> BOTH_IMG;
        };
    }

    private static JButton imageButton(Image img) {
        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(KEY_INTERPOLATION, VALUE_INTERPOLATION_BICUBIC);
                g2.setRenderingHint(KEY_RENDERING,     VALUE_RENDER_QUALITY);
                g2.drawImage(img, 0, 0, getWidth(), getHeight(), this);
            }
        };
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private static class ItemPanel extends JPanel {
        private final Image img;

        ItemPanel(Image img) {
            this.img = img;
            setLayout(null);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(KEY_INTERPOLATION, VALUE_INTERPOLATION_BICUBIC);
            g2.setRenderingHint(KEY_RENDERING,     VALUE_RENDER_QUALITY);
            g2.setRenderingHint(KEY_ANTIALIASING,  VALUE_ANTIALIAS_ON);
            g2.drawImage(img, 0, 0, getWidth(), getHeight(), this);
        }
    }
}
