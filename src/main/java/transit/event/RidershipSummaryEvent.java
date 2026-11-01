package transit.event;

import java.time.Instant;
import java.util.Objects;

public record RidershipSummaryEvent(String routeId, long passengers, Instant timestamp) implements Event {

    public RidershipSummaryEvent {
        Objects.requireNonNull(routeId, "routeId");
        Objects.requireNonNull(timestamp, "timestamp");
    }

    @Override
    public EventType type() {
        return EventType.RIDERSHIP_SUMMARY;
    }
}
