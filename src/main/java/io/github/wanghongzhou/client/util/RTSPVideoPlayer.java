package io.github.wanghongzhou.client.util;

import javafx.scene.image.ImageView;
import lombok.Getter;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FrameGrabber;

/**
 * @author Brian
 */
@Getter
public class RTSPVideoPlayer extends AbstractVideoPlayer {

    private String rtsp;

    public RTSPVideoPlayer(ImageView imageView) {
        super(imageView);
    }

    public void startPlaying(String rtsp) {
        this.rtsp = rtsp;
        super.startPlaying();
    }

    @Override
    protected FrameGrabber startFrameGrabber() throws FrameGrabber.Exception {
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(this.rtsp);
        grabber.setOption("probesize", "10240"); // 字节
        grabber.setOption("analyzeduration", "40000"); // 微妙
        grabber.setOption("rtsp_transport", "tcp");
        grabber.start();
        return grabber;
    }
}
