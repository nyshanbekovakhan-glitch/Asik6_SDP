package transit.app;

import java.util.Map;
import transit.concurrent.ProducerRunner;
import transit.listener.DelayAlertService;
import transit.listener.InspectionService;
import transit.listener.MaintenanceTicketDesk;
import transit.listener.RidershipDashboard;
import transit.listener.RidershipStatistics;
import transit.listener.StopDisplay;
import transit.model.Network;
import transit.source.TicketGate;
import transit.source.VehicleArrivalFeed;

public record TransitSystem(
        Network network,
        VehicleArrivalFeed arrivals,
        TicketGate ticketGate,
        Map<String, StopDisplay> stopDisplays,
        DelayAlertService delayAlerts,
        RidershipStatistics ridership,
        RidershipDashboard dashboard,
        InspectionService inspection,
        MaintenanceTicketDesk ticketDesk,
        ProducerRunner runner) {
}
