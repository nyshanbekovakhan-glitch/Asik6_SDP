package transit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import transit.app.CompositionRoot;
import transit.app.SystemConfig;
import transit.app.TransitSystem;
import transit.concurrent.ProducerRunner;
import transit.concurrent.ProducerTask;
import transit.event.EventType;
import transit.event.VehicleEvent;
import transit.observer.Observer;
import transit.source.VehicleArrivalFeed;

class ConcurrencyTest {

    private static final int THREADS = 4;
    private static final int EVENTS = 1_000;

    @Test
    void stressTestFourThreadsTimesThousandValidationsGivesExactCounts() throws Exception {
        TransitSystem system = CompositionRoot.assemble(
                new SystemConfig(THREADS, 300, 100, Duration.ofSeconds(30)), TestData.network(), TestData.CLOCK);
        AtomicInteger summaries = new AtomicInteger();
        system.ridership().subscribe(EventType.RIDERSHIP_SUMMARY, e -> summaries.incrementAndGet());

        system.runner().runAll(producers(THREADS, i -> system.ticketGate().validate("R1", "S1", 250)));

        assertEquals(4_000, system.ridership().totalPassengers());
        assertEquals(4_000, system.dashboard().latestPassengers("R1"));
        assertEquals(40, summaries.get());
    }

    @Test
    void stressTestFourThreadsTimesThousandArrivalsGivesExactAlertsAndTickets() throws Exception {
        TransitSystem system = CompositionRoot.assemble(
                new SystemConfig(THREADS, 300, 100, Duration.ofSeconds(30)), TestData.network(), TestData.CLOCK);

        system.runner().runAll(producers(THREADS, i ->
                system.arrivals().reportArrival("V" + (i % 4), "R1", "S2", i % 4 == 0 ? 600 : 10)));

        assertEquals(1_000, system.delayAlerts().alertsRaised());
        assertEquals(1_000, system.ticketDesk().tickets().size());
        assertEquals(4, system.stopDisplays().get("S2").vehiclesAtStop().size());
    }

    @Test
    void subscribingAndUnsubscribingWhilePublishingNeverLosesEventsForStableListeners() throws Exception {
        VehicleArrivalFeed feed = new VehicleArrivalFeed(TestData.CLOCK);
        AtomicInteger stable = new AtomicInteger();
        feed.subscribe(EventType.ARRIVAL, e -> stable.incrementAndGet());
        Observer<VehicleEvent> churning = e -> { };
        AtomicBoolean running = new AtomicBoolean(true);
        Thread churner = new Thread(() -> {
            while (running.get()) {
                feed.subscribe(EventType.ARRIVAL, churning);
                feed.unsubscribe(EventType.ARRIVAL, churning);
            }
        });
        churner.start();

        new ProducerRunner(Executors.newFixedThreadPool(THREADS), Duration.ofSeconds(30))
                .runAll(producers(THREADS, i -> feed.reportArrival("B", "R1", "S1", 0)));
        running.set(false);
        churner.join(5_000);

        assertEquals(4_000, stable.get());
        assertTrue(!churner.isAlive());
    }

    @Test
    void listenerUnsubscribingItselfWhileThreadsPublishIsSafe() throws Exception {
        VehicleArrivalFeed feed = new VehicleArrivalFeed(TestData.CLOCK);
        AtomicInteger stable = new AtomicInteger();
        AtomicInteger selfRemoving = new AtomicInteger();
        feed.subscribe(EventType.ARRIVAL, e -> stable.incrementAndGet());
        Observer<VehicleEvent>[] once = new Observer[1];
        once[0] = e -> {
            selfRemoving.incrementAndGet();
            feed.unsubscribe(EventType.ARRIVAL, once[0]);
        };
        feed.subscribe(EventType.ARRIVAL, once[0]);

        new ProducerRunner(Executors.newFixedThreadPool(THREADS), Duration.ofSeconds(30))
                .runAll(producers(THREADS, i -> feed.reportArrival("B", "R1", "S1", 0)));

        assertEquals(4_000, stable.get());
        assertTrue(selfRemoving.get() >= 1 && selfRemoving.get() <= THREADS,
                "a listener may see at most one event per racing thread before it is gone");
    }

    @Test
    void gracefulShutdownWaitsForAllTasksAndTerminatesThePool() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(3);
        AtomicInteger done = new AtomicInteger();

        new ProducerRunner(pool, Duration.ofSeconds(10)).runAll(producers(3, i -> {
            if (i == 0) {
                sleep(50);
            }
            done.incrementAndGet();
        }));

        assertEquals(3_000, done.get());
        assertTrue(pool.isTerminated());
    }

    @Test
    void failingProducerIsReportedAndPoolStillTerminates() {
        ExecutorService pool = Executors.newFixedThreadPool(3);
        List<ProducerTask> tasks = List.of(
                new ProducerTask(1, i -> { }),
                new ProducerTask(1, i -> { throw new IllegalStateException("boom"); }),
                new ProducerTask(1, i -> { }));

        ExecutionException failure = assertThrows(ExecutionException.class,
                () -> new ProducerRunner(pool, Duration.ofSeconds(10)).runAll(tasks));

        assertEquals("boom", failure.getCause().getMessage());
        assertTrue(pool.isTerminated());
    }

    @Test
    void hangingProducerIsInterruptedAfterTimeoutSoNoThreadHangs() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(3);
        List<ProducerTask> tasks = List.of(new ProducerTask(1, i -> sleep(10_000)));

        assertThrows(IllegalStateException.class,
                () -> new ProducerRunner(pool, Duration.ofMillis(200)).runAll(tasks));

        assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS));
    }

    private static List<ProducerTask> producers(int count, java.util.function.IntConsumer emitter) {
        return java.util.stream.IntStream.range(0, count).mapToObj(n -> new ProducerTask(EVENTS, emitter)).toList();
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
