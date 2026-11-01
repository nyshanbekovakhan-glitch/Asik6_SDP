package transit.event;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public record MaintenanceTicketEvent(String routeId, String stopId, List<String> elementIds,
                                     String reason, Instant timestamp) implements Event {

    public MaintenanceTicketEvent {
        Objects.requireNonNull(routeId, "routeId");
        Objects.requireNonNull(stopId, "stopId");
        Objects.requireNonNull(reason, "reason");
        Objects.requireNonNull(timestamp, "timestamp");
        elementIds = List.copyOf(elementIds);
    }

    @Override
    public EventType type() {
        return EventType.MAINTENANCE_TICKET;
    }
}
