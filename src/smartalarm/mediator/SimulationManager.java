package smartalarm.mediator;

import smartalarm.controller.PhoneController;
import java.util.HashMap;
import java.util.Map;

public class SimulationManager {

    private final Map<String, PhoneController> byId   = new HashMap<>();
    private final Map<String, PhoneController> byName = new HashMap<>();

    public void register(String id, PhoneController controller) {
        byId.put(id, controller);
    }

    public void registerName(String name, PhoneController controller) {
        byName.put(name, controller);
    }

    public void unregisterName(String name) {
        byName.remove(name);
    }

    public PhoneController findByName(String name) {
        return byName.get(name);
    }

    public String getIdOf(PhoneController controller) {
        return byId.entrySet().stream()
                .filter(e -> e.getValue() == controller)
                .map(Map.Entry::getKey)
                .findFirst().orElse(null);
    }

    public void relay(String targetId, String fromName) {
        PhoneController target = byId.get(targetId);
        if (target != null) target.receiveRemoteAlarm(fromName);
    }

    public void routeWakeLimitExceeded(String fromName, String toName) {
        PhoneController to = byName.get(toName);
        if (to != null) to.receiveWakeLimitExceeded(fromName);
    }

    public void routeFriendRequest(String fromName, String toName) {
        PhoneController to = byName.get(toName);
        if (to != null) to.receiveFriendRequest(fromName);
    }

    public void routeFriendAccept(String acceptorName, String requesterName) {
        PhoneController to = byName.get(requesterName);
        if (to != null) to.friendRequestAccepted(acceptorName);
    }

    public void routeFriendDecline(String declinerName, String requesterName) {
        PhoneController to = byName.get(requesterName);
        if (to != null) to.friendRequestDenied(declinerName);
    }

    public void routeNameChange(String oldName, String newName) {
        for (PhoneController ctrl : byId.values()) {
            ctrl.receiveFriendNameChange(oldName, newName);
        }
    }
}
