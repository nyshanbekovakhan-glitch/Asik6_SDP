package transit.source;

import java.time.Clock;
import transit.event.VehicleEvent;
import transit.observer.BaseSubject;

public final class VehicleArrivalFeed extends BaseSubject<VehicleEvent> {

    private final Clock clock;

    public VehicleArrivalFeed(Clock clock) {
        this.clock = clock;
    }

    public void reportArrival(String vehicleId, String routeId, String stopId, int delaySeconds) {
        publish(VehicleEvent.arrival(vehicleId, routeId, stopId, delaySeconds, clock.instant()));
    }

    public void reportDeparture(String vehicleId, String routeId, String stopId, int delaySeconds) {
        publish(VehicleEvent.departure(vehicleId, routeId, stopId, delaySeconds, clock.instant()));
    }
}
