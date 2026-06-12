package smartalarm.view;

import smartalarm.model.question.AnswerResult;
import smartalarm.model.question.Question;

import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
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
        super(owner, "Dismiss Alarm", Dialog.ModalityType.DOCUMENT_MODAL);
        this.questionSupplier = questionSupplier;
        this.onDismiss = onDismiss;
        setSize(440, 330);
        setResizable(false);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        initUI();
        startBeeping();
    }

    private void initUI() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(new EmptyBorder(24, 28, 24, 28));
        panel.setBackground(new Color(30, 30, 46));

        // top title area
        JLabel titleLabel = new JLabel("Alarm is ringing!", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLabel.setForeground(new Color(243, 139, 168));

        JLabel subtitleLabel = new JLabel("Solve the problem below to dismiss the alarm.", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(166, 173, 200));

        JPanel topPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        topPanel.setBackground(new Color(30, 30, 46));
        topPanel.add(titleLabel);
        topPanel.add(subtitleLabel);
        panel.add(topPanel, BorderLayout.NORTH);

        // center: question and input
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(new Color(49, 50, 68));
        centerPanel.setBorder(new EmptyBorder(16, 16, 16, 16));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        questionLabel = new JLabel("", SwingConstants.CENTER);
        questionLabel.setFont(new Font("SansSerif", Font.BOLD, 30));
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
            new EmptyBorder(4, 8, 4, 8)
        ));
        answerField.setPreferredSize(new Dimension(160, 44));
        gbc.gridy = 1; gbc.gridwidth = 2;
        centerPanel.add(answerField, gbc);

        feedbackLabel = new JLabel(" ", SwingConstants.CENTER);
        feedbackLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        gbc.gridy = 2; gbc.gridwidth = 2;
        centerPanel.add(feedbackLabel, gbc);

        panel.add(centerPanel, BorderLayout.CENTER);

        // bottom buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        btnPanel.setBackground(new Color(30, 30, 46));

        submitButton = new JButton("Submit");
        submitButton.setBackground(new Color(137, 180, 250));
        submitButton.setForeground(Color.WHITE);
        submitButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        submitButton.setFocusPainted(false);
        submitButton.setBorderPainted(false);
        submitButton.setPreferredSize(new Dimension(110, 38));
        submitButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        submitButton.addActionListener(e -> checkAnswer());

        dismissButton = new JButton("Dismiss");
        dismissButton.setBackground(new Color(166, 227, 161));
        dismissButton.setForeground(new Color(30, 30, 46));
        dismissButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        dismissButton.setFocusPainted(false);
        dismissButton.setBorderPainted(false);
        dismissButton.setPreferredSize(new Dimension(130, 38));
        dismissButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        dismissButton.setVisible(false);
        dismissButton.addActionListener(e -> dismiss());

        btnPanel.add(submitButton);
        btnPanel.add(dismissButton);
        panel.add(btnPanel, BorderLayout.SOUTH);

        // submit on Enter key
        answerField.addActionListener(e -> checkAnswer());

        add(panel);
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
                feedbackLabel.setText("Correct! Use the button below to dismiss the alarm.");
                submitButton.setEnabled(false);
                answerField.setEnabled(false);
                dismissButton.setVisible(true);
                dismissButton.requestFocus();
            }
            case INCORRECT -> {
                wrongCount++;
                feedbackLabel.setForeground(new Color(243, 139, 168));
                feedbackLabel.setText("Wrong! Try again. (Incorrect: " + wrongCount + ")");
                answerField.selectAll();
                answerField.requestFocus();
                nextQuestion();
            }
            case INVALID_FORMAT -> {
                feedbackLabel.setForeground(new Color(250, 219, 121));
                feedbackLabel.setText("Please enter numbers only.");
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
            buf[2 * i] = (byte) (val & 0xFF);
            buf[2 * i + 1] = (byte) ((val >> 8) & 0xFF);
        }
        AudioFormat af = new AudioFormat(sampleRate, 16, 1, true, false);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, af);
        if (!AudioSystem.isLineSupported(info)) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        try (SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info)) {
            line.open(af);
            line.start();
            line.write(buf, 0, buf.length);
            line.drain();
        } catch (LineUnavailableException | IllegalArgumentException ex) {
            Toolkit.getDefaultToolkit().beep();
        }
    }
}
