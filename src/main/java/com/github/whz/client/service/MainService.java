package com.github.whz.client.service;

import com.github.whz.client.HCNetSDKClientApplication;
import com.github.whz.client.mode.LoginInfo;
import com.github.whz.hcnetsdk.DeviceTemplate;
import com.github.whz.hcnetsdk.HCNetSDK;
import com.github.whz.hcnetsdk.HikDevice;
import com.github.whz.hcnetsdk.model.Token;
import com.github.whz.hcnetsdk.operations.HikResult;
import com.sun.glass.ui.Window;
import com.sun.javafx.stage.WindowHelper;
import com.sun.javafx.tk.TKStage;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.ptr.ByteByReference;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.apache.commons.io.input.QueueInputStream;
import org.apache.commons.io.output.QueueOutputStream;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Brian
 */
@Service
public class MainService {

    private Long previewHandle;
    private HikDevice currentDevice;
    private HCNetSDK.FRealDataCallBack_V30 callBack_v30;
    private final QueueInputStream inputStream = new QueueInputStream();
    private final QueueOutputStream outputStream = inputStream.newQueueOutputStream();
    private final Java2DFrameConverter converter = new Java2DFrameConverter();
    private final ScheduledExecutorService decoderService = Executors.newSingleThreadScheduledExecutor();

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

    public void preview(long channel, boolean callback, ImageView imageView) {
        if (Objects.isNull(currentDevice)) {
            HCNetSDKClientApplication.showErrorAlert("请先登录");
            return;
        }

        if (Objects.nonNull(previewHandle)) {
            HCNetSDKClientApplication.showErrorAlert("正在预览");
            return;
        }

        HCNetSDK.NET_DVR_PREVIEWINFO previewinfo = new HCNetSDK.NET_DVR_PREVIEWINFO();
        previewinfo.read();
        previewinfo.lChannel = new NativeLong(channel);  //通道号
        previewinfo.dwStreamType = 0; //0-主码流，1-子码流，2-三码流，3-虚拟码流，以此类推
        previewinfo.dwLinkMode = 0; //连接方式：0- TCP方式，1- UDP方式，2- 多播方式，3- RTP方式，4- RTP/RTSP，5- RTP/HTTP，6- HRUDP（可靠传输） ，7- RTSP/HTTPS，8- NPQ
        previewinfo.bBlocked = true;  //0- 非阻塞取流，1- 阻塞取流

        ImageView test = new ImageView();
        HikResult<Long> result;
        if (callback) {
            callBack_v30 = (NativeLong lRealHandle, int dwDataType, ByteByReference pBuffer, int dwBufSize, Pointer pUser) -> {
                switch (dwDataType) {
                    case HCNetSDK.NET_DVR_SYSHEAD: //系统头
                    case HCNetSDK.NET_DVR_STREAMDATA:   //PS封装的码流数据
                        try {
                            outputStream.write(pBuffer.getPointer().getByteArray(0, dwBufSize));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                }
            };
            result = currentDevice.getDeviceTemplate().realPlay(currentDevice.getToken(), previewinfo, callBack_v30);
            if (result.isSuccess()) {
                decoderService.execute(() -> {
                    try {
                        // 启动视频流抓取器
                        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputStream);
                        grabber.setVideoOption("preset", "ultrafast");
                        grabber.start();

                        // 抓取视频流
                        decoderService.scheduleAtFixedRate(() -> {
                            try {
                                final Frame frame = grabber.grab();
                                System.out.println("grabber.grab() " + frame);
                                if (Objects.nonNull(frame)) {
                                    final BufferedImage bufferedImage = converter.convert(frame);
                                    if (Objects.nonNull(bufferedImage)) {
                                        Platform.runLater(() -> imageView.setImage(SwingFXUtils.toFXImage(bufferedImage, null)));
                                    }
                                }
                            } catch (FFmpegFrameGrabber.Exception e) {
                                e.printStackTrace();
                            }
                        }, 0, 20, TimeUnit.MILLISECONDS);
                    } catch (IOException e) {
                        e.printStackTrace();
                        previewHandle = null;
                        Platform.runLater(() -> HCNetSDKClientApplication.showErrorAlert("拉流失败！"));
                    }
                });
            }
        } else {
            // 不回调预览
            Stage videoStage = new Stage();
            videoStage.initOwner(HCNetSDKClientApplication.getStageManager().getPrimaryStage());
            videoStage.setTitle("VideoStage");
            videoStage.setWidth(730);
            videoStage.setHeight(730);
            videoStage.centerOnScreen();
            videoStage.setOnHiding(event -> stopPreview());
            videoStage.show();
            previewinfo.hPlayWnd = getStageHWND2(videoStage);
            result = currentDevice.getDeviceTemplate().realPlay(currentDevice.getToken(), previewinfo, null);
        }

        if (result.isSuccess()) {
            previewHandle = result.getData();
        } else {
            HCNetSDKClientApplication.showErrorAlert("预览失败, " + result.getError() + "！");
        }
    }

    public void stopPreview() {
        currentDevice.getDeviceTemplate().stopRealPlay(previewHandle);
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