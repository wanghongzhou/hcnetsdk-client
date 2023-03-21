package io.github.wanghongzhou.client.service;

import com.sun.glass.ui.Window;
import com.sun.javafx.stage.WindowHelper;
import com.sun.javafx.tk.TKStage;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.ptr.ByteByReference;
import io.github.wanghongzhou.client.HCNetSDKClientApplication;
import io.github.wanghongzhou.client.mode.LoginInfo;
import io.github.wanghongzhou.client.util.StreamVideoPlayer;
import io.github.wanghongzhou.hcnetsdk.DeviceTemplate;
import io.github.wanghongzhou.hcnetsdk.HCNetSDK;
import io.github.wanghongzhou.hcnetsdk.HikDevice;
import io.github.wanghongzhou.hcnetsdk.model.Token;
import io.github.wanghongzhou.hcnetsdk.operations.HikResult;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Brian
 */
@Service
public class MainService {

    private HikDevice currentDevice;
    private StreamVideoPlayer videoPlayer;
    private HCNetSDK.FRealDataCallBack_V30 callBack_v30;
    private final ExecutorService writeExecutor = Executors.newSingleThreadExecutor();

    @Resource
    private DeviceTemplate deviceTemplate;

    public HikDevice login(LoginInfo loginInfo) {

        // check logged
        if (Objects.nonNull(currentDevice)) {
            HCNetSDKClientApplication.showErrorAlert("设备已登录！");
            return null;
        }

        // login
        HikDevice hikDevice = new HikDevice(loginInfo.getIp(), Integer.parseInt(loginInfo.getPort()), loginInfo.getUser(), loginInfo.getPassword(), deviceTemplate);
        HikResult<Token> loginResult = hikDevice.init();
        if (loginResult.isSuccess()) {
            currentDevice = hikDevice;
            HCNetSDKClientApplication.showInfoAlert("登录成功");
            return currentDevice;
        } else {
            HCNetSDKClientApplication.showErrorAlert("登录失败, " + loginResult.getError() + "！");
            return null;
        }
    }

    public void preview(int channel, boolean callback, ImageView imageView) {
        if (Objects.isNull(currentDevice)) {
            HCNetSDKClientApplication.showErrorAlert("请先登录");
            return;
        }

        if (currentDevice.getRealHandle() >= 0) {
            HCNetSDKClientApplication.showErrorAlert("正在预览");
            return;
        }

        HCNetSDK.NET_DVR_PREVIEWINFO previewInfo = new HCNetSDK.NET_DVR_PREVIEWINFO();
        previewInfo.read();
        previewInfo.lChannel = channel;  //通道号
        previewInfo.dwStreamType = 0; //0-主码流，1-子码流，2-三码流，3-虚拟码流，以此类推
        previewInfo.dwLinkMode = 0; //连接方式：0- TCP方式，1- UDP方式，2- 多播方式，3- RTP方式，4- RTP/RTSP，5- RTP/HTTP，6- HRUDP（可靠传输） ，7- RTSP/HTTPS，8- NPQ
        previewInfo.bBlocked = 1;  //0- 非阻塞取流，1- 阻塞取流

        HikResult<Integer> result;
        if (callback) {
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
            result = currentDevice.realPlay(previewInfo, callBack_v30);
            if (result.isSuccess()) {
                videoPlayer.startPlaying();
            }
        } else {
            // 不回调预览
            Stage videoStage = new Stage();
            videoStage.initOwner(HCNetSDKClientApplication.getStageManager().getPrimaryStage().getStage());
            videoStage.setTitle("VideoStage");
            videoStage.setWidth(730);
            videoStage.setHeight(730);
            videoStage.centerOnScreen();
            videoStage.setOnHiding(event -> stopPreview());
            videoStage.show();
            previewInfo.hPlayWnd = getStageHWND2(videoStage);
            result = currentDevice.realPlay(previewInfo, null);
        }

        if (!result.isSuccess()) {
            HCNetSDKClientApplication.showErrorAlert("预览失败, " + result.getError() + "！");
        }
    }

    @PreDestroy
    public void stopPreview() {
        currentDevice.stopRealPlay();
        videoPlayer.stopPlaying();
        writeExecutor.shutdownNow();
    }

    /**
     * 获取stage句柄，因为javafx只有主窗口存在句柄（注意有同名title的窗口）
     */
    public static WinDef.HWND getStageHWND1(Stage stage) {
        return User32.INSTANCE.FindWindow(null, stage.getTitle());
    }

    /**
     * 获取stage句柄，因为javafx只有主窗口存在句柄
     */
    private static WinDef.HWND getStageHWND2(Stage stage) {
        try {
            // 获取窗口对等操作的中间对象
            TKStage tkStage = WindowHelper.getPeer(stage);

            // 反射修改访问权限， 获取平台相关的窗口封装对象
            Method getPlatformWindow = tkStage.getClass().getDeclaredMethod("getPlatformWindow");
            getPlatformWindow.setAccessible(true);
            Window platformWindow = (Window) getPlatformWindow.invoke(tkStage);

            // 从封装对象中获取本地句柄
            return new WinDef.HWND(Pointer.createConstant(platformWindow.getNativeWindow()));
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }
}