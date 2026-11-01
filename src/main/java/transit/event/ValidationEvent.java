package transit.event;

import java.time.Instant;
import java.util.Objects;

public record ValidationEvent(String routeId, String stopId, int fareTenge, Instant timestamp) implements Event {

    public ValidationEvent {
        Objects.requireNonNull(routeId, "routeId");
        Objects.requireNonNull(stopId, "stopId");
        Objects.requireNonNull(timestamp, "timestamp");
        if (fareTenge < 0) {
            throw new IllegalArgumentException("fare must not be negative");
        }
    }

    @Override
    public EventType type() {
        return EventType.TICKET_VALIDATION;
    }
}
