package transit.model;

import transit.visitor.Visitor;

public record Station(String id, String name, int platforms, boolean stepFreeAccess) implements TransitNode {

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitStation(this);
    }
}
