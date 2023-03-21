package io.github.wanghongzhou.client.util;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.Getter;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameRecorder;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Brian
 */
@Getter
public abstract class AbstractVideoPlayer implements Runnable {

    protected File outputFile;
    protected volatile Thread playThread;
    protected final ImageView imageView;
    protected final AtomicBoolean playing = new AtomicBoolean();
    protected final AtomicBoolean recording = new AtomicBoolean();
    protected final AtomicBoolean initRecorder = new AtomicBoolean();
    protected final ShareFrameConverter converter = new ShareFrameConverter();

    public AbstractVideoPlayer(ImageView imageView) {
        this.imageView = imageView;
    }

    protected void startPlaying() {
        this.playThread = new Thread(this);
        this.playThread.setDaemon(true);
        this.playThread.start();
    }

    public void stopPlaying() {
        if (Objects.nonNull(this.playThread)) {
            this.playThread.interrupt();
        }
    }

    public void startRecording(String outputFile) {
        try {
            File file = new File(outputFile);
            if (!file.exists() && !file.createNewFile()) {
                throw new RuntimeException("Create file fail: ");
            }
            this.outputFile = file;
            this.recording.set(false);
            this.initRecorder.set(true);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void stopRecording() {
        this.recording.set(false);
    }

    protected abstract FrameGrabber startFrameGrabber() throws FrameGrabber.Exception;

    protected FrameRecorder startFrameRecorder(FrameGrabber grabber) throws FrameRecorder.Exception {
        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(this.outputFile, grabber.getImageWidth(), grabber.getImageHeight(), grabber.getAudioChannels());
        recorder.setFrameRate(grabber.getFrameRate());
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
        recorder.setAudioOptions(grabber.getAudioOptions());
        recorder.setVideoOptions(grabber.getVideoOptions());
        recorder.start();
        return recorder;
    }

    protected SourceDataLine startSourceDataLine(FrameGrabber grabber) throws LineUnavailableException {
        if (grabber.getAudioChannels() > 0) {
            AudioFormat audioFormat = new AudioFormat(grabber.getSampleRate(), 16, grabber.getAudioChannels(), true, true);
            SourceDataLine soundLine = (SourceDataLine) AudioSystem.getLine(new DataLine.Info(SourceDataLine.class, audioFormat));
            soundLine.open(audioFormat);
            soundLine.start();
            return soundLine;
        }
        return null;
    }

    @Override
    public void run() {
        FrameGrabber grabber = null;
        FrameRecorder recorder = null;
        SourceDataLine soundLine = null;
        ExecutorService audioExecutor = Executors.newSingleThreadExecutor();
        ExecutorService imageExecutor = Executors.newSingleThreadExecutor();
        try {
            avutil.av_log_set_level(avutil.AV_LOG_ERROR);
            grabber = startFrameGrabber(); // 启动视频流抓取器
            soundLine = startSourceDataLine(grabber); // 启动音频数据线
            this.playing.set(true);
            while (!Thread.interrupted()) {
                final Frame frame = grabber.grab();
                if (Objects.isNull(frame)) {
                    continue;
                }
                if (this.recording.get()) {
                    if (Objects.nonNull(recorder)) {
                        recorder.record(frame);
                    }
                } else {
                    if (this.initRecorder.get()) {
                        try {
                            recorder = startFrameRecorder(grabber);
                            recorder.record(frame);
                            this.recording.set(true);
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            this.initRecorder.set(false);
                        }
                    } else if (Objects.nonNull(recorder)) {
                        recorder.close();
                        recorder = null;
                    }
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
                    SourceDataLine finalSoundLine = soundLine;
                    audioExecutor.submit(() -> {
                        finalSoundLine.write(outBuffer.array(), 0, outBuffer.capacity());
                        outBuffer.clear();
                    });
                }
            }
        } catch (LineUnavailableException | IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            this.playing.set(false);
            this.recording.set(false);
            if (Objects.nonNull(soundLine)) {
                soundLine.close();
            }
            if (Objects.nonNull(grabber)) {
                try {
                    grabber.close();
                } catch (FrameGrabber.Exception e) {
                    e.printStackTrace();
                }
            }
            if (Objects.nonNull(recorder)) {
                try {
                    recorder.close();
                } catch (FrameRecorder.Exception e) {
                    e.printStackTrace();
                }
            }
            audioExecutor.shutdownNow();
            imageExecutor.shutdownNow();
        }
    }
}
