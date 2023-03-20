package io.github.wanghongzhou.hcnetsdk;

import com.sun.jna.Structure;
import io.github.wanghongzhou.hcnetsdk.model.Token;
import io.github.wanghongzhou.hcnetsdk.operations.*;

import java.util.function.BiFunction;

public interface DeviceTemplate extends Operations {

    /**
     * 登录设备
     */
    HikResult<Token> login(String ip, int port, String user, String password);

    /**
     * 注销登录
     */
    HikResult<Void> logout(Token token);

    /**
     * 执行动作
     */
    <T> HikResult<T> doAction(String ip, int port, String user, String password, BiFunction<HCNetSDK, Token, HikResult<T>> action);

    /**
     * 获取设备配置数据.
     */
    <T extends Structure> HikResult<T> getDvrConfig(Token token, int channel, int command, Class<T> clazz);

    /**
     * 获取设备配置数据.
     */
    HikResult<?> getDvrConfig(Token token, int channel, int command, Structure data);

    /**
     * 设置设备配置数据.
     */
    HikResult<Void> setDvrConfig(Token token, int channel, int command, Structure data);

    /**
     * 设置视频实时预览
     */
    HikResult<Integer> realPlay(Token token, HCNetSDK.FRealDataCallBack_V30 callback);

    /**
     * 设置实时预览
     */
    HikResult<Integer> realPlay(Token token, HCNetSDK.NET_DVR_PREVIEWINFO previewInfo, HCNetSDK.FRealDataCallBack_V30 callback);

    /**
     * 停止实时预览
     */
    HikResult<Void> stopRealPlay(int realHandle);

    /**
     * 修改设备密码.
     */
    HikResult<Void> modifyPassword(Token token, String username, String newPassword);

    /**
     * 设置视频效果
     */
    HikResult<Void> setVideoEffect(Token token, int channel, int brightness, int contrast, int saturation, int hue);

    /**
     * 本地sdk操作.
     */
    SdkOperations opsForSdk();

    /**
     * 云台操作.
     */
    PtzOperations opsForPtz(Token token);

    /**
     * 设备维护.
     */
    MaintainOperations opsForMaintain(Token token);
}
