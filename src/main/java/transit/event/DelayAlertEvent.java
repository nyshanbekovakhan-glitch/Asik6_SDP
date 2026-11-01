package transit.event;

import java.time.Instant;
import java.util.Objects;

public record DelayAlertEvent(String routeId, String stopId, String vehicleId,
                              int delaySeconds, Instant timestamp) implements Event {

    public DelayAlertEvent {
        Objects.requireNonNull(routeId, "routeId");
        Objects.requireNonNull(stopId, "stopId");
        Objects.requireNonNull(vehicleId, "vehicleId");
        Objects.requireNonNull(timestamp, "timestamp");
    }

    @Override
    public EventType type() {
        return EventType.DELAY_ALERT;
    }
}
