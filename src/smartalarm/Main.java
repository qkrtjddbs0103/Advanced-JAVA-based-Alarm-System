package smartalarm;

import smartalarm.controller.PhoneController;
import smartalarm.mediator.SimulationManager;
import smartalarm.view.PhoneFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SimulationManager mediator = new SimulationManager();

            PhoneFrame frameA = new PhoneFrame("Phone A");
            PhoneFrame frameB = new PhoneFrame("Phone B");

            PhoneController controllerA = new PhoneController(frameA, mediator);
            PhoneController controllerB = new PhoneController(frameB, mediator);

            mediator.register("A", controllerA);
            mediator.register("B", controllerB);

            frameA.getFriendsPanel().addFriend("Phone B", e -> controllerA.sendRemoteAlarm("B"));
            frameB.getFriendsPanel().addFriend("Phone A", e -> controllerB.sendRemoteAlarm("A"));

            frameA.setLocation(100, 100);
            frameB.setLocation(560, 100);

            frameA.setVisible(true);
            frameB.setVisible(true);
        });
    }
}
