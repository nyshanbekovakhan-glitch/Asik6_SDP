package transit.listener;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import transit.event.RidershipSummaryEvent;
import transit.observer.Observer;

public final class RidershipDashboard implements Observer<RidershipSummaryEvent> {

    private final Map<String, Long> latestByRoute = new ConcurrentHashMap<>();

    @Override
    public void onEvent(RidershipSummaryEvent summary) {

        latestByRoute.merge(summary.routeId(), summary.passengers(), Math::max);
    }

    public long latestPassengers(String routeId) {
        return latestByRoute.getOrDefault(routeId, 0L);
    }
}
