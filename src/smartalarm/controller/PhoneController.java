package smartalarm.controller;

import smartalarm.mediator.SimulationManager;
import smartalarm.model.Alarm;
import smartalarm.model.question.MathQuestion;
import smartalarm.view.AlarmPanel;
import smartalarm.view.MissionDialog;
import smartalarm.view.PhoneFrame;

import javax.swing.*;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class PhoneController {

    private static final Color COLOR_NEUTRAL = new Color(166, 173, 200);
    private static final Color COLOR_ACTIVE = new Color(166, 227, 161);
    private static final Color COLOR_RINGING = new Color(243, 139, 168);

    private final PhoneFrame frame;
    private final AlarmPanel alarmPanel;
    private final SimulationManager mediator;
    private final Random rng = new Random();

    private Timer clockTimer;
    private Alarm alarm = null;
    private MissionDialog activeDialog = null;

    public PhoneController(PhoneFrame frame, SimulationManager mediator) {
        this.frame = frame;
        this.alarmPanel = frame.getAlarmPanel();
        this.mediator = mediator;
        wireEvents();
        startClock();
    }

    private void wireEvents() {
        alarmPanel.getSetAlarmButton().addActionListener(e -> setAlarm());
        alarmPanel.getCancelAlarmButton().addActionListener(e -> cancelAlarm());
    }

    private void setAlarm() {
        int h = (int) alarmPanel.getHourSpinner().getValue();
        int m = (int) alarmPanel.getMinuteSpinner().getValue();
        int s = (int) alarmPanel.getSecondSpinner().getValue();
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
        activeDialog = new MissionDialog(frame, () -> new MathQuestion(rng), this::onAlarmDismissed);
        activeDialog.setVisible(true);
    }

    private void onAlarmDismissed() {
        alarm = null;
        alarmPanel.getCancelAlarmButton().setEnabled(false);
        alarmPanel.getSetAlarmButton().setEnabled(true);
        alarmPanel.setAlarmStatus("Alarm dismissed.", COLOR_NEUTRAL);
        activeDialog = null;
    }

    public void sendRemoteAlarm(String targetId) {
        mediator.relay(targetId);
    }

    public void receiveRemoteAlarm() {
        if (activeDialog != null) return;
        triggerAlarm();
    }
}
