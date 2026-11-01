package transit.app;

import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import transit.concurrent.ProducerRunner;
import transit.event.EventType;
import transit.listener.DelayAlertService;
import transit.listener.InspectionService;
import transit.listener.MaintenanceTicketDesk;
import transit.listener.RidershipDashboard;
import transit.listener.RidershipStatistics;
import transit.listener.StopDisplay;
import transit.model.Network;
import transit.model.Route;
import transit.model.Stop;
import transit.source.TicketGate;
import transit.source.VehicleArrivalFeed;
import transit.visitor.AccessibilityAuditVisitor;

public final class CompositionRoot {

    private CompositionRoot() {
    }

    public static TransitSystem assemble(SystemConfig config, Network network, Clock clock) {
        VehicleArrivalFeed arrivals = new VehicleArrivalFeed(clock);
        TicketGate ticketGate = new TicketGate(clock);

        Map<String, StopDisplay> stopDisplays = new LinkedHashMap<>();
        for (Route route : network.routes()) {
            for (Stop stop : route.stops()) {
                StopDisplay display = new StopDisplay(stop.id());
                arrivals.subscribe(EventType.ARRIVAL, display);
                arrivals.subscribe(EventType.DEPARTURE, display);
                stopDisplays.put(stop.id(), display);
            }
        }

        DelayAlertService delayAlerts = new DelayAlertService(config.delayThresholdSeconds(), clock);
        arrivals.subscribe(EventType.ARRIVAL, delayAlerts);

        InspectionService inspection = new InspectionService(network, new AccessibilityAuditVisitor(), clock);
        delayAlerts.subscribe(EventType.DELAY_ALERT, inspection);

        MaintenanceTicketDesk ticketDesk = new MaintenanceTicketDesk();
        inspection.subscribe(EventType.MAINTENANCE_TICKET, ticketDesk);

        RidershipStatistics ridership = new RidershipStatistics(config.ridershipBatchSize(), clock);
        ticketGate.subscribe(EventType.TICKET_VALIDATION, ridership);

        RidershipDashboard dashboard = new RidershipDashboard();
        ridership.subscribe(EventType.RIDERSHIP_SUMMARY, dashboard);

        ProducerRunner runner = new ProducerRunner(
                Executors.newFixedThreadPool(config.producerThreads()), config.shutdownTimeout());

        return new TransitSystem(network, arrivals, ticketGate, Map.copyOf(stopDisplays), delayAlerts,
                ridership, dashboard, inspection, ticketDesk, runner);
    }
}
