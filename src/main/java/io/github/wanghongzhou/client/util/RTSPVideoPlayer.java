package io.github.wanghongzhou.client.util;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.Getter;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Brian
 */
@Getter
public class RTSPVideoPlayer implements Runnable {

    private final String rtsp;
    private final String outputFile;
    private final ImageView imageView;

    public RTSPVideoPlayer(String rtsp, ImageView imageView) {
        this(rtsp, null, imageView);
    }

    public RTSPVideoPlayer(String rtsp, String outputFile, ImageView imageView) {
        this.rtsp = rtsp;
        this.imageView = imageView;
        this.outputFile = outputFile;
    }

    @Override
    public void run() {
        avutil.av_log_set_level(avutil.AV_LOG_ERROR);
        try (ShareFrameConverter converter = new ShareFrameConverter(); FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(this.rtsp)) {

            // 启动视频流抓取器
            grabber.setOption("rtsp_transport", "tcp");
            grabber.start();

            // 启动视频流录制器
            FFmpegFrameRecorder recorder = null;
            if (Objects.nonNull(this.outputFile)) {
                File file = new File(this.outputFile);
                if (!file.exists() && !file.createNewFile()) {
                    throw new RuntimeException("Create file fail: ");
                }
                recorder = new FFmpegFrameRecorder(file, grabber.getImageWidth(), grabber.getImageHeight(), grabber.getAudioChannels());
                recorder.setFrameRate(grabber.getFrameRate());
                recorder.setAudioOptions(grabber.getAudioOptions());
                recorder.setVideoOptions(grabber.getVideoOptions());
                recorder.start();
            }

            // 音频数据线
            final SourceDataLine soundLine;
            if (grabber.getAudioChannels() > 0) {
                AudioFormat audioFormat = new AudioFormat(grabber.getSampleRate(), 16, grabber.getAudioChannels(), true, true);
                soundLine = (SourceDataLine) AudioSystem.getLine(new DataLine.Info(SourceDataLine.class, audioFormat));
                soundLine.open(audioFormat);
                soundLine.start();
            } else {
                soundLine = null;
            }

            // 处理帧数据
            ExecutorService audioExecutor = Executors.newSingleThreadExecutor();
            ExecutorService imageExecutor = Executors.newSingleThreadExecutor();
            while (!Thread.interrupted()) {
                final Frame frame = grabber.grab();
                if (Objects.isNull(frame)) {
                    continue;
                }
                if (Objects.nonNull(recorder)) {
                    recorder.record(frame);
                }
                if (Objects.nonNull(frame.image)) {  // 视频帧
                    final Image image = converter.convert(frame);
                    if (Objects.nonNull(image)) {
                        imageExecutor.submit(() -> Platform.runLater(() -> imageView.setImage(image)));
                    }
                } else if (Objects.nonNull(frame.samples) && Objects.nonNull(soundLine)) {  // 音频帧
                    final ShortBuffer samplesBuffer = (ShortBuffer) frame.samples[0];
                    final ByteBuffer outBuffer = ByteBuffer.allocate(samplesBuffer.capacity() * 2);
                    outBuffer.asShortBuffer().put(samplesBuffer.rewind());
                    audioExecutor.submit(() -> {
                        soundLine.write(outBuffer.array(), 0, outBuffer.capacity());
                        outBuffer.clear();
                    });
                }
            }
            if (Objects.nonNull(soundLine)) {
                soundLine.close();
            }
            if (Objects.nonNull(recorder)) {
                recorder.stop();
                recorder.release();
                recorder.close();
            }
            grabber.stop();
            grabber.release();
            audioExecutor.shutdownNow();
            imageExecutor.shutdownNow();
        } catch (LineUnavailableException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
