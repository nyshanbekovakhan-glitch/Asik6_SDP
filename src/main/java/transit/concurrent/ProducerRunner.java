package transit.concurrent;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public final class ProducerRunner {

    private final ExecutorService executor;
    private final Duration timeout;

    public ProducerRunner(ExecutorService executor, Duration timeout) {
        this.executor = executor;
        this.timeout = timeout;
    }

    public void runAll(List<? extends Runnable> tasks) throws InterruptedException, ExecutionException {
        List<Future<?>> futures = new ArrayList<>();
        try {
            for (Runnable task : tasks) {
                futures.add(executor.submit(task));
            }
        } finally {
            executor.shutdown();
        }
        awaitTermination();
        for (Future<?> future : futures) {
            future.get();
        }
    }

    private void awaitTermination() throws InterruptedException {
        try {
            if (!executor.awaitTermination(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
                executor.shutdownNow();
                throw new IllegalStateException("Producers did not finish within " + timeout);
            }
        } catch (InterruptedException interrupted) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
            throw interrupted;
        }
    }
}
