package transit.source;

import java.time.Clock;
import transit.event.ValidationEvent;
import transit.observer.BaseSubject;

public final class TicketGate extends BaseSubject<ValidationEvent> {

    private final Clock clock;

    public TicketGate(Clock clock) {
        this.clock = clock;
    }

    public void validate(String routeId, String stopId, int fareTenge) {
        publish(new ValidationEvent(routeId, stopId, fareTenge, clock.instant()));
    }
}
