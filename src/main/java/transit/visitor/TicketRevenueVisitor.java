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

public final class TicketRevenueVisitor implements Visitor<Double> {

    @Override
    public Double visitNetwork(Network network) {
        return sum(network.routes());
    }

    @Override
    public Double visitRoute(Route route) {
        return sum(route.stops());
    }

    @Override
    public Double visitStop(Stop stop) {
        return sum(stop.children());
    }

    @Override
    public Double visitBus(Bus bus) {
        return bus.dailyTicketRevenue();
    }

    @Override
    public Double visitTram(Tram tram) {
        return tram.dailyTicketRevenue();
    }

    @Override
    public Double visitStation(Station station) {
        return 0.0;
    }

    @Override
    public Double visitTicketMachine(TicketMachine machine) {
        return machine.dailySales();
    }

    @Override
    public Double visitTrolleybus(Trolleybus trolleybus) {
        return trolleybus.dailyTicketRevenue();
    }

    private double sum(List<? extends TransitNode> nodes) {
        double total = 0;
        for (TransitNode node : nodes) {
            total += node.accept(this);
        }
        return total;
    }
}
