package transit.visitor;

import transit.model.Bus;
import transit.model.Network;
import transit.model.Route;
import transit.model.Station;
import transit.model.Stop;
import transit.model.TicketMachine;
import transit.model.Tram;
import transit.model.Trolleybus;

public interface Visitor<R> {

    R visitNetwork(Network network);

    R visitRoute(Route route);

    R visitStop(Stop stop);

    R visitBus(Bus bus);

    R visitTram(Tram tram);

    R visitStation(Station station);

    R visitTicketMachine(TicketMachine machine);

    R visitTrolleybus(Trolleybus trolleybus);
}
