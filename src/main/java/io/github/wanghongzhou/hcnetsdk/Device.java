package io.github.wanghongzhou.hcnetsdk;


import com.sun.jna.Structure;
import io.github.wanghongzhou.hcnetsdk.model.Token;
import io.github.wanghongzhou.hcnetsdk.operations.HikResult;
import io.github.wanghongzhou.hcnetsdk.operations.MaintainOperations;
import io.github.wanghongzhou.hcnetsdk.operations.PtzOperations;
import io.github.wanghongzhou.hcnetsdk.operations.SdkOperations;

import java.util.function.BiFunction;

/**
 * 设备操作接口.
 */
public interface Device {

    /**
     * 初始化.
     */
    HikResult<Token> init();

    /**
     * 销毁.
     */
    HikResult<Void> destroy();

    /**
     * 执行动作.
     */
    <T> HikResult<T> doAction(BiFunction<HCNetSDK, Token, HikResult<T>> action);

    /**
     * 设置设备配置.
     */
    HikResult<Void> setDvrConfig(int channel, int command, Structure settings);

    /**
     * 获取设备配置.
     */
    <T extends Structure> HikResult<T> getDvrConfig(int channel, int command, Class<T> clazz);

    /**
     * 设置视频实时预览
     */
    HikResult<Integer> realPlay(HCNetSDK.FRealDataCallBack_V30 callback);

    /**
     * 设置实时预览
     */
    HikResult<Integer> realPlay(HCNetSDK.NET_DVR_PREVIEWINFO previewInfo, HCNetSDK.FRealDataCallBack_V30 callback);

    /**
     * 停止实时预览
     */
    HikResult<Void> stopRealPlay();

    /**
     * 修改指定用户密码.
     */
    HikResult<Void> modifyPassword(String targetUser, String newPassword);

    /**
     * 设置视频效果
     */
    HikResult<Void> setVideoEffect(int channel, int brightness, int contrast, int saturation, int hue);

    /**
     * SDK操作.
     */
    SdkOperations opsForSdk();

    /**
     * 云台操作.
     */
    PtzOperations opsForPtz();

    /**
     * 设备维护.
     */
    MaintainOperations opsForMaintain();
}
