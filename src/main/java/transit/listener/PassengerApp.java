package transit.listener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import transit.event.EventType;
import transit.event.VehicleEvent;
import transit.observer.Observer;
import transit.observer.Subject;

public final class PassengerApp implements Observer<VehicleEvent> {

    private final Subject<VehicleEvent> feed;
    private final String routeId;
    private final String stopId;
    private final AtomicBoolean notified = new AtomicBoolean(false);
    private final List<String> notifications = new CopyOnWriteArrayList<>();

    public PassengerApp(Subject<VehicleEvent> feed, String routeId, String stopId) {
        this.feed = feed;
        this.routeId = routeId;
        this.stopId = stopId;
    }

    @Override
    public void onEvent(VehicleEvent event) {
        boolean mine = routeId.equals(event.routeId()) && stopId.equals(event.stopId());
        if (mine && notified.compareAndSet(false, true)) {
            notifications.add("Vehicle " + event.vehicleId() + " of route " + routeId
                    + " arrived at " + stopId + " (delay " + event.delaySeconds() + "s)");
            feed.unsubscribe(EventType.ARRIVAL, this);
        }
    }

    public List<String> notifications() {
        return List.copyOf(notifications);
    }
}
