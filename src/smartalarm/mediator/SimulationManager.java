package smartalarm.mediator;

import smartalarm.controller.PhoneController;

import java.util.HashMap;
import java.util.Map;

public class SimulationManager {

    private final Map<String, PhoneController> phones = new HashMap<>();

    public void register(String id, PhoneController controller) {
        phones.put(id, controller);
    }

    public void relay(String targetId) {
        PhoneController target = phones.get(targetId);
        if (target != null) {
            target.receiveRemoteAlarm();
        }
    }
}
