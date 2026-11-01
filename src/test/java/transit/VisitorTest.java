package transit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import transit.model.Bus;
import transit.model.Network;
import transit.model.Route;
import transit.model.Station;
import transit.model.Stop;
import transit.model.TicketMachine;
import transit.model.TransitNode;
import transit.visitor.AccessibilityAuditVisitor;
import transit.visitor.FuelUsageVisitor;
import transit.visitor.JsonExportVisitor;
import transit.visitor.TicketRevenueVisitor;

class VisitorTest {

    private final Network network = TestData.network();

    @Test
    void ticketRevenueOfWholeNetworkMatchesHandCalculation() {

        assertEquals(5000.0, network.accept(new TicketRevenueVisitor()));
    }

    @Test
    void ticketRevenueWorksOnAnySubtree() {
        TicketRevenueVisitor visitor = new TicketRevenueVisitor();
        assertEquals(3500.0, network.routes().get(0).accept(visitor));
        assertEquals(3000.0, network.routes().get(0).stops().get(0).accept(visitor));
        assertEquals(500.0, network.routes().get(0).stops().get(1).accept(visitor));
        assertEquals(0.0, new Station("X", "X", 1, true).accept(visitor));
    }

    @Test
    void fuelVisitorAccumulatesLitersKwhAndCostPerRoute() {
        FuelUsageVisitor fuel = new FuelUsageVisitor(100.0, 10.0);
        network.accept(fuel);

        assertEquals(20.0, fuel.totalLiters());
        assertEquals(100.0, fuel.totalKwh());
        assertEquals(Map.of("R1", 2000.0, "R2", 1000.0), fuel.costByRoute());
        assertEquals(3000.0, fuel.totalCost());
    }

    @Test
    void fuelVisitorStateIsPerInstance() {
        FuelUsageVisitor first = new FuelUsageVisitor(100.0, 10.0);
        network.accept(first);
        FuelUsageVisitor second = new FuelUsageVisitor(100.0, 10.0);

        assertEquals(0.0, second.totalCost());
        assertEquals(20.0, first.totalLiters());
    }

    @Test
    void accessibilityAuditReturnsExactlyTheFailingElementsInTraversalOrder() {
        List<TransitNode> failing = network.accept(new AccessibilityAuditVisitor());

        assertEquals(List.of("T1", "S2", "ST1", "B2"), failing.stream().map(TransitNode::id).toList());
    }

    @Test
    void accessibilityAuditOfOneStopDoesNotLookAtOtherStops() {
        Stop s1 = network.routes().get(0).stops().get(0);

        assertEquals(List.of("T1"), s1.accept(new AccessibilityAuditVisitor()).stream().map(TransitNode::id).toList());
    }

    @Test
    void jsonExportNestsEveryLevelCorrectly() {
        Network tiny = new Network("N", "Net", List.of(new Route("R", "Route", List.of(
                new Stop("S", "Stop", true, List.of(new Bus("B", 10.0, 100.0, 1000.0, true, 50)))))));

        String expected = """
                {
                  "type": "network",
                  "id": "N",
                  "name": "Net",
                  "routes": [
                    {
                      "type": "route",
                      "id": "R",
                      "name": "Route",
                      "stops": [
                        {
                          "type": "stop",
                          "id": "S",
                          "name": "Stop",
                          "stepFreePlatform": true,
                          "children": [
                            {
                              "type": "bus",
                              "id": "B",
                              "litersPer100Km": 10.0,
                              "dailyKm": 100.0,
                              "dailyTicketRevenue": 1000.0,
                              "lowFloor": true,
                              "capacity": 50
                            }
                          ]
                        }
                      ]
                    }
                  ]
                }""";
        assertEquals(expected, tiny.accept(new JsonExportVisitor()));
    }

    @Test
    void jsonExportEscapesSpecialCharactersAndHandlesEmptyContainers() {
        String json = new Stop("S", "Say \"hi\"\n\\", true, List.of()).accept(new JsonExportVisitor());

        assertTrue(json.contains("\"name\": \"Say \\\"hi\\\"\\n\\\\\""));
        assertTrue(json.contains("\"children\": []"));
    }

    @Test
    void doubleDispatchPicksTheMethodOfTheRuntimeElementType() {
        JsonExportVisitor json = new JsonExportVisitor();
        TransitNode node = new TicketMachine("TM", 1.0, true);

        assertTrue(node.accept(json).contains("\"type\": \"ticketMachine\""));
        assertTrue(new Station("ST", "S", 1, true).accept(json).contains("\"type\": \"station\""));
    }

    @Test
    void treeIsImmutable() {
        Stop stop = network.routes().get(0).stops().get(0);

        assertThrows(UnsupportedOperationException.class, () -> stop.children().add(new Station("X", "X", 1, true)));
        assertThrows(UnsupportedOperationException.class, () -> network.routes().clear());
    }
}
