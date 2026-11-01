package transit.visitor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import transit.model.Bus;
import transit.model.Network;
import transit.model.Route;
import transit.model.Station;
import transit.model.Stop;
import transit.model.TicketMachine;
import transit.model.Tram;
import transit.model.Trolleybus;
import transit.model.TransitNode;

public final class JsonExportVisitor implements Visitor<String> {

    private static final String INDENT = "  ";

    @Override
    public String visitNetwork(Network network) {
        return object(
                text("type", "network"), text("id", network.id()), text("name", network.name()),
                array("routes", network.routes()));
    }

    @Override
    public String visitRoute(Route route) {
        return object(
                text("type", "route"), text("id", route.id()), text("name", route.name()),
                array("stops", route.stops()));
    }

    @Override
    public String visitStop(Stop stop) {
        return object(
                text("type", "stop"), text("id", stop.id()), text("name", stop.name()),
                raw("stepFreePlatform", stop.stepFreePlatform()),
                array("children", stop.children()));
    }

    @Override
    public String visitBus(Bus bus) {
        return object(
                text("type", "bus"), text("id", bus.id()),
                raw("litersPer100Km", bus.litersPer100Km()), raw("dailyKm", bus.dailyKm()),
                raw("dailyTicketRevenue", bus.dailyTicketRevenue()),
                raw("lowFloor", bus.lowFloor()), raw("capacity", bus.capacity()));
    }

    @Override
    public String visitTram(Tram tram) {
        return object(
                text("type", "tram"), text("id", tram.id()),
                raw("kwhPer100Km", tram.kwhPer100Km()), raw("dailyKm", tram.dailyKm()),
                raw("dailyTicketRevenue", tram.dailyTicketRevenue()),
                raw("lowFloor", tram.lowFloor()), raw("capacity", tram.capacity()));
    }

    @Override
    public String visitStation(Station station) {
        return object(
                text("type", "station"), text("id", station.id()), text("name", station.name()),
                raw("platforms", station.platforms()), raw("stepFreeAccess", station.stepFreeAccess()));
    }

    @Override
    public String visitTicketMachine(TicketMachine machine) {
        return object(
                text("type", "ticketMachine"), text("id", machine.id()),
                raw("dailySales", machine.dailySales()), raw("audioGuidance", machine.audioGuidance()));
    }

    @Override
    public String visitTrolleybus(Trolleybus trolleybus) {
        return object(
                text("type", "trolleybus"), text("id", trolleybus.id()),
                raw("kwhPer100Km", trolleybus.kwhPer100Km()), raw("dailyKm", trolleybus.dailyKm()),
                raw("dailyTicketRevenue", trolleybus.dailyTicketRevenue()),
                raw("lowFloor", trolleybus.lowFloor()), raw("capacity", trolleybus.capacity()));
    }

    private static String object(String... fields) {
        String body = List.of(fields).stream().map(JsonExportVisitor::indent).collect(Collectors.joining(",\n"));
        return "{\n" + body + "\n}";
    }

    private String array(String key, List<? extends TransitNode> children) {
        if (children.isEmpty()) {
            return quote(key) + ": []";
        }
        List<String> items = new ArrayList<>();
        for (TransitNode child : children) {
            items.add(indent(child.accept(this)));
        }
        return quote(key) + ": [\n" + String.join(",\n", items) + "\n]";
    }

    private static String text(String key, String value) {
        return quote(key) + ": " + quote(value);
    }

    private static String raw(String key, Object value) {
        return quote(key) + ": " + value;
    }

    private static String indent(String block) {
        return block.lines().map(line -> INDENT + line).collect(Collectors.joining("\n"));
    }

    private static String quote(String value) {
        StringBuilder out = new StringBuilder("\"");
        for (char c : value.toCharArray()) {
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
                }
            }
        }
        return out.append('"').toString();
    }
}
