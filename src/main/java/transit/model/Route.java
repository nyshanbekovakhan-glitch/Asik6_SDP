package transit.model;

import java.util.List;
import transit.visitor.Visitor;

public record Route(String id, String name, List<Stop> stops) implements TransitNode {

    public Route {
        stops = List.copyOf(stops);
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitRoute(this);
    }
}
