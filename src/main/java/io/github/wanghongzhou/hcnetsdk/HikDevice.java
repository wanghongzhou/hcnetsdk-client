package io.github.wanghongzhou.hcnetsdk;

import com.sun.jna.Structure;
import io.github.wanghongzhou.hcnetsdk.model.Token;
import io.github.wanghongzhou.hcnetsdk.operations.HikResult;
import io.github.wanghongzhou.hcnetsdk.operations.MaintainOperations;
import io.github.wanghongzhou.hcnetsdk.operations.PtzOperations;
import io.github.wanghongzhou.hcnetsdk.operations.SdkOperations;
import lombok.Getter;

import java.util.Objects;
import java.util.function.BiFunction;

/**
 * 设备.
 * <p>
 * 线程安全的.
 */
@Getter
public class HikDevice implements Device {

    private final String ip;
    private final int port;
    private final String user;
    private final String password;
    private final DeviceTemplate deviceTemplate;
    private volatile Token token;
    private int realHandle = -1;

    public HikDevice(String ip, int port, String user, String password, DeviceTemplate deviceTemplate) {

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
        return HikResult.ok(token);
    }

    @Override
    public synchronized HikResult<Void> destroy() {
        // 登录注销
        if (Objects.nonNull(token)) {
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
    public HikResult<Void> setDvrConfig(int channel, int type, Structure settings) {
        checkInit();
        return deviceTemplate.setDvrConfig(token, channel, type, settings);
    }

    @Override
    public <T extends Structure> HikResult<T> getDvrConfig(int channel, int command, Class<T> clazz) {
        checkInit();
        return deviceTemplate.getDvrConfig(token, channel, command, clazz);
    }

    @Override
    public HikResult<Integer> realPlay(HCNetSDK.FRealDataCallBack_V30 callback) {
        checkInit();
        HikResult<Integer> result = deviceTemplate.realPlay(token, callback);
        if (result.isSuccess()) {
            realHandle = result.getData();
        }
        return result;
    }

    @Override
    public HikResult<Integer> realPlay(HCNetSDK.NET_DVR_PREVIEWINFO previewInfo, HCNetSDK.FRealDataCallBack_V30 callback) {
        checkInit();
        HikResult<Integer> result = deviceTemplate.realPlay(token, previewInfo, callback);
        if (result.isSuccess()) {
            realHandle = result.getData();
        }
        return result;
    }

    @Override
    public HikResult<Void> stopRealPlay() {
        checkInit();
        HikResult<Void> result = deviceTemplate.stopRealPlay(realHandle);
        if (result.isSuccess()) {
            realHandle = -1;
        }
        return result;
    }

    @Override
    public HikResult<Void> modifyPassword(String targetUser, String newPassword) {
        checkInit();
        return deviceTemplate.modifyPassword(token, targetUser, newPassword);
    }

    @Override
    public HikResult<Void> setVideoEffect(int channel, int brightness, int contrast, int saturation, int hue) {
        checkInit();
        return deviceTemplate.setVideoEffect(token, channel, brightness, contrast, saturation, hue);
    }

    @Override
    public SdkOperations opsForSdk() {
        return deviceTemplate.opsForSdk();
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
