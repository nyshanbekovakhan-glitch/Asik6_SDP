package transit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import transit.event.DelayAlertEvent;
import transit.event.EventType;
import transit.event.RidershipSummaryEvent;
import transit.listener.DelayAlertService;
import transit.listener.PassengerApp;
import transit.listener.RidershipDashboard;
import transit.listener.RidershipStatistics;
import transit.listener.StopDisplay;
import transit.source.TicketGate;
import transit.source.VehicleArrivalFeed;

class ListenerTest {

    @Test
    void stopDisplayShowsVehiclesCurrentlyAtItsOwnStopOnly() {
        VehicleArrivalFeed feed = new VehicleArrivalFeed(TestData.CLOCK);
        StopDisplay display = new StopDisplay("S1");
        feed.subscribe(EventType.ARRIVAL, display);
        feed.subscribe(EventType.DEPARTURE, display);

        feed.reportArrival("B1", "R1", "S1", 30);
        feed.reportArrival("B2", "R2", "S1", 0);
        feed.reportArrival("B3", "R1", "S2", 0);
        feed.reportDeparture("B2", "R2", "S1", 0);

        assertEquals(Set.of("B1"), display.vehiclesAtStop());
        assertEquals(List.of("R1 | B1 | delay 30s"), display.boardLines());
    }

    @Test
    void passengerAppNotifiesOnceAndUnsubscribesItself() {
        VehicleArrivalFeed feed = new VehicleArrivalFeed(TestData.CLOCK);
        PassengerApp app = new PassengerApp(feed, "R1", "S2");
        feed.subscribe(EventType.ARRIVAL, app);

        feed.reportArrival("B9", "R1", "S1", 0);
        feed.reportArrival("B8", "R2", "S2", 0);
        assertTrue(app.notifications().isEmpty());

        feed.reportArrival("B1", "R1", "S2", 10);
        feed.reportArrival("B2", "R1", "S2", 20);

        assertEquals(List.of("Vehicle B1 of route R1 arrived at S2 (delay 10s)"), app.notifications());
    }

    @Test
    void delayAlertIsRaisedAtThresholdButNotBelow() {
        VehicleArrivalFeed feed = new VehicleArrivalFeed(TestData.CLOCK);
        DelayAlertService service = new DelayAlertService(300, TestData.CLOCK);
        feed.subscribe(EventType.ARRIVAL, service);
        List<DelayAlertEvent> alerts = new ArrayList<>();
        service.subscribe(EventType.DELAY_ALERT, alerts::add);

        feed.reportArrival("B1", "R1", "S1", 299);
        feed.reportArrival("B2", "R1", "S1", 300);
        feed.reportArrival("B3", "R2", "S3", 900);

        assertEquals(2, alerts.size());
        assertEquals("B2", alerts.get(0).vehicleId());
        assertEquals(900, alerts.get(1).delaySeconds());
        assertEquals(2, service.alertsRaised());
    }

    @Test
    void ridershipStatisticsRepublishesAggregatedSummaryEveryBatchPerRoute() {
        TicketGate gate = new TicketGate(TestData.CLOCK);
        RidershipStatistics stats = new RidershipStatistics(3, TestData.CLOCK);
        gate.subscribe(EventType.TICKET_VALIDATION, stats);
        List<RidershipSummaryEvent> summaries = new ArrayList<>();
        stats.subscribe(EventType.RIDERSHIP_SUMMARY, summaries::add);

        for (int i = 0; i < 7; i++) {
            gate.validate("R1", "S1", 250);
        }
        gate.validate("R2", "S3", 250);
        gate.validate("R2", "S3", 250);

        assertEquals(7, stats.passengers("R1"));
        assertEquals(2, stats.passengers("R2"));
        assertEquals(9, stats.totalPassengers());
        assertEquals(List.of(3L, 6L), summaries.stream().map(RidershipSummaryEvent::passengers).toList());
    }

    @Test
    void dashboardKeepsTheLargestSummaryPerRoute() {
        RidershipDashboard dashboard = new RidershipDashboard();
        dashboard.onEvent(new RidershipSummaryEvent("R1", 200, TestData.CLOCK.instant()));
        dashboard.onEvent(new RidershipSummaryEvent("R1", 100, TestData.CLOCK.instant()));

        assertEquals(200, dashboard.latestPassengers("R1"));
        assertEquals(0, dashboard.latestPassengers("R2"));
    }
}
