package transit.model;

import transit.visitor.Visitor;

public interface TransitNode {

    String id();

    <R> R accept(Visitor<R> visitor);
}
