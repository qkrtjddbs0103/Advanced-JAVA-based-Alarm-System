package smartalarm.view;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class AlarmPanel extends JPanel {

    // ── Transparent numeric field (replaces JSpinner) ──────────────────────
    static class TimeField extends JLabel {
        private int value = 0;

        TimeField(int min, int max) {
            super("00", SwingConstants.CENTER);
            setFont(new Font("Consolas", Font.BOLD, 30));
            setForeground(new Color(0x1a4a8a));
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseWheelListener(e -> {
                value -= (int) e.getWheelRotation();
                if (value < min) value = max;
                if (value > max) value = min;
                setText(String.format("%02d", value));
            });
        }

        int getValue() { return value; }
    }

    // ── Fields ──────────────────────────────────────────────────────────────
    private final TimeField hourField   = new TimeField(0, 23);
    private final TimeField minuteField = new TimeField(0, 59);
    private final TimeField secondField = new TimeField(0, 59);

    private JLabel  miniTimeLbl;
    private JLabel  currentTimeLabel;
    private JLabel  alarmStatusLabel;
    private JButton setAlarmButton, cancelAlarmButton;

    private final Image clockPanelImg;
    private final Image alarmSetPanelImg;

    // ── Constructor ─────────────────────────────────────────────────────────
    public AlarmPanel() {
        setLayout(null);
        setOpaque(false);

        clockPanelImg    = new ImageIcon("assets/UI_Images/clock_panel.png").getImage();
        alarmSetPanelImg = new ImageIcon("assets/UI_Images/alarm_set_panel.png").getImage();

        JLabel miniTimeLabel = new JLabel("00:00", SwingConstants.LEFT);
        miniTimeLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        miniTimeLabel.setForeground(new Color(37, 99, 168));
        miniTimeLabel.setOpaque(false);
        miniTimeLabel.setBounds(25, 23, 80, 18);
        add(miniTimeLabel);
        this.miniTimeLbl = miniTimeLabel;

        currentTimeLabel = new JLabel("00:00:00", SwingConstants.CENTER);
        currentTimeLabel.setFont(new Font("Consolas", Font.BOLD, 52));
        currentTimeLabel.setForeground(new Color(0x1a4a8a));
        currentTimeLabel.setOpaque(false);
        currentTimeLabel.setBounds(30, 135, 330, 90);
        add(currentTimeLabel);

        hourField.setBounds(79,  305, 72, 64);
        minuteField.setBounds(159, 305, 72, 64);
        secondField.setBounds(239, 305, 72, 64);
        add(hourField);
        add(minuteField);
        add(secondField);

        setAlarmButton    = imageButton("assets/UI_Images/set_alarm_btn.png",    330, 54);
        cancelAlarmButton = imageButton("assets/UI_Images/cancel_alarm_btn.png", 330, 54);
        setAlarmButton.setBounds(30, 394, 330, 54);
        cancelAlarmButton.setBounds(30, 460, 330, 54);
        cancelAlarmButton.setEnabled(false);
        add(setAlarmButton);
        add(cancelAlarmButton);

        alarmStatusLabel = new JLabel("No alarm set.", SwingConstants.CENTER);
        alarmStatusLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        alarmStatusLabel.setForeground(new Color(166, 173, 200));
        alarmStatusLabel.setOpaque(false);
        alarmStatusLabel.setBounds(30, 526, 330, 40);
        add(alarmStatusLabel);
    }

    // ── Painting ─────────────────────────────────────────────────────────────
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,     RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_ON);

        if (clockPanelImg    != null) g2.drawImage(clockPanelImg,    30, 118, 330, 110, this);
        if (alarmSetPanelImg != null) g2.drawImage(alarmSetPanelImg, 30, 246, 330, 130, this);

    }

    // ── Image loading ─────────────────────────────────────────────────────────
    private static JButton imageButton(String path, int w, int h) {
        ImageIcon icon = loadHQ(path, w, h);
        JButton btn = new JButton(icon);
        btn.setDisabledIcon(icon);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    static ImageIcon loadHQ(String path, int w, int h) {
        Image src = new ImageIcon(path).getImage();
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = out.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,     RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_ON);
        g2.drawImage(src, 0, 0, w, h, null);
        g2.dispose();
        return new ImageIcon(out);
    }

    // ── Getters ───────────────────────────────────────────────────────────────
    public int     getHourValue()         { return hourField.getValue(); }
    public int     getMinuteValue()       { return minuteField.getValue(); }
    public int     getSecondValue()       { return secondField.getValue(); }
    public JButton getSetAlarmButton()    { return setAlarmButton; }
    public JButton getCancelAlarmButton() { return cancelAlarmButton; }

    public void setCurrentTime(String text) { currentTimeLabel.setText(text); }
    public void setMiniTime(String text)    { miniTimeLbl.setText(text); }

    public void setAlarmStatus(String text, Color color) {
        alarmStatusLabel.setForeground(color);
        alarmStatusLabel.setText(text);
    }
}
