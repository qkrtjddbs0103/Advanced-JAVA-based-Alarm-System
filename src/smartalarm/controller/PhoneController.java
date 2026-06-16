package smartalarm.controller;

import smartalarm.mediator.SimulationManager;
import smartalarm.model.Alarm;
import smartalarm.model.question.DictationQuestion;
import smartalarm.model.question.MathQuestion;
import smartalarm.model.question.MissionType;
import smartalarm.view.AlarmPanel;
import smartalarm.view.MissionDialog;
import smartalarm.view.PhoneFrame;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class PhoneController {

    private static final Color COLOR_NEUTRAL = new Color(166, 173, 200);
    private static final Color COLOR_ACTIVE  = new Color(166, 227, 161);
    private static final Color COLOR_RINGING = new Color(243, 139, 168);

    private final PhoneFrame        frame;
    private final AlarmPanel        alarmPanel;
    private final SimulationManager mediator;
    private final Random            rng = new Random();

    private String myName;
    private final Set<String>         addedFriends      = new HashSet<>();
    private final Map<String, Integer> wakeCountByFriend = new HashMap<>();

    private Timer         clockTimer;
    private Alarm         alarm        = null;
    private MissionDialog activeDialog = null;

    public PhoneController(PhoneFrame frame, SimulationManager mediator) {
        this.frame      = frame;
        this.alarmPanel = frame.getAlarmPanel();
        this.mediator   = mediator;
        wireEvents();
        startClock();
    }

    public void init(String id, String name) {
        myName = name;
        mediator.registerName(name, this);
        frame.getSettingsPanel().init(name, this::changeName, this::resetWakeCounts);
        frame.getFriendsPanel().setOnAddFriend(this::tryAddFriend);
    }

    // ── name / wake limit management ─────────────────────────────────────────
    private void resetWakeCounts() { wakeCountByFriend.clear(); }

    private void changeName(String newName) {
        mediator.unregisterName(myName);
        myName = newName;
        mediator.registerName(newName, this);
    }

    // ── friend request flow (sender side) ────────────────────────────────────
    private boolean tryAddFriend(String targetName) {
        if (targetName.isEmpty()
                || targetName.equalsIgnoreCase(myName)
                || addedFriends.contains(targetName)
                || mediator.findByName(targetName) == null) {
            return false;
        }
        addedFriends.add(targetName);
        frame.getFriendsPanel().addFriendPending(targetName);
        mediator.routeFriendRequest(myName, targetName);
        return true;
    }

    // ── friend request flow (receiver side) ──────────────────────────────────
    public void receiveFriendRequest(String fromName) {
        SwingUtilities.invokeLater(() -> showFriendRequestDialog(fromName));
    }

    private void showFriendRequestDialog(String fromName) {
        JDialog dialog = new JDialog(frame, Dialog.ModalityType.DOCUMENT_MODAL);
        dialog.setUndecorated(true);
        dialog.setResizable(false);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(new Color(49, 50, 68));
        outer.setBorder(BorderFactory.createLineBorder(new Color(88, 91, 112), 1));

        // drag handle header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(30, 30, 46));
        header.setBorder(new EmptyBorder(10, 18, 10, 18));
        JLabel headerTitle = new JLabel("Friend Request", SwingConstants.CENTER);
        headerTitle.setFont(new Font("SansSerif", Font.BOLD, 13));
        headerTitle.setForeground(new Color(137, 180, 250));
        header.add(headerTitle);
        addDrag(header, dialog);
        outer.add(header, BorderLayout.NORTH);

        // body
        JPanel body = new JPanel(new GridBagLayout());
        body.setBackground(new Color(49, 50, 68));
        body.setBorder(new EmptyBorder(16, 24, 16, 24));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 12, 4);
        gbc.gridwidth = 2; gbc.gridx = 0; gbc.gridy = 0;

        JLabel msg = new JLabel(
            "<html><center><b>" + fromName + "</b> wants to be your friend.</center></html>",
            SwingConstants.CENTER);
        msg.setForeground(new Color(205, 214, 244));
        msg.setFont(new Font("SansSerif", Font.PLAIN, 14));
        body.add(msg, gbc);

        gbc.gridy = 1; gbc.gridwidth = 1; gbc.insets = new Insets(4, 6, 4, 6);
        JButton acceptBtn  = styledBtn("Accept",  new Color(166, 227, 161), new Color(30, 30, 46));
        JButton declineBtn = styledBtn("Decline", new Color(243, 139, 168), new Color(30, 30, 46));
        body.add(acceptBtn,  gbc);
        gbc.gridx = 1;
        body.add(declineBtn, gbc);

        outer.add(body, BorderLayout.CENTER);
        dialog.add(outer);

        acceptBtn.addActionListener(e -> {
            dialog.dispose();
            if (!addedFriends.contains(fromName)) {
                addedFriends.add(fromName);
                ActionListener wakeListener = buildWakeListener(fromName);
                if (wakeListener != null)
                    frame.getFriendsPanel().addFriend(fromName, wakeListener);
            }
            mediator.routeFriendAccept(myName, fromName);
        });
        declineBtn.addActionListener(e -> {
            dialog.dispose();
            mediator.routeFriendDecline(myName, fromName);
        });

        dialog.pack();
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }

    // ── callbacks from mediator ───────────────────────────────────────────────
    public void friendRequestAccepted(String acceptorName) {
        SwingUtilities.invokeLater(() -> {
            ActionListener wakeListener = buildWakeListener(acceptorName);
            if (wakeListener != null)
                frame.getFriendsPanel().confirmFriend(acceptorName, wakeListener);
            showNotification("Friend Request Accepted!", new Color(166, 227, 161));
        });
    }

    public void friendRequestDenied(String declinerName) {
        SwingUtilities.invokeLater(() -> {
            frame.getFriendsPanel().removeFriendPending(declinerName);
            addedFriends.remove(declinerName);
            showNotification("Friend Request Denied", new Color(243, 139, 168));
        });
    }

    // ── styled notification (replaces JOptionPane) ────────────────────────────
    private void showNotification(String message, Color msgColor) {
        JDialog dlg = new JDialog(frame, Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setUndecorated(true);
        dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(new Color(49, 50, 68));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(88, 91, 112), 1),
            new EmptyBorder(24, 32, 20, 32)));

        JLabel msgLabel = new JLabel(
            "<html><center>" + message + "</center></html>", SwingConstants.CENTER);
        msgLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        msgLabel.setForeground(msgColor);
        panel.add(msgLabel, BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        btnRow.setOpaque(false);
        JButton okBtn = styledBtn("OK", new Color(137, 180, 250), Color.WHITE);
        okBtn.setPreferredSize(new Dimension(90, 34));
        okBtn.addActionListener(e -> dlg.dispose());
        btnRow.add(okBtn);
        panel.add(btnRow, BorderLayout.SOUTH);

        dlg.add(panel);
        dlg.pack();
        dlg.setLocationRelativeTo(frame);
        dlg.setVisible(true);
    }

    // ── alarm core ───────────────────────────────────────────────────────────
    private void wireEvents() {
        alarmPanel.getSetAlarmButton().addActionListener(e -> setAlarm());
        alarmPanel.getCancelAlarmButton().addActionListener(e -> cancelAlarm());
    }

    private void setAlarm() {
        int h = alarmPanel.getHourValue();
        int m = alarmPanel.getMinuteValue();
        int s = alarmPanel.getSecondValue();
        alarm = new Alarm(LocalTime.of(h, m, s));
        alarmPanel.getCancelAlarmButton().setEnabled(true);
        alarmPanel.getSetAlarmButton().setEnabled(false);
        alarmPanel.setAlarmStatus(String.format("Alarm set: %02d:%02d:%02d", h, m, s), COLOR_ACTIVE);
    }

    private void cancelAlarm() {
        alarm = null;
        alarmPanel.getCancelAlarmButton().setEnabled(false);
        alarmPanel.getSetAlarmButton().setEnabled(true);
        alarmPanel.setAlarmStatus("Alarm canceled.", COLOR_NEUTRAL);
        if (activeDialog != null) {
            activeDialog.forceClose();
            activeDialog = null;
        }
    }

    private void startClock() {
        clockTimer = new Timer(500, e -> {
            LocalTime now = LocalTime.now();
            alarmPanel.setCurrentTime(now.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            if (alarm != null && !alarm.isFired() && alarm.matches(now)) {
                alarm.setFired(true);
                triggerAlarm();
            }
        });
        clockTimer.start();
    }

    private void triggerAlarm() {
        alarmPanel.getCancelAlarmButton().setEnabled(true);
        alarmPanel.getSetAlarmButton().setEnabled(false);
        alarmPanel.setAlarmStatus("Alarm ringing! Solve the problem to dismiss.", COLOR_RINGING);
        MissionType type = frame.getSettingsPanel().getMissionType();
        activeDialog = new MissionDialog(frame, () -> switch (type) {
            case MATH      -> new MathQuestion(rng);
            case DICTATION -> new DictationQuestion(rng);
            case BOTH      -> rng.nextBoolean() ? new MathQuestion(rng) : new DictationQuestion(rng);
        }, this::onAlarmDismissed);
        activeDialog.setVisible(true);
    }

    private void onAlarmDismissed() {
        alarm = null;
        alarmPanel.getCancelAlarmButton().setEnabled(false);
        alarmPanel.getSetAlarmButton().setEnabled(true);
        alarmPanel.setAlarmStatus("Alarm dismissed.", COLOR_NEUTRAL);
        activeDialog = null;
    }

    public void receiveRemoteAlarm(String fromName) {
        if (activeDialog != null) return;
        int limit = frame.getSettingsPanel().getWakeLimit();
        if (limit > 0) {
            int count = wakeCountByFriend.getOrDefault(fromName, 0);
            if (count >= limit) {
                mediator.routeWakeLimitExceeded(myName, fromName);
                return;
            }
            wakeCountByFriend.put(fromName, count + 1);
        }
        triggerAlarm();
    }

    public void receiveWakeLimitExceeded(String targetName) {
        SwingUtilities.invokeLater(() ->
            showNotification(
                "<html><center>You can no longer<br>wake <b>" + targetName + "</b></center></html>",
                new Color(243, 139, 168)));
    }

    // ── helpers ───────────────────────────────────────────────────────────────
    private ActionListener buildWakeListener(String targetName) {
        PhoneController target = mediator.findByName(targetName);
        if (target == null) return null;
        String targetId = mediator.getIdOf(target);
        return e -> mediator.relay(targetId, myName);
    }

    private static JButton styledBtn(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
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
