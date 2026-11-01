package transit.observer;

import transit.event.Event;
import transit.event.EventType;

public interface Subject<E extends Event> {

    void subscribe(EventType type, Observer<? super E> observer);

    void unsubscribe(EventType type, Observer<? super E> observer);

    void publish(E event);
}
