package transit.listener;

import java.time.Clock;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import transit.event.RidershipSummaryEvent;
import transit.event.ValidationEvent;
import transit.observer.BaseSubject;
import transit.observer.Observer;

public final class RidershipStatistics extends BaseSubject<RidershipSummaryEvent>
        implements Observer<ValidationEvent> {

    private final int batchSize;
    private final Clock clock;
    private final Map<String, AtomicLong> passengersByRoute = new ConcurrentHashMap<>();

    public RidershipStatistics(int batchSize, Clock clock) {
        this.batchSize = batchSize;
        this.clock = clock;
    }

    @Override
    public void onEvent(ValidationEvent event) {
        long passengers = passengersByRoute
                .computeIfAbsent(event.routeId(), route -> new AtomicLong())
                .incrementAndGet();

        if (passengers % batchSize == 0) {
            publish(new RidershipSummaryEvent(event.routeId(), passengers, clock.instant()));
        }
    }

    public long passengers(String routeId) {
        AtomicLong counter = passengersByRoute.get(routeId);
        return counter == null ? 0 : counter.get();
    }

    public long totalPassengers() {
        return passengersByRoute.values().stream().mapToLong(AtomicLong::get).sum();
    }
}
