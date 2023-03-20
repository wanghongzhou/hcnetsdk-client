package io.github.wanghongzhou.client.util.io;

import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Brian
 */
public class QueueOutputStream extends OutputStream {

    private final BlockingQueue<Integer> blockingQueue;

    public QueueOutputStream() {
        this(new LinkedBlockingQueue<>());
    }

    public QueueOutputStream(final BlockingQueue<Integer> blockingQueue) {
        this.blockingQueue = Objects.requireNonNull(blockingQueue, "blockingQueue");
    }

    public QueueInputStream newQueueInputStream() {
        return new QueueInputStream(blockingQueue);
    }

    @Override
    public void write(final int b) throws InterruptedIOException {
        try {
            blockingQueue.put(0xFF & b);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            final InterruptedIOException interruptedIoException = new InterruptedIOException();
            interruptedIoException.initCause(e);
            throw interruptedIoException;
        }
    }
}
