package io.github.wanghongzhou.hcnetsdk;

import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;
import io.github.wanghongzhou.hcnetsdk.model.Token;
import io.github.wanghongzhou.hcnetsdk.operations.HikResult;
import io.github.wanghongzhou.hcnetsdk.operations.MaintainOperations;
import io.github.wanghongzhou.hcnetsdk.operations.PtzOperations;
import io.github.wanghongzhou.hcnetsdk.operations.SdkOperations;
import io.github.wanghongzhou.hcnetsdk.operations.impl.AbstractOperations;
import io.github.wanghongzhou.hcnetsdk.operations.impl.MaintainOperationsImpl;
import io.github.wanghongzhou.hcnetsdk.operations.impl.PtzOperationsImpl;
import io.github.wanghongzhou.hcnetsdk.operations.impl.SdkOperationsImpl;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;

import java.util.Objects;
import java.util.function.BiFunction;

/**
 * 海康SDK工具类.
 */
public class HikDeviceTemplate extends AbstractOperations implements DeviceTemplate {

    @Getter
    @NonNull
    private final HCNetSDK hcnetsdk;

    public HikDeviceTemplate(@NonNull HCNetSDK hcnetsdk) {
        super(hcnetsdk);
        this.hcnetsdk = hcnetsdk;
    }

    @Override
    public HikResult<Token> login(String ip, int port, String user, String password) {
        HCNetSDK.NET_DVR_USER_LOGIN_INFO loginInfo = new HCNetSDK.NET_DVR_USER_LOGIN_INFO();
        System.arraycopy(ip.getBytes(), 0, loginInfo.sDeviceAddress, 0, ip.length());
        System.arraycopy(user.getBytes(), 0, loginInfo.sUserName, 0, user.length());
        System.arraycopy(password.getBytes(), 0, loginInfo.sPassword, 0, password.length());
        loginInfo.wPort = (short) port;
        loginInfo.bUseAsynLogin = false;
        loginInfo.write();

        HCNetSDK.NET_DVR_DEVICEINFO_V40 deviceInfo = new HCNetSDK.NET_DVR_DEVICEINFO_V40();
        int userId = hcnetsdk.NET_DVR_Login_V40(loginInfo, deviceInfo);
        deviceInfo.read();
        if (userId == -1) {
            return lastError();
        }
        return HikResult.ok(Token.builder().userId(userId).deviceInfo(deviceInfo).build());
    }

    @Override
    public HikResult<Void> logout(Token token) {
        if (Objects.nonNull(token)) {
            if (!hcnetsdk.NET_DVR_Logout(token.getUserId())) {
                return lastError();
            }
        }
        return HikResult.ok();
    }

    @Override
    public <T> HikResult<T> doAction(String ip, int port, String user, String password, BiFunction<HCNetSDK, Token, HikResult<T>> action) {
        HikResult<Token> loginResult = login(ip, port, user, password);
        if (!loginResult.isSuccess()) {
            return HikResult.fail(loginResult.getErrorCode(), loginResult.getErrorMsg());
        }

        Token token = loginResult.getData();
        try {
            HikResult<T> result = action.apply(hcnetsdk, token);
            if (Objects.isNull(result)) {
                result = HikResult.ok();
            }
            return result;
        } finally {
            logout(token);
        }
    }

    @Override
    public HikResult<Void> modifyPassword(Token token, String username, String newPassword) {
        // 获取原始配置
        HCNetSDK.NET_DVR_USER_V30 dvrUser = new HCNetSDK.NET_DVR_USER_V30();
        boolean getResult = hcnetsdk.NET_DVR_GetDVRConfig(token.getUserId(), HCNetSDK.NET_DVR_GET_USERCFG_V30, 0, dvrUser.getPointer(), dvrUser.size(), new IntByReference(0));
        if (!getResult) {
            return lastError();
        }

        // 修改指定用户密码
        dvrUser.read();
        for (HCNetSDK.NET_DVR_USER_INFO_V30 userInfo : dvrUser.struUser) {
            String name = new String(userInfo.sUserName).trim();
            if (Objects.equals(username, name)) {
                userInfo.sPassword = newPassword.getBytes();
            }
        }
        dvrUser.write();
        boolean setResult = hcnetsdk.NET_DVR_SetDVRConfig(token.getUserId(), HCNetSDK.NET_DVR_SET_USERCFG_V30, 0, dvrUser.getPointer(), dvrUser.dwSize);
        if (!setResult) {
            return lastError();
        }
        return HikResult.ok();
    }

    @Override
    public HikResult<Void> setVideoEffect(Token token, int channel, int brightness, int contrast, int saturation, int hue) {
        boolean getResult = hcnetsdk.NET_DVR_SetVideoEffect(token.getUserId(), channel, brightness, contrast, saturation, hue);
        if (!getResult) {
            return lastError();
        }
        return HikResult.ok();
    }

    @Override
    @SneakyThrows
    public <T extends Structure> HikResult<T> getDvrConfig(Token token, int channel, int command, Class<T> clazz) {
        T data = clazz.getDeclaredConstructor().newInstance();
        data.write();
        boolean result = hcnetsdk.NET_DVR_GetDVRConfig(token.getUserId(), command, channel, data.getPointer(), data.size(), new IntByReference(0));
        if (!result) {
            return lastError();
        }
        data.read();
        return HikResult.ok(data);
    }

    @Override
    public HikResult<?> getDvrConfig(Token token, int channel, int command, Structure data) {
        data.write();
        boolean result = hcnetsdk.NET_DVR_GetDVRConfig(token.getUserId(), command, channel, data.getPointer(), data.size(), new IntByReference(0));
        if (!result) {
            return lastError();
        }
        data.read();
        return HikResult.ok();
    }

    @Override
    public HikResult<Void> setDvrConfig(Token token, int channel, int command, Structure data) {
        data.write();
        boolean result = hcnetsdk.NET_DVR_SetDVRConfig(token.getUserId(), command, channel, data.getPointer(), data.size());
        if (!result) {
            return lastError();
        }
        return HikResult.ok();
    }

    @Override
    public HikResult<Integer> realPlay(Token token, HCNetSDK.FRealDataCallBack_V30 callback) {
        HCNetSDK.NET_DVR_PREVIEWINFO previewInfo = new HCNetSDK.NET_DVR_PREVIEWINFO();
        previewInfo.lChannel = 1;
        previewInfo.dwStreamType = 0;
        previewInfo.dwLinkMode = 1;
        previewInfo.hPlayWnd = null;
        previewInfo.bBlocked = 0;
        previewInfo.bPassbackRecord = 0;
        previewInfo.byPreviewMode = 0;
        return realPlay(token, previewInfo, callback);
    }

    @Override
    public HikResult<Integer> realPlay(Token token, HCNetSDK.NET_DVR_PREVIEWINFO previewInfo, HCNetSDK.FRealDataCallBack_V30 callback) {
        int realPlayHandle = hcnetsdk.NET_DVR_RealPlay_V40(token.getUserId(), previewInfo, callback, null);
        if (realPlayHandle == -1) {
            return lastError();
        }
        return HikResult.ok(realPlayHandle);
    }

    @Override
    public HikResult<Void> stopRealPlay(int realHandle) {
        boolean result = hcnetsdk.NET_DVR_StopRealPlay(realHandle);
        return result ? HikResult.ok() : lastError();
    }

    @Override
    public SdkOperations opsForSdk() {
        return new SdkOperationsImpl(getHcnetsdk());
    }

    @Override
    public PtzOperations opsForPtz(Token token) {
        return new PtzOperationsImpl(token, getHcnetsdk());
    }

    @Override
    public MaintainOperations opsForMaintain(Token token) {
        return new MaintainOperationsImpl(token, this);
    }
}
