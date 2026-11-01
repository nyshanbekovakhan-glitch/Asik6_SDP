package transit.model;

import java.util.List;
import transit.visitor.Visitor;

public record Stop(String id, String name, boolean stepFreePlatform, List<TransitNode> children)
        implements TransitNode {

    public Stop {
        children = List.copyOf(children);
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitStop(this);
    }
}
