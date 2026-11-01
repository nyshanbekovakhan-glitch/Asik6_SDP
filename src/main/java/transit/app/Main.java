package transit.app;

import java.time.Clock;
import java.util.List;
import java.util.concurrent.ExecutionException;
import transit.concurrent.ProducerTask;
import transit.event.EventType;
import transit.listener.PassengerApp;
import transit.visitor.AccessibilityAuditVisitor;
import transit.visitor.FuelUsageVisitor;
import transit.visitor.JsonExportVisitor;
import transit.visitor.TicketRevenueVisitor;

public final class Main {

    private static final int EVENTS_PER_PRODUCER = 1_000;

    private Main() {
    }

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        int threads = args.length > 0 ? Integer.parseInt(args[0]) : 4;
        TransitSystem system = CompositionRoot.assemble(
                SystemConfig.withThreads(threads), SampleNetwork.create(), Clock.systemUTC());

        PassengerApp passenger = new PassengerApp(system.arrivals(), "R1", "S2");
        system.arrivals().subscribe(EventType.ARRIVAL, passenger);

        List<ProducerTask> producers = List.of(
                new ProducerTask(EVENTS_PER_PRODUCER, i -> {
                    system.arrivals().reportArrival("B-102", "R1", "S2", i % 4 == 0 ? 400 : 30);
                    system.arrivals().reportDeparture("B-102", "R1", "S2", 0);
                }),
                new ProducerTask(EVENTS_PER_PRODUCER, i ->
                        system.arrivals().reportArrival("T-302", "R2", "S4", i % 10 == 0 ? 600 : 0)),
                new ProducerTask(EVENTS_PER_PRODUCER, i -> system.ticketGate().validate("R1", "S1", 250)),
                new ProducerTask(EVENTS_PER_PRODUCER, i -> system.ticketGate().validate("R2", "S4", 250)));

        system.runner().runAll(producers);

        System.out.println("== Observer side (" + threads + " producer threads) ==");
        System.out.println("Passenger app: " + passenger.notifications());
        System.out.println("Delay alerts raised: " + system.delayAlerts().alertsRaised());
        System.out.println("Maintenance tickets: " + system.ticketDesk().tickets().size());
        System.out.println("Passengers R1/R2: " + system.ridership().passengers("R1") + "/"
                + system.ridership().passengers("R2"));
        System.out.println("Dashboard R1: " + system.dashboard().latestPassengers("R1"));

        System.out.println("== Visitor side ==");
        System.out.println("Ticket revenue: " + system.network().accept(new TicketRevenueVisitor()));
        FuelUsageVisitor fuel = new FuelUsageVisitor(260.0, 35.0);
        system.network().accept(fuel);
        System.out.println("Fuel: " + fuel.totalLiters() + " L, " + fuel.totalKwh() + " kWh, cost by route "
                + fuel.costByRoute());
        System.out.println("Not accessible: " + system.network().accept(new AccessibilityAuditVisitor())
                .stream().map(node -> node.id()).toList());
        System.out.println(system.network().accept(new JsonExportVisitor()));
    }
}
