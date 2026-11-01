package transit.app;

import java.util.List;
import transit.model.Bus;
import transit.model.Network;
import transit.model.Route;
import transit.model.Station;
import transit.model.Stop;
import transit.model.TicketMachine;
import transit.model.Tram;

public final class SampleNetwork {

    private SampleNetwork() {
    }

    public static Network create() {
        Stop central = new Stop("S1", "Central Square", true, List.of(
                new Bus("B-101", 38.0, 220.0, 185_000.0, true, 90),
                new Station("ST-1", "Central Hub", 2, true)));
        Stop congress = new Stop("S2", "Congress Center", false, List.of(
                new Bus("B-102", 40.0, 200.0, 160_000.0, false, 90),
                new TicketMachine("TM-2", 42_000.0, false)));
        Stop airport = new Stop("S3", "Airport", true, List.of(
                new Tram("T-301", 250.0, 120.0, 210_000.0, true, 200),
                new Station("ST-3", "Airport Terminal", 1, false)));
        Stop railway = new Stop("S4", "Railway Station", true, List.of(
                new Tram("T-302", 260.0, 130.0, 230_000.0, false, 200),
                new TicketMachine("TM-4", 51_000.0, true)));
        Stop mall = new Stop("S5", "Mall", true, List.of(
                new Bus("B-201", 36.5, 180.0, 120_000.0, true, 80)));

        return new Network("N1", "Astana Transit", List.of(
                new Route("R1", "Central - Airport", List.of(central, congress, airport)),
                new Route("R2", "North Loop", List.of(railway, mall))));
    }
}
