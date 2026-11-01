package transit.visitor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import transit.model.Bus;
import transit.model.Network;
import transit.model.Route;
import transit.model.Station;
import transit.model.Stop;
import transit.model.TicketMachine;
import transit.model.Tram;
import transit.model.Trolleybus;
import transit.model.TransitNode;

public final class FuelUsageVisitor implements Visitor<Void> {

    private static final String NO_ROUTE = "-";

    private final double dieselPricePerLiter;
    private final double electricityPricePerKwh;
    private final Map<String, Double> costByRoute = new LinkedHashMap<>();
    private double totalLiters;
    private double totalKwh;
    private String currentRoute = NO_ROUTE;

    public FuelUsageVisitor(double dieselPricePerLiter, double electricityPricePerKwh) {
        this.dieselPricePerLiter = dieselPricePerLiter;
        this.electricityPricePerKwh = electricityPricePerKwh;
    }

    public double totalLiters() {
        return totalLiters;
    }

    public double totalKwh() {
        return totalKwh;
    }

    public double totalCost() {
        return costByRoute.values().stream().mapToDouble(Double::doubleValue).sum();
    }

    public Map<String, Double> costByRoute() {
        return Map.copyOf(costByRoute);
    }

    @Override
    public Void visitNetwork(Network network) {
        visitAll(network.routes());
        return null;
    }

    @Override
    public Void visitRoute(Route route) {
        currentRoute = route.id();
        visitAll(route.stops());
        currentRoute = NO_ROUTE;
        return null;
    }

    @Override
    public Void visitStop(Stop stop) {
        visitAll(stop.children());
        return null;
    }

    @Override
    public Void visitBus(Bus bus) {
        double liters = bus.litersPer100Km() * bus.dailyKm() / 100.0;
        totalLiters += liters;
        costByRoute.merge(currentRoute, liters * dieselPricePerLiter, Double::sum);
        return null;
    }

    @Override
    public Void visitTram(Tram tram) {
        double kwh = tram.kwhPer100Km() * tram.dailyKm() / 100.0;
        totalKwh += kwh;
        costByRoute.merge(currentRoute, kwh * electricityPricePerKwh, Double::sum);
        return null;
    }

    @Override
    public Void visitStation(Station station) {
        return null;
    }

    @Override
    public Void visitTicketMachine(TicketMachine machine) {
        return null;
    }

    @Override
    public Void visitTrolleybus(Trolleybus trolleybus) {
        double kwh = trolleybus.kwhPer100Km() * trolleybus.dailyKm() / 100.0;
        totalKwh += kwh;
        costByRoute.merge(currentRoute, kwh * electricityPricePerKwh, Double::sum);
        return null;
    }

    private void visitAll(List<? extends TransitNode> nodes) {
        for (TransitNode node : nodes) {
            node.accept(this);
        }
    }
}
