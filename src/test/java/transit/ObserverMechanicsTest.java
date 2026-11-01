package transit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import transit.event.EventType;
import transit.event.VehicleEvent;
import transit.observer.Observer;
import transit.source.VehicleArrivalFeed;

class ObserverMechanicsTest {

    private VehicleArrivalFeed feed;

    @BeforeEach
    void setUp() {
        feed = new VehicleArrivalFeed(TestData.CLOCK);
    }

    @Test
    void subscriberReceivesImmutableEventWithAllData() {
        List<VehicleEvent> received = new ArrayList<>();
        feed.subscribe(EventType.ARRIVAL, received::add);

        feed.reportArrival("B1", "R1", "S1", 42);

        assertEquals(1, received.size());
        assertEquals(new VehicleEvent(EventType.ARRIVAL, "B1", "R1", "S1", 42, TestData.CLOCK.instant()),
                received.get(0));
    }

    @Test
    void listenerOnlyGetsEventTypesItSubscribedTo() {
        List<VehicleEvent> received = new ArrayList<>();
        feed.subscribe(EventType.DEPARTURE, received::add);

        feed.reportArrival("B1", "R1", "S1", 0);
        feed.reportDeparture("B1", "R1", "S1", 0);

        assertEquals(1, received.size());
        assertEquals(EventType.DEPARTURE, received.get(0).type());
    }

    @Test
    void unsubscribedListenerGetsNothingMore() {
        List<VehicleEvent> received = new ArrayList<>();
        Observer<VehicleEvent> listener = received::add;
        feed.subscribe(EventType.ARRIVAL, listener);
        feed.reportArrival("B1", "R1", "S1", 0);

        feed.unsubscribe(EventType.ARRIVAL, listener);
        feed.reportArrival("B1", "R1", "S1", 0);

        assertEquals(1, received.size());
    }

    @Test
    void listenerCanUnsubscribeItselfInsideItsHandlerWithoutDisturbingOthers() {
        List<String> log = new ArrayList<>();
        Observer<VehicleEvent>[] selfRemoving = new Observer[1];
        selfRemoving[0] = e -> {
            log.add("self");
            feed.unsubscribe(EventType.ARRIVAL, selfRemoving[0]);
        };
        feed.subscribe(EventType.ARRIVAL, selfRemoving[0]);
        feed.subscribe(EventType.ARRIVAL, e -> log.add("other"));

        feed.reportArrival("B1", "R1", "S1", 0);
        feed.reportArrival("B1", "R1", "S1", 0);

        assertEquals(List.of("self", "other", "other"), log);
    }

    @Test
    void listenerJoiningInsideAHandlerStartsWithTheNextEvent() {
        List<String> late = new ArrayList<>();
        Observer<VehicleEvent> lateListener = e -> late.add(e.vehicleId());
        feed.subscribe(EventType.ARRIVAL, e -> feed.subscribe(EventType.ARRIVAL, lateListener));

        feed.reportArrival("first", "R1", "S1", 0);
        feed.reportArrival("second", "R1", "S1", 0);

        assertEquals(List.of("second"), late);
    }

    @Test
    void failingListenerDoesNotStopOtherListeners() {
        List<String> received = new ArrayList<>();
        feed.subscribe(EventType.ARRIVAL, e -> {
            throw new IllegalStateException("broken listener");
        });
        feed.subscribe(EventType.ARRIVAL, e -> received.add(e.vehicleId()));

        feed.reportArrival("B1", "R1", "S1", 0);

        assertEquals(List.of("B1"), received);
    }

    @Test
    void sameListenerIsRegisteredOnlyOncePerType() {
        List<String> received = new ArrayList<>();
        Observer<VehicleEvent> listener = e -> received.add(e.vehicleId());
        feed.subscribe(EventType.ARRIVAL, listener);
        feed.subscribe(EventType.ARRIVAL, listener);

        feed.reportArrival("B1", "R1", "S1", 0);

        assertEquals(1, received.size());
    }

    @Test
    void vehicleEventRejectsNonVehicleTypes() {
        assertThrows(IllegalArgumentException.class, () ->
                new VehicleEvent(EventType.DELAY_ALERT, "B1", "R1", "S1", 0, TestData.CLOCK.instant()));
    }
}
