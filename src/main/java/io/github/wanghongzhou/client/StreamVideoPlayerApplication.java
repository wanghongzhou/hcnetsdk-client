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
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * @author Brian
 */
public class StreamVideoPlayerApplication extends Application {

    private HikDevice hikDevice;
    private volatile Thread playThread;
    private HCNetSDK.FRealDataCallBack_V30 callBack_v30;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        final ImageView imageView = new ImageView();
        imageView.fitWidthProperty().bind(primaryStage.widthProperty());
        imageView.fitHeightProperty().bind(primaryStage.heightProperty());

        primaryStage.setTitle("Stream Video Player");
        primaryStage.setScene(new Scene(new StackPane(imageView), 640, 480));

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

        final StreamVideoPlayer videoPlayer = new StreamVideoPlayer(imageView);
        callBack_v30 = (int lRealHandle, int dwDataType, ByteByReference pBuffer, int dwBufSize, Pointer pUser) -> {
            switch (dwDataType) {
                case HCNetSDK.NET_DVR_SYSHEAD: //系统头
                case HCNetSDK.NET_DVR_STREAMDATA:   //PS封装的码流数据
                    try {
                        videoPlayer.getOutputStream().write(pBuffer.getPointer().getByteArray(0, dwBufSize));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }
        };

        HikResult<Integer> result = hikDevice.realPlay(previewInfo, callBack_v30);
        if (result.isSuccess()) {
            playThread = new Thread(videoPlayer);
            playThread.start();
        }
    }

    @Override
    public void stop() {
        hikDevice.stopRealPlay();
        playThread.interrupt();
    }
}
