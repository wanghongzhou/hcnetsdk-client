package com.github.whz.hcnetsdk;

import com.github.whz.hcnetsdk.model.PassThroughResponse;
import com.github.whz.hcnetsdk.model.Token;
import com.github.whz.hcnetsdk.operations.HikResult;
import com.github.whz.hcnetsdk.operations.MaintainOperations;
import com.github.whz.hcnetsdk.operations.PtzOperations;
import com.github.whz.hcnetsdk.operations.impl.AbstractOperations;
import com.sun.jna.NativeLong;
import com.sun.jna.Structure;
import lombok.Getter;

import java.util.Objects;
import java.util.function.BiFunction;

/**
 * 设备.
 * <p>
 * 线程安全的.
 */
public class HikDevice extends AbstractOperations implements Device {

    @Getter
    private final String ip;

    @Getter
    private final int port;

    @Getter
    private final String user;

    @Getter
    private final String password;

    @Getter
    private final DeviceTemplate deviceTemplate;

    @Getter
    private volatile Token token;

    private volatile Long setupAlarmHandle;

    public HikDevice(String ip, int port, String user, String password, DeviceTemplate deviceTemplate) {
        super(deviceTemplate.getHcnetsdk());
        this.ip = ip;
        this.port = port;
        this.user = user;
        this.password = password;
        this.deviceTemplate = deviceTemplate;
    }

    @Override
    public HikResult<Token> init() {
        if (Objects.isNull(token)) {
            synchronized (this) {
                if (Objects.isNull(token)) {
                    HikResult<Token> loginResult = deviceTemplate.login(ip, port, user, password);
                    if (loginResult.isSuccess()) {
                        token = loginResult.getData();
                    }
                    return loginResult;
                }
            }
        }
        return HikResult.ok();
    }

    @Override
    public synchronized HikResult<Void> destroy() {
        // 消息回调取消布防
        if (Objects.nonNull(setupAlarmHandle)) {
            deviceTemplate.getHcnetsdk().NET_DVR_CloseAlarmChan_V30(new NativeLong(setupAlarmHandle));
            setupAlarmHandle = null;
        }

        // 登录注销
        if (Objects.nonNull(token) && Objects.nonNull(token.getUserId())) {
            deviceTemplate.logout(token);
        }
        return HikResult.ok();
    }

    @Override
    public <T> HikResult<T> doAction(BiFunction<HCNetSDK, Token, HikResult<T>> action) {
        checkInit();
        return action.apply(deviceTemplate.getHcnetsdk(), token);
    }

    @Override
    public HikResult<Long> setupDeploy(HCNetSDK.FMSGCallBack messageCallback, HCNetSDK.FExceptionCallBack exceptionCallback) {
        checkInit();
        if (Objects.nonNull(setupAlarmHandle)) {
            throw new RuntimeException("重复布防.");
        }
        HikResult<Long> deployResult = deviceTemplate.setupDeploy(token, messageCallback, exceptionCallback);
        if (deployResult.isSuccess() && Objects.nonNull(deployResult.getData())) {
            setupAlarmHandle = deployResult.getData();
        }
        return deployResult;
    }

    @Override
    public HikResult<PassThroughResponse> passThrough(String url, String data) {
        checkInit();
        return deviceTemplate.passThrough(token, url, data);
    }

    @Override
    public HikResult<PassThroughResponse> passThrough(String url, String data, int exceptOutByteSize) {
        checkInit();
        return deviceTemplate.passThrough(token, url, data.getBytes(), exceptOutByteSize);
    }

    @Override
    public <T extends Structure> HikResult<T> getDvrConfig(long channel, int command, Class<T> clazz) {
        checkInit();
        return deviceTemplate.getDvrConfig(token, channel, command, clazz);
    }

    @Override
    public HikResult<Void> setDvrConfig(long channel, int type, Structure settings) {
        checkInit();
        return deviceTemplate.setDvrConfig(token, channel, type, settings);
    }

    @Override
    public HikResult<Void> modifyPassword(String targetUser, String newPassword) {
        checkInit();
        return deviceTemplate.modifyPassword(token, targetUser, newPassword);
    }

    @Override
    public PtzOperations opsForPtz() {
        checkInit();
        return deviceTemplate.opsForPtz(token);
    }

    @Override
    public MaintainOperations opsForMaintain() {
        checkInit();
        return deviceTemplate.opsForMaintain(token);
    }

    private void checkInit() {
        HikResult<?> result = init();
        if (!result.isSuccess()) {
            throw new RuntimeException(result.getError());
        }
    }
}
