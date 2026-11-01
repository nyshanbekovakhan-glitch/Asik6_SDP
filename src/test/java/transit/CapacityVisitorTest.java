package transit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import transit.model.Network;
import transit.visitor.CapacityVisitor;

class CapacityVisitorTest {

    @Test
    void capacityIsSumOfAllVehicleCapacities() {
        Network network = TestData.network();

        assertEquals(190, network.accept(new CapacityVisitor()));
        assertEquals(150, network.routes().get(0).accept(new CapacityVisitor()));
        assertEquals(0, network.routes().get(0).stops().get(1).accept(new CapacityVisitor()));
    }
}
