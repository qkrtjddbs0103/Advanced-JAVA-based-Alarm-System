package smartalarm;

import smartalarm.controller.PhoneController;
import smartalarm.mediator.SimulationManager;
import smartalarm.view.PhoneFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        System.setProperty("sun.java2d.uiScale.enabled", "true");
        SwingUtilities.invokeLater(() -> {
            SimulationManager mediator = new SimulationManager();

            PhoneFrame frameA = new PhoneFrame("Phone A");
            PhoneFrame frameB = new PhoneFrame("Phone B");

            PhoneController controllerA = new PhoneController(frameA, mediator);
            PhoneController controllerB = new PhoneController(frameB, mediator);

            mediator.register("A", controllerA);
            mediator.register("B", controllerB);

            controllerA.init("A", "Phone A");
            controllerB.init("B", "Phone B");

            frameA.setLocation(100, 100);
            frameB.setLocation(560, 100);

            frameA.setVisible(true);
            frameB.setVisible(true);
        });
    }
}
