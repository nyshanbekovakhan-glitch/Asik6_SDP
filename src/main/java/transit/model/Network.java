package transit.model;

import java.util.List;
import transit.visitor.Visitor;

public record Network(String id, String name, List<Route> routes) implements TransitNode {

    public Network {
        routes = List.copyOf(routes);
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitNetwork(this);
    }
}
