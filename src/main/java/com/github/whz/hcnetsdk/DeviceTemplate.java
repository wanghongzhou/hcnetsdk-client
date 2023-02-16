package com.github.whz.hcnetsdk;

import com.github.whz.hcnetsdk.model.PassThroughResponse;
import com.github.whz.hcnetsdk.model.Token;
import com.github.whz.hcnetsdk.operations.*;
import com.sun.jna.Structure;
import lombok.SneakyThrows;

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
     * 设备透传, 实现数据获取或配置修改.
     */
    HikResult<PassThroughResponse> passThrough(Token token, String url, String input);

    /**
     * 设备透传, 实现数据获取或配置修改.
     */
    HikResult<PassThroughResponse> passThrough(Token token, String url, byte[] inputBytes, int exceptOutByteSize);

    /**
     * 布防.
     * <p>
     * 包括3个步骤: a.设置回调消息, b.建立上传通道, c.设置异常回调.
     */
    HikResult<Long> setupDeploy(Token token, HCNetSDK.FMSGCallBack messageCallback, HCNetSDK.FExceptionCallBack exceptionCallback);

    /**
     * 修改设备密码.
     */
    HikResult<Void> modifyPassword(Token token, String username, String newPassword);

    /**
     * NVR重新绑定通道, 抓拍机修改密码后需要重新绑定.
     */
    HikResult<?> nvrRebindChannels(Token token, String dvrUsername, String dvrNewPassword);

    /**
     * 获取设备配置数据.
     */
    @SneakyThrows
    <T extends Structure> HikResult<T> getDvrConfig(Token token, long channel, int command, Class<T> clazz);

    /**
     * 获取设备配置数据.
     */
    HikResult<?> getDvrConfig(Token token, long channel, int command, Structure data);

    /**
     * 设置设备配置数据.
     */
    HikResult<Void> setDvrConfig(Token token, long channel, int command, Structure data);

    /**
     * 设置视频实时预览
     */
    HikResult<Long> realPlay(Token token, HCNetSDK.FRealDataCallBack_V30 callback);

    /**
     * 设置实时预览
     */
    HikResult<Long> realPlay(Token token, HCNetSDK.NET_DVR_PREVIEWINFO previewInfo, HCNetSDK.FRealDataCallBack_V30 callback);

    /**
     * 停止实时预览
     */
    HikResult<Void> stopRealPlay(long realHandle);

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
