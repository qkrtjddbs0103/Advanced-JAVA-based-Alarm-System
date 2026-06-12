package smartalarm.view;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class AlarmPanel extends JPanel {

    private JSpinner hourSpinner, minuteSpinner, secondSpinner;
    private JLabel currentTimeLabel;
    private JLabel alarmStatusLabel;
    private JButton setAlarmButton, cancelAlarmButton;

    public AlarmPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setBackground(new Color(30, 30, 46));

        // current time display
        currentTimeLabel = new JLabel("00:00:00", SwingConstants.CENTER);
        currentTimeLabel.setFont(new Font("SansSerif", Font.BOLD, 52));
        currentTimeLabel.setForeground(new Color(205, 214, 244));
        add(currentTimeLabel, BorderLayout.NORTH);

        // alarm time settings panel
        JPanel setPanel = new JPanel(new GridBagLayout());
        setPanel.setBackground(new Color(49, 50, 68));
        setPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(137, 180, 250), 1),
            "Set Alarm Time",
            TitledBorder.CENTER, TitledBorder.TOP,
            new Font("SansSerif", Font.BOLD, 13),
            new Color(137, 180, 250)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);

        hourSpinner = createTimeSpinner(0, 23);
        minuteSpinner = createTimeSpinner(0, 59);
        secondSpinner = createTimeSpinner(0, 59);

        gbc.gridx = 0; gbc.gridy = 0;
        setPanel.add(styledLabel("Hour"), gbc);
        gbc.gridx = 1;
        setPanel.add(hourSpinner, gbc);
        gbc.gridx = 2;
        setPanel.add(styledLabel("Min"), gbc);
        gbc.gridx = 3;
        setPanel.add(minuteSpinner, gbc);
        gbc.gridx = 4;
        setPanel.add(styledLabel("Sec"), gbc);
        gbc.gridx = 5;
        setPanel.add(secondSpinner, gbc);

        add(setPanel, BorderLayout.CENTER);

        // bottom buttons and status display
        JPanel bottomPanel = new JPanel(new BorderLayout(8, 8));
        bottomPanel.setBackground(new Color(30, 30, 46));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        buttonPanel.setBackground(new Color(30, 30, 46));

        setAlarmButton = new JButton("Set Alarm");
        styleButton(setAlarmButton, new Color(137, 180, 250), Color.WHITE);

        cancelAlarmButton = new JButton("Cancel Alarm");
        styleButton(cancelAlarmButton, new Color(243, 139, 168), Color.WHITE);
        cancelAlarmButton.setEnabled(false);

        buttonPanel.add(setAlarmButton);
        buttonPanel.add(cancelAlarmButton);

        alarmStatusLabel = new JLabel("No alarm set.", SwingConstants.CENTER);
        alarmStatusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        alarmStatusLabel.setForeground(new Color(166, 173, 200));

        bottomPanel.add(buttonPanel, BorderLayout.CENTER);
        bottomPanel.add(alarmStatusLabel, BorderLayout.SOUTH);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JSpinner createTimeSpinner(int min, int max) {
        SpinnerNumberModel model = new SpinnerNumberModel(0, min, max, 1);
        JSpinner spinner = new JSpinner(model);
        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(spinner, "00");
        spinner.setEditor(editor);
        spinner.setPreferredSize(new Dimension(58, 32));
        spinner.setFont(new Font("SansSerif", Font.BOLD, 16));
        JFormattedTextField tf = ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField();
        tf.setBackground(new Color(69, 71, 90));
        tf.setForeground(new Color(205, 214, 244));
        tf.setCaretColor(new Color(205, 214, 244));
        tf.setHorizontalAlignment(SwingConstants.CENTER);
        return spinner;
    }

    private JLabel styledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 15));
        label.setForeground(new Color(166, 173, 200));
        return label;
    }

    private void styleButton(JButton button, Color bg, Color fg) {
        button.setBackground(bg);
        button.setForeground(fg);
        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(120, 36));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public JSpinner getHourSpinner() { return hourSpinner; }
    public JSpinner getMinuteSpinner() { return minuteSpinner; }
    public JSpinner getSecondSpinner() { return secondSpinner; }
    public JButton getSetAlarmButton() { return setAlarmButton; }
    public JButton getCancelAlarmButton() { return cancelAlarmButton; }

    public void setCurrentTime(String text) {
        currentTimeLabel.setText(text);
    }

    public void setAlarmStatus(String text, Color color) {
        alarmStatusLabel.setForeground(color);
        alarmStatusLabel.setText(text);
    }
}
