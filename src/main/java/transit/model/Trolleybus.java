package transit.model;

import transit.visitor.Visitor;

public record Trolleybus(String id, double kwhPer100Km, double dailyKm, double dailyTicketRevenue,
                         boolean lowFloor, int capacity) implements TransitNode {

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitTrolleybus(this);
    }
}
