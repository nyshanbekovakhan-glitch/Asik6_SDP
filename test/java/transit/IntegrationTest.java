package transit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import transit.app.CompositionRoot;
import transit.app.SystemConfig;
import transit.app.TransitSystem;
import transit.event.DelayAlertEvent;
import transit.event.EventType;
import transit.event.MaintenanceTicketEvent;
import transit.listener.InspectionService;
import transit.model.Network;
import transit.model.Route;
import transit.model.Stop;
import transit.model.Bus;
import transit.visitor.AccessibilityAuditVisitor;

class IntegrationTest {

    private final TransitSystem system =
            CompositionRoot.assemble(SystemConfig.withThreads(1), TestData.network(), TestData.CLOCK);

    @Test
    void lateArrivalProducesTicketForThatStopsSubtreeOnly() {
        system.arrivals().reportArrival("V1", "R1", "S2", 600);

        List<MaintenanceTicketEvent> tickets = system.ticketDesk().tickets();
        assertEquals(1, tickets.size());
        assertEquals("R1", tickets.get(0).routeId());
        assertEquals("S2", tickets.get(0).stopId());

        assertEquals(List.of("S2", "ST1"), tickets.get(0).elementIds());
    }

    @Test
    void eachAlertInspectsOnlyItsOwnStop() {
        system.arrivals().reportArrival("V1", "R1", "S1", 600);
        system.arrivals().reportArrival("V2", "R2", "S3", 600);

        List<MaintenanceTicketEvent> tickets = system.ticketDesk().tickets();
        assertEquals(List.of("T1"), tickets.get(0).elementIds());
        assertEquals(List.of("B2"), tickets.get(1).elementIds());
    }

    @Test
    void noTicketBelowThresholdOrForUnknownStop() {
        system.arrivals().reportArrival("V1", "R1", "S2", 299);
        system.arrivals().reportArrival("V2", "R1", "S99", 600);

        assertTrue(system.ticketDesk().tickets().isEmpty());
    }

    @Test
    void noTicketWhenInspectedSubtreeIsFullyAccessible() {
        Network accessible = new Network("N", "N", List.of(new Route("R", "R", List.of(
                new Stop("S", "S", true, List.of(new Bus("B", 1.0, 1.0, 1.0, true, 10)))))));
        InspectionService inspection =
                new InspectionService(accessible, new AccessibilityAuditVisitor(), TestData.CLOCK);
        List<MaintenanceTicketEvent> tickets = new ArrayList<>();
        inspection.subscribe(EventType.MAINTENANCE_TICKET, tickets::add);

        inspection.onEvent(new DelayAlertEvent("R", "S", "B", 900, TestData.CLOCK.instant()));

        assertTrue(tickets.isEmpty());
    }
}
