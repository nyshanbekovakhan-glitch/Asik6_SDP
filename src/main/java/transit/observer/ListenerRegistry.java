package transit.observer;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import transit.event.Event;
import transit.event.EventType;

final class ListenerRegistry<E extends Event> {

    private final ConcurrentMap<EventType, CopyOnWriteArrayList<Observer<? super E>>> byType =
            new ConcurrentHashMap<>();

    void add(EventType type, Observer<? super E> observer) {
        byType.computeIfAbsent(type, t -> new CopyOnWriteArrayList<>()).addIfAbsent(observer);
    }

    void remove(EventType type, Observer<? super E> observer) {
        CopyOnWriteArrayList<Observer<? super E>> listeners = byType.get(type);
        if (listeners != null) {
            listeners.remove(observer);
        }
    }

    List<Observer<? super E>> listenersFor(EventType type) {
        CopyOnWriteArrayList<Observer<? super E>> listeners = byType.get(type);
        return listeners == null ? List.of() : listeners;
    }
}
