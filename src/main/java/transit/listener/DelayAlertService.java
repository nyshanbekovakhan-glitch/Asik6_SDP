package transit.listener;

import java.time.Clock;
import java.util.concurrent.atomic.LongAdder;
import transit.event.DelayAlertEvent;
import transit.event.VehicleEvent;
import transit.observer.BaseSubject;
import transit.observer.Observer;

public final class DelayAlertService extends BaseSubject<DelayAlertEvent> implements Observer<VehicleEvent> {

    private final int thresholdSeconds;
    private final Clock clock;
    private final LongAdder alertsRaised = new LongAdder();

    public DelayAlertService(int thresholdSeconds, Clock clock) {
        this.thresholdSeconds = thresholdSeconds;
        this.clock = clock;
    }

    @Override
    public void onEvent(VehicleEvent event) {
        if (event.delaySeconds() >= thresholdSeconds) {
            alertsRaised.increment();
            publish(new DelayAlertEvent(event.routeId(), event.stopId(), event.vehicleId(),
                    event.delaySeconds(), clock.instant()));
        }
    }

    public long alertsRaised() {
        return alertsRaised.sum();
    }
}
