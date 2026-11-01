package transit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import transit.model.Stop;
import transit.model.Trolleybus;
import transit.model.TransitNode;
import transit.visitor.AccessibilityAuditVisitor;
import transit.visitor.CapacityVisitor;
import transit.visitor.FuelUsageVisitor;
import transit.visitor.JsonExportVisitor;
import transit.visitor.TicketRevenueVisitor;

class TrolleybusVisitorTest {

    private final Stop stop = new Stop("S", "Stop", true,
            List.of(new Trolleybus("TB1", 150.0, 80.0, 900.0, false, 70)));

    @Test
    void everyVisitorKnowsTheNewElement() {
        assertEquals(900.0, stop.accept(new TicketRevenueVisitor()));
        assertEquals(70, stop.accept(new CapacityVisitor()));
        assertEquals(List.of("TB1"), stop.accept(new AccessibilityAuditVisitor()).stream().map(TransitNode::id).toList());
        assertTrue(stop.accept(new JsonExportVisitor()).contains("\"type\": \"trolleybus\""));

        FuelUsageVisitor fuel = new FuelUsageVisitor(100.0, 10.0);
        stop.accept(fuel);
        assertEquals(120.0, fuel.totalKwh());
        assertEquals(1200.0, fuel.totalCost());
    }
}
