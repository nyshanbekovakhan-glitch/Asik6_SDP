package transit;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import transit.model.Bus;
import transit.model.Network;
import transit.model.Route;
import transit.model.Station;
import transit.model.Stop;
import transit.model.TicketMachine;
import transit.model.Tram;

public final class TestData {

    public static final Clock CLOCK = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);

    private TestData() {
    }

    public static Network network() {
        Stop s1 = new Stop("S1", "Central", true, List.of(
                new Bus("B1", 10.0, 100.0, 1000.0, true, 50),
                new Tram("T1", 200.0, 50.0, 2000.0, false, 100)));
        Stop s2 = new Stop("S2", "Congress", false, List.of(
                new Station("ST1", "Hub", 2, false),
                new TicketMachine("TM1", 500.0, true)));
        Stop s3 = new Stop("S3", "Mall", true, List.of(
                new Bus("B2", 20.0, 50.0, 1500.0, false, 40)));
        return new Network("N1", "Net", List.of(
                new Route("R1", "Line 1", List.of(s1, s2)),
                new Route("R2", "Line 2", List.of(s3))));
    }
}
