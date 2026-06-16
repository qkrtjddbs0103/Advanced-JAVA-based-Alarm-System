package smartalarm.view;

import smartalarm.model.question.AnswerResult;
import smartalarm.model.question.Question;

import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.function.Supplier;

public class MissionDialog extends JDialog {

    private final Supplier<Question> questionSupplier;
    private final Runnable onDismiss;

    private Question currentQuestion;
    private int wrongCount = 0;

    private JLabel questionLabel;
    private JTextField answerField;
    private JLabel feedbackLabel;
    private JButton submitButton;
    private JButton dismissButton;
    private Timer beepTimer;
    private boolean closed = false;

    public MissionDialog(JFrame owner, Supplier<Question> questionSupplier, Runnable onDismiss) {
        super(owner, Dialog.ModalityType.DOCUMENT_MODAL);
        this.questionSupplier = questionSupplier;
        this.onDismiss = onDismiss;
        setUndecorated(true);
        setResizable(false);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        initUI();
        pack();
        setLocationRelativeTo(owner);
        startBeeping();
    }

    private void initUI() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(new Color(30, 30, 46));
        outer.setBorder(BorderFactory.createLineBorder(new Color(88, 91, 112), 1));

        // ── drag handle header ────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(30, 30, 46));
        header.setBorder(new EmptyBorder(10, 18, 10, 18));
        JLabel headerTitle = new JLabel("Dismiss Alarm", SwingConstants.CENTER);
        headerTitle.setFont(new Font("SansSerif", Font.BOLD, 13));
        headerTitle.setForeground(new Color(243, 139, 168));
        header.add(headerTitle);
        addDrag(header, this);
        outer.add(header, BorderLayout.NORTH);

        // ── content ───────────────────────────────────────────────────────────
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(new EmptyBorder(4, 16, 16, 16));
        panel.setBackground(new Color(30, 30, 46));

        JLabel titleLabel = new JLabel("Alarm is ringing!", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 17));
        titleLabel.setForeground(new Color(243, 139, 168));

        JLabel subtitleLabel = new JLabel("Solve the problem to dismiss.", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(166, 173, 200));

        JPanel topPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        topPanel.setBackground(new Color(30, 30, 46));
        topPanel.add(titleLabel);
        topPanel.add(subtitleLabel);
        panel.add(topPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(new Color(49, 50, 68));
        centerPanel.setBorder(new EmptyBorder(12, 12, 12, 12));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        questionLabel = new JLabel("", SwingConstants.CENTER);
        questionLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        questionLabel.setForeground(new Color(250, 219, 121));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        centerPanel.add(questionLabel, gbc);

        answerField = new JTextField();
        answerField.setFont(new Font("SansSerif", Font.BOLD, 22));
        answerField.setHorizontalAlignment(SwingConstants.CENTER);
        answerField.setBackground(new Color(69, 71, 90));
        answerField.setForeground(new Color(205, 214, 244));
        answerField.setCaretColor(new Color(205, 214, 244));
        answerField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(137, 180, 250), 1),
            new EmptyBorder(4, 8, 4, 8)));
        answerField.setPreferredSize(new Dimension(220, 40));
        gbc.gridy = 1; gbc.gridwidth = 2;
        centerPanel.add(answerField, gbc);

        feedbackLabel = new JLabel("<html><center>&nbsp;</center></html>", SwingConstants.CENTER);
        feedbackLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        gbc.gridy = 2; gbc.gridwidth = 2;
        centerPanel.add(feedbackLabel, gbc);

        panel.add(centerPanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        btnPanel.setBackground(new Color(30, 30, 46));

        submitButton = new JButton("Submit");
        submitButton.setBackground(new Color(137, 180, 250));
        submitButton.setForeground(Color.WHITE);
        submitButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        submitButton.setFocusPainted(false);
        submitButton.setBorderPainted(false);
        submitButton.setPreferredSize(new Dimension(100, 34));
        submitButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        submitButton.addActionListener(e -> checkAnswer());

        dismissButton = new JButton("Dismiss");
        dismissButton.setBackground(new Color(166, 227, 161));
        dismissButton.setForeground(new Color(30, 30, 46));
        dismissButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        dismissButton.setFocusPainted(false);
        dismissButton.setBorderPainted(false);
        dismissButton.setPreferredSize(new Dimension(120, 34));
        dismissButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        dismissButton.setVisible(false);
        dismissButton.addActionListener(e -> dismiss());

        btnPanel.add(submitButton);
        btnPanel.add(dismissButton);
        panel.add(btnPanel, BorderLayout.SOUTH);

        answerField.addActionListener(e -> checkAnswer());

        outer.add(panel, BorderLayout.CENTER);
        add(outer);
        nextQuestion();
    }

    private void nextQuestion() {
        currentQuestion = questionSupplier.get();
        questionLabel.setText(currentQuestion.getText());
        answerField.setText("");
        answerField.requestFocus();
    }

    private void checkAnswer() {
        String input = answerField.getText().trim();
        switch (currentQuestion.checkAnswer(input)) {
            case CORRECT -> {
                beepTimer.stop();
                feedbackLabel.setForeground(new Color(166, 227, 161));
                feedbackLabel.setText("<html><center>Correct! Press Dismiss to stop the alarm.</center></html>");
                submitButton.setEnabled(false);
                answerField.setEnabled(false);
                dismissButton.setVisible(true);
                dismissButton.requestFocus();
            }
            case INCORRECT -> {
                wrongCount++;
                feedbackLabel.setForeground(new Color(243, 139, 168));
                feedbackLabel.setText("<html><center>Wrong! Try again. (" + wrongCount + " incorrect)</center></html>");
                answerField.selectAll();
                answerField.requestFocus();
                nextQuestion();
            }
            case INVALID_FORMAT -> {
                feedbackLabel.setForeground(new Color(250, 219, 121));
                feedbackLabel.setText("<html><center>Please enter numbers only.</center></html>");
                answerField.selectAll();
            }
        }
    }

    private void dismiss() {
        closed = true;
        beepTimer.stop();
        dispose();
        onDismiss.run();
    }

    public void forceClose() {
        closed = true;
        if (beepTimer != null) beepTimer.stop();
        dispose();
    }

    private void startBeeping() {
        beepTimer = new Timer(1200, e -> playBeep());
        beepTimer.setInitialDelay(0);
        beepTimer.start();
    }

    private void playBeep() {
        if (closed) return;
        float sampleRate = 44100f;
        int numSamples = (int) (sampleRate * 400 / 1000);
        byte[] buf = new byte[2 * numSamples];
        double freq = 880.0;
        for (int i = 0; i < numSamples; i++) {
            double angle = 2.0 * Math.PI * i * freq / sampleRate;
            short val = (short) (Short.MAX_VALUE * 0.6 * Math.sin(angle));
            buf[2 * i]     = (byte) (val & 0xFF);
            buf[2 * i + 1] = (byte) ((val >> 8) & 0xFF);
        }
        AudioFormat af = new AudioFormat(sampleRate, 16, 1, true, false);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, af);
        if (!AudioSystem.isLineSupported(info)) { Toolkit.getDefaultToolkit().beep(); return; }
        try (SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info)) {
            line.open(af); line.start(); line.write(buf, 0, buf.length); line.drain();
        } catch (LineUnavailableException | IllegalArgumentException ex) {
            Toolkit.getDefaultToolkit().beep();
        }
    }

    private static void addDrag(Component handle, Window window) {
        int[] ox = {0}, oy = {0};
        handle.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { ox[0] = e.getX(); oy[0] = e.getY(); }
        });
        handle.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                Point p = window.getLocation();
                window.setLocation(p.x + e.getX() - ox[0], p.y + e.getY() - oy[0]);
            }
        });
    }
}
