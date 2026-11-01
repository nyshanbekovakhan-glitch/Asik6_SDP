package transit.observer;

import java.util.logging.Level;
import java.util.logging.Logger;
import transit.event.Event;
import transit.event.EventType;

public abstract class BaseSubject<E extends Event> implements Subject<E> {

    private static final Logger LOG = Logger.getLogger(BaseSubject.class.getName());

    private final ListenerRegistry<E> registry = new ListenerRegistry<>();

    @Override
    public void subscribe(EventType type, Observer<? super E> observer) {
        registry.add(type, observer);
    }

    @Override
    public void unsubscribe(EventType type, Observer<? super E> observer) {
        registry.remove(type, observer);
    }

    @Override
    public void publish(E event) {
        for (Observer<? super E> observer : registry.listenersFor(event.type())) {
            try {
                observer.onEvent(event);
            } catch (RuntimeException failure) {

                LOG.log(Level.WARNING, "Listener failed on " + event.type(), failure);
            }
        }
    }
}
