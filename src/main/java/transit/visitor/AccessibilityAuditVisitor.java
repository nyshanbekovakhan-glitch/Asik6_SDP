package transit.visitor;

import java.util.ArrayList;
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

public final class AccessibilityAuditVisitor implements Visitor<List<TransitNode>> {

    @Override
    public List<TransitNode> visitNetwork(Network network) {
        return collect(network.routes());
    }

    @Override
    public List<TransitNode> visitRoute(Route route) {
        return collect(route.stops());
    }

    @Override
    public List<TransitNode> visitStop(Stop stop) {
        List<TransitNode> failing = new ArrayList<>();
        if (!stop.stepFreePlatform()) {
            failing.add(stop);
        }
        failing.addAll(collect(stop.children()));
        return failing;
    }

    @Override
    public List<TransitNode> visitBus(Bus bus) {
        return bus.lowFloor() ? List.of() : List.of(bus);
    }

    @Override
    public List<TransitNode> visitTram(Tram tram) {
        return tram.lowFloor() ? List.of() : List.of(tram);
    }

    @Override
    public List<TransitNode> visitStation(Station station) {
        return station.stepFreeAccess() ? List.of() : List.of(station);
    }

    @Override
    public List<TransitNode> visitTicketMachine(TicketMachine machine) {
        return machine.audioGuidance() ? List.of() : List.of(machine);
    }

    @Override
    public List<TransitNode> visitTrolleybus(Trolleybus trolleybus) {
        return trolleybus.lowFloor() ? List.of() : List.of(trolleybus);
    }

    private List<TransitNode> collect(List<? extends TransitNode> nodes) {
        List<TransitNode> failing = new ArrayList<>();
        for (TransitNode node : nodes) {
            failing.addAll(node.accept(this));
        }
        return failing;
    }
}
