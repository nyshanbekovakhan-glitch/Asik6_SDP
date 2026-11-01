package transit.app;

import java.time.Duration;

public record SystemConfig(int producerThreads, int delayThresholdSeconds, int ridershipBatchSize,
                           Duration shutdownTimeout) {

    public SystemConfig {
        if (producerThreads < 1 || ridershipBatchSize < 1 || delayThresholdSeconds < 0) {
            throw new IllegalArgumentException("invalid configuration");
        }
    }

    public static SystemConfig withThreads(int producerThreads) {
        return new SystemConfig(producerThreads, 300, 100, Duration.ofSeconds(30));
    }
}
