package transit.model;

import transit.visitor.Visitor;

public record Bus(String id, double litersPer100Km, double dailyKm, double dailyTicketRevenue,
                  boolean lowFloor, int capacity) implements TransitNode {

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitBus(this);
    }
}
