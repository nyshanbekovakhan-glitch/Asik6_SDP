package transit.observer;

import transit.event.Event;

@FunctionalInterface
public interface Observer<E extends Event> {

    void onEvent(E event);
}
