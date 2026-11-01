package transit.listener;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import transit.event.DelayAlertEvent;
import transit.event.MaintenanceTicketEvent;
import transit.model.Network;
import transit.model.Route;
import transit.model.Stop;
import transit.model.TransitNode;
import transit.observer.BaseSubject;
import transit.observer.Observer;
import transit.visitor.Visitor;

public final class InspectionService extends BaseSubject<MaintenanceTicketEvent>
        implements Observer<DelayAlertEvent> {

    private final Network network;
    private final Visitor<List<TransitNode>> audit;
    private final Clock clock;

    public InspectionService(Network network, Visitor<List<TransitNode>> audit, Clock clock) {
        this.network = network;
        this.audit = audit;
        this.clock = clock;
    }

    @Override
    public void onEvent(DelayAlertEvent alert) {
        findStop(alert.routeId(), alert.stopId()).ifPresent(stop -> inspect(alert, stop));
    }

    private void inspect(DelayAlertEvent alert, Stop stop) {
        List<TransitNode> failing = stop.accept(audit);
        if (failing.isEmpty()) {
            return;
        }
        List<String> ids = failing.stream().map(TransitNode::id).toList();
        publish(new MaintenanceTicketEvent(alert.routeId(), alert.stopId(), ids,
                "Delay of " + alert.delaySeconds() + "s, accessibility audit failed", clock.instant()));
    }

    private Optional<Stop> findStop(String routeId, String stopId) {
        return network.routes().stream()
                .filter(route -> route.id().equals(routeId))
                .map(Route::stops)
                .flatMap(List::stream)
                .filter(stop -> stop.id().equals(stopId))
                .findFirst();
    }
}
