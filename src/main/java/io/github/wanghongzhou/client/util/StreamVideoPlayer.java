package io.github.wanghongzhou.client.util;

import io.github.wanghongzhou.client.util.io.QueueInputStream;
import io.github.wanghongzhou.client.util.io.QueueOutputStream;
import javafx.scene.image.ImageView;
import lombok.Getter;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FrameGrabber;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * @author Brian
 */
@Getter
public class StreamVideoPlayer extends AbstractVideoPlayer {

    private final QueueInputStream inputStream = new QueueInputStream(new ArrayBlockingQueue<>(1024 * 1024)); // 1mb
    private final QueueOutputStream outputStream = inputStream.newQueueOutputStream();

    public StreamVideoPlayer(ImageView imageView) {
        super(imageView);
    }

    public void startPlaying() {
        super.startPlaying();
    }

    @Override
    protected FrameGrabber startFrameGrabber() throws FFmpegFrameGrabber.Exception {
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(this.inputStream, 0);
        grabber.setOption("probesize", "10240"); // 字节
        grabber.setOption("analyzeduration", "40000"); // 微妙
        grabber.start();
        return grabber;
    }
}
