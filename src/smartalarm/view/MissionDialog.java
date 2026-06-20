package smartalarm.view;

import smartalarm.model.question.Question;

import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.File;
import java.util.function.Supplier;

import static java.awt.RenderingHints.*;

public class MissionDialog extends JDialog {

    private static final Image DIALOG_BG  = new ImageIcon("assets/UI_Images/background.png").getImage();
    private static final Image INPUT_IMG  = new ImageIcon("assets/UI_Images/input_box.png").getImage();
    private static final Image SUBMIT_IMG  = new ImageIcon("assets/UI_Images/mission_submit_btn.png").getImage();
    private static final Image DISMISS_IMG = new ImageIcon("assets/UI_Images/mission_dismiss_btn.png").getImage();

    private final Supplier<Question> questionSupplier;
    private final Supplier<File>     soundSupplier;
    private final Runnable           onDismiss;

    private Question currentQuestion;
    private int wrongCount = 0;

    private JLabel questionLabel;
    private JTextField answerField;
    private JLabel feedbackLabel;
    private JButton submitButton;
    private JButton dismissButton;
    private Timer beepTimer;
    private Clip  activeClip = null;
    private boolean closed = false;

    public MissionDialog(JFrame owner, Supplier<Question> questionSupplier, Supplier<File> soundSupplier, Runnable onDismiss) {
        super(owner, Dialog.ModalityType.DOCUMENT_MODAL);
        this.questionSupplier = questionSupplier;
        this.soundSupplier    = soundSupplier;
        this.onDismiss        = onDismiss;
        setUndecorated(true);
        setResizable(false);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        initUI();
        pack();
        setLocationRelativeTo(owner);
        startBeeping();
    }

    private void initUI() {
        JPanel outer = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(KEY_INTERPOLATION, VALUE_INTERPOLATION_BICUBIC);
                g2.setRenderingHint(KEY_RENDERING,     VALUE_RENDER_QUALITY);
                g2.drawImage(DIALOG_BG, 0, 0, getWidth(), getHeight(), this);
            }
        };
        outer.setBorder(BorderFactory.createLineBorder(new Color(59, 139, 255, 77), 1));

        // ── drag handle header ────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(10, 18, 10, 18));
        JLabel headerTitle = new JLabel("Dismiss Alarm", SwingConstants.CENTER);
        headerTitle.setFont(new Font("SansSerif", Font.BOLD, 13));
        headerTitle.setForeground(new Color(224, 53, 53));
        header.add(headerTitle);
        addDrag(header, this);
        outer.add(header, BorderLayout.NORTH);

        // ── content ───────────────────────────────────────────────────────────
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(new EmptyBorder(4, 16, 16, 16));
        panel.setOpaque(false);

        JLabel titleLabel = new JLabel("Alarm is ringing!", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 17));
        titleLabel.setForeground(new Color(224, 53, 53));

        JLabel subtitleLabel = new JLabel("Solve the problem to dismiss.", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(166, 173, 200));

        JPanel topPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        topPanel.setOpaque(false);
        topPanel.add(titleLabel);
        topPanel.add(subtitleLabel);
        panel.add(topPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        centerPanel.setBorder(new EmptyBorder(12, 12, 12, 12));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        questionLabel = new JLabel("", SwingConstants.CENTER);
        questionLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        questionLabel.setForeground(new Color(26, 74, 138));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        centerPanel.add(questionLabel, gbc);

        answerField = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(KEY_INTERPOLATION, VALUE_INTERPOLATION_BICUBIC);
                g2.setRenderingHint(KEY_RENDERING,     VALUE_RENDER_QUALITY);
                g2.drawImage(INPUT_IMG, 0, 0, getWidth(), getHeight(), this);
                super.paintComponent(g);
            }
        };
        answerField.setOpaque(false);
        answerField.setFont(new Font("SansSerif", Font.BOLD, 22));
        answerField.setHorizontalAlignment(SwingConstants.CENTER);
        answerField.setForeground(new Color(90, 127, 168));
        answerField.setCaretColor(new Color(205, 214, 244));
        answerField.setBorder(new EmptyBorder(4, 8, 4, 8));
        answerField.setPreferredSize(new Dimension(264, 40));
        gbc.gridy = 1; gbc.gridwidth = 2;
        centerPanel.add(answerField, gbc);

        feedbackLabel = new JLabel("<html><center>&nbsp;</center></html>", SwingConstants.CENTER);
        feedbackLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        gbc.gridy = 2; gbc.gridwidth = 2;
        centerPanel.add(feedbackLabel, gbc);

        panel.add(centerPanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        btnPanel.setOpaque(false);

        submitButton = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(KEY_INTERPOLATION, VALUE_INTERPOLATION_BICUBIC);
                g2.setRenderingHint(KEY_RENDERING,     VALUE_RENDER_QUALITY);
                g2.drawImage(SUBMIT_IMG, 0, 0, getWidth(), getHeight(), this);
            }
        };
        submitButton.setBorderPainted(false);
        submitButton.setContentAreaFilled(false);
        submitButton.setFocusPainted(false);
        submitButton.setPreferredSize(new Dimension(100, 34));
        submitButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        submitButton.addActionListener(e -> checkAnswer());

        dismissButton = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(KEY_INTERPOLATION, VALUE_INTERPOLATION_BICUBIC);
                g2.setRenderingHint(KEY_RENDERING,     VALUE_RENDER_QUALITY);
                g2.drawImage(DISMISS_IMG, 0, 0, getWidth(), getHeight(), this);
            }
        };
        dismissButton.setBorderPainted(false);
        dismissButton.setContentAreaFilled(false);
        dismissButton.setFocusPainted(false);
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
                feedbackLabel.setForeground(new Color(26, 74, 138));
                feedbackLabel.setText("<html><center>Correct! Press Dismiss to stop the alarm.</center></html>");
                submitButton.setVisible(false);
                answerField.setEnabled(false);
                dismissButton.setVisible(true);
                dismissButton.requestFocus();
            }
            case INCORRECT -> {
                wrongCount++;
                feedbackLabel.setForeground(new Color(224, 53, 53));
                feedbackLabel.setText("<html><center>Wrong! Try again. (" + wrongCount + " incorrect)</center></html>");
                answerField.selectAll();
                answerField.requestFocus();
                nextQuestion();
            }
            case INVALID_FORMAT -> {
                feedbackLabel.setForeground(new Color(26, 74, 138));
                feedbackLabel.setText("<html><center>Please enter numbers only.</center></html>");
                answerField.selectAll();
            }
        }
    }

    private void dismiss() {
        closed = true;
        beepTimer.stop();
        stopClip();
        dispose();
        onDismiss.run();
    }

    public void forceClose() {
        closed = true;
        if (beepTimer != null) beepTimer.stop();
        stopClip();
        dispose();
    }

    private void stopClip() {
        if (activeClip != null) {
            activeClip.stop();
            activeClip.close();
            activeClip = null;
        }
    }

    private void startBeeping() {
        beepTimer = new Timer(1200, e -> playBeep());
        beepTimer.setInitialDelay(0);
        beepTimer.start();
    }

    private void playBeep() {
        if (closed) return;
        File f = soundSupplier != null ? soundSupplier.get() : null;
        if (f != null && f.exists()) {
            playFromFile(f);
        } else {
            playGeneratedBeep();
        }
    }

    private void playFromFile(File f) {
        if (activeClip != null && activeClip.isRunning()) return;
        try {
            if (activeClip != null) activeClip.close();
            AudioInputStream ais = AudioSystem.getAudioInputStream(f);
            activeClip = AudioSystem.getClip();
            activeClip.open(ais);
            activeClip.start();
        } catch (Exception ex) {
            Toolkit.getDefaultToolkit().beep();
        }
    }

    private void playGeneratedBeep() {
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
