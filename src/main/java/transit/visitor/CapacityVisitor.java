package transit.visitor;

import java.util.List;
import transit.model.Bus;
import transit.model.Network;
import transit.model.Route;
import transit.model.Station;
import transit.model.Stop;
import transit.model.TicketMachine;
import transit.model.Tram;
import transit.model.Trolleybus;
import transit.model.TransitNode;

public final class CapacityVisitor implements Visitor<Integer> {

    @Override
    public Integer visitNetwork(Network network) {
        return sum(network.routes());
    }

    @Override
    public Integer visitRoute(Route route) {
        return sum(route.stops());
    }

    @Override
    public Integer visitStop(Stop stop) {
        return sum(stop.children());
    }

    @Override
    public Integer visitBus(Bus bus) {
        return bus.capacity();
    }

    @Override
    public Integer visitTram(Tram tram) {
        return tram.capacity();
    }

    @Override
    public Integer visitStation(Station station) {
        return 0;
    }

    @Override
    public Integer visitTicketMachine(TicketMachine machine) {
        return 0;
    }

    @Override
    public Integer visitTrolleybus(Trolleybus trolleybus) {
        return trolleybus.capacity();
    }

    private int sum(List<? extends TransitNode> nodes) {
        int total = 0;
        for (TransitNode node : nodes) {
            total += node.accept(this);
        }
        return total;
    }
}
