package com.github.whz.hcnetsdk;

import com.github.whz.hcnetsdk.model.PassThroughResponse;
import com.github.whz.hcnetsdk.model.Token;
import com.github.whz.hcnetsdk.operations.HikResult;
import com.github.whz.hcnetsdk.operations.MaintainOperations;
import com.github.whz.hcnetsdk.operations.Operations;
import com.github.whz.hcnetsdk.operations.PtzOperations;
import com.sun.jna.Structure;

import java.util.function.BiFunction;

/**
 * 设备操作接口.
 */
public interface Device extends Operations {

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
     * 布防.
     */
    HikResult<Long> setupDeploy(HCNetSDK.FMSGCallBack messageCallback, HCNetSDK.FExceptionCallBack exceptionCallback);

    /**
     * 透传.
     */
    HikResult<PassThroughResponse> passThrough(String url, String data);

    /**
     * 透传.
     */
    HikResult<PassThroughResponse> passThrough(String url, String data, int exceptOutByteSize);

    /**
     * 获取设备配置.
     */
    <T extends Structure> HikResult<T> getDvrConfig(long channel, int command, Class<T> clazz);

    /**
     * 设置设备配置.
     */
    HikResult<Void> setDvrConfig(long channel, int command, Structure settings);

    /**
     * 修改指定用户密码.
     */
    HikResult<Void> modifyPassword(String targetUser, String newPassword);

    /**
     * 云台操作.
     */
    PtzOperations opsForPtz();

    /**
     * 设备维护.
     */
    MaintainOperations opsForMaintain();
}
