package io.github.wanghongzhou.client.util.io;

import java.io.InputStream;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Brian
 */
public class QueueInputStream extends InputStream {
    private final BlockingQueue<Integer> blockingQueue;

    public QueueInputStream() {
        this(new LinkedBlockingQueue<>());
    }

    public QueueInputStream(final BlockingQueue<Integer> blockingQueue) {
        this.blockingQueue = Objects.requireNonNull(blockingQueue, "blockingQueue");
    }

    public QueueOutputStream newQueueOutputStream() {
        return new QueueOutputStream(blockingQueue);
    }

    @Override
    public int read() {
        try {
            return 0xFF & blockingQueue.take();
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            return -1;
        }
    }
}