package transit.event;

import java.time.Instant;
import java.util.Objects;

public record VehicleEvent(EventType type, String vehicleId, String routeId, String stopId,
                           int delaySeconds, Instant timestamp) implements Event {

    public VehicleEvent {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(vehicleId, "vehicleId");
        Objects.requireNonNull(routeId, "routeId");
        Objects.requireNonNull(stopId, "stopId");
        Objects.requireNonNull(timestamp, "timestamp");
        if (type != EventType.ARRIVAL && type != EventType.DEPARTURE) {
            throw new IllegalArgumentException("VehicleEvent must be ARRIVAL or DEPARTURE, got " + type);
        }
    }

    public static VehicleEvent arrival(String vehicleId, String routeId, String stopId,
                                       int delaySeconds, Instant timestamp) {
        return new VehicleEvent(EventType.ARRIVAL, vehicleId, routeId, stopId, delaySeconds, timestamp);
    }

    public static VehicleEvent departure(String vehicleId, String routeId, String stopId,
                                         int delaySeconds, Instant timestamp) {
        return new VehicleEvent(EventType.DEPARTURE, vehicleId, routeId, stopId, delaySeconds, timestamp);
    }
}
