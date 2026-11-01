package transit.model;

import transit.visitor.Visitor;

public record TicketMachine(String id, double dailySales, boolean audioGuidance) implements TransitNode {

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitTicketMachine(this);
    }
}
