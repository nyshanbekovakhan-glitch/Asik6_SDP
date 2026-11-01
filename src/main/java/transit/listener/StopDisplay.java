package transit.listener;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import transit.event.VehicleEvent;
import transit.observer.Observer;

public final class StopDisplay implements Observer<VehicleEvent> {

    private final String stopId;
    private final Map<String, VehicleEvent> atStop = new ConcurrentHashMap<>();

    public StopDisplay(String stopId) {
        this.stopId = stopId;
    }

    @Override
    public void onEvent(VehicleEvent event) {
        if (!stopId.equals(event.stopId())) {
            return;
        }
        switch (event.type()) {
            case ARRIVAL -> atStop.put(event.vehicleId(), event);
            case DEPARTURE -> atStop.remove(event.vehicleId());
            default -> {  }
        }
    }

    public Set<String> vehiclesAtStop() {
        return new TreeSet<>(atStop.keySet());
    }

    public List<String> boardLines() {
        List<String> lines = new ArrayList<>();
        atStop.values().stream()
                .sorted(Comparator.comparing(VehicleEvent::vehicleId))
                .forEach(e -> lines.add(e.routeId() + " | " + e.vehicleId() + " | delay " + e.delaySeconds() + "s"));
        return lines;
    }
}
