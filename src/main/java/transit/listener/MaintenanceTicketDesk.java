package transit.listener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import transit.event.MaintenanceTicketEvent;
import transit.observer.Observer;

public final class MaintenanceTicketDesk implements Observer<MaintenanceTicketEvent> {

    private final List<MaintenanceTicketEvent> tickets = new CopyOnWriteArrayList<>();

    @Override
    public void onEvent(MaintenanceTicketEvent ticket) {
        tickets.add(ticket);
    }

    public List<MaintenanceTicketEvent> tickets() {
        return List.copyOf(tickets);
    }
}
