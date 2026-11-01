package transit.concurrent;

import java.util.function.IntConsumer;

public record ProducerTask(int count, IntConsumer emitter) implements Runnable {

    @Override
    public void run() {
        for (int i = 0; i < count; i++) {
            emitter.accept(i);
        }
    }
}
