package io.github.wanghongzhou.client;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.ByteByReference;
import io.github.wanghongzhou.client.util.StreamVideoPlayer;
import io.github.wanghongzhou.hcnetsdk.HCNetSDK;
import io.github.wanghongzhou.hcnetsdk.HikDevice;
import io.github.wanghongzhou.hcnetsdk.HikDeviceTemplate;
import io.github.wanghongzhou.hcnetsdk.model.Token;
import io.github.wanghongzhou.hcnetsdk.operations.HikResult;
import io.github.wanghongzhou.hcnetsdk.util.JnaPathUtils;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Brian
 */
public class StreamVideoPlayerApplication extends Application {

    private HikDevice hikDevice;
    private StreamVideoPlayer videoPlayer;
    private HCNetSDK.FRealDataCallBack_V30 callBack_v30;
    private final ExecutorService writeExecutor = Executors.newSingleThreadExecutor();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        final ImageView imageView = new ImageView();
        imageView.fitWidthProperty().bind(primaryStage.widthProperty());
        imageView.fitHeightProperty().bind(primaryStage.heightProperty());

        videoPlayer = new StreamVideoPlayer(imageView);
        final Button recordBtn = new Button("start recording");
        recordBtn.setOnAction(event -> {
            if (videoPlayer.getRecording().get()) {
                videoPlayer.stopRecording();
                recordBtn.setText("start recording");
            } else {
                videoPlayer.startRecording("video/" + System.currentTimeMillis() + ".mp4");
                recordBtn.setText("stop recording");
            }
        });

        primaryStage.setTitle("Stream Video Player");
        primaryStage.setScene(new Scene(new StackPane(imageView, recordBtn), 640, 480));

        JnaPathUtils.initJnaLibraryPath(StreamVideoPlayerApplication.class);
        HCNetSDK hcNetSDK = HCNetSDK.INSTANCE;
        hcNetSDK.NET_DVR_Init();

        hikDevice = new HikDevice("192.168.60.164", 8000, "admin", "JL654321", new HikDeviceTemplate(hcNetSDK));
        HikResult<Token> loginResult = hikDevice.init();
        if (loginResult.isSuccess()) {
            primaryStage.show();
            preview(imageView);
        }
    }

    public void preview(ImageView imageView) {
        HCNetSDK.NET_DVR_PREVIEWINFO previewInfo = new HCNetSDK.NET_DVR_PREVIEWINFO();
        previewInfo.read();
        previewInfo.lChannel = 1;  //通道号
        previewInfo.dwStreamType = 0; //0-主码流，1-子码流，2-三码流，3-虚拟码流，以此类推
        previewInfo.dwLinkMode = 0; //连接方式：0- TCP方式，1- UDP方式，2- 多播方式，3- RTP方式，4- RTP/RTSP，5- RTP/HTTP，6- HRUDP（可靠传输） ，7- RTSP/HTTPS，8- NPQ
        previewInfo.bBlocked = 1;  //0- 非阻塞取流，1- 阻塞取流

        videoPlayer = new StreamVideoPlayer(imageView);
        callBack_v30 = (int lRealHandle, int dwDataType, ByteByReference pBuffer, int dwBufSize, Pointer pUser) -> {
            switch (dwDataType) {
                case HCNetSDK.NET_DVR_SYSHEAD, HCNetSDK.NET_DVR_STREAMDATA -> {   //PS封装的码流数据
                    byte[] data = pBuffer.getPointer().getByteArray(0, dwBufSize);
                    writeExecutor.submit(() -> {
                        try {
                            videoPlayer.getOutputStream().write(data);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                }
            }
        };

        HikResult<Integer> result = hikDevice.realPlay(previewInfo, callBack_v30);
        if (result.isSuccess()) {
            videoPlayer.startPlaying();
        }
    }

    @Override
    public void stop() {
        hikDevice.stopRealPlay();
        videoPlayer.stopPlaying();
        writeExecutor.shutdownNow();
    }
}
