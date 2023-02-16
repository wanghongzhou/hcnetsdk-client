package com.github.whz.hcnetsdk.operations.impl;

import com.github.whz.hcnetsdk.DeviceTemplate;
import com.github.whz.hcnetsdk.HCNetSDK;
import com.github.whz.hcnetsdk.model.Token;
import com.github.whz.hcnetsdk.model.UpgradeAsyncResponse;
import com.github.whz.hcnetsdk.model.UpgradeResponse;
import com.github.whz.hcnetsdk.operations.HikResult;
import com.github.whz.hcnetsdk.operations.MaintainOperations;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import lombok.SneakyThrows;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

/**
 * 设备维护.
 */
public class MaintainOperationsImpl extends AbstractOperations implements MaintainOperations {

    private final Token token;
    private final DeviceTemplate deviceTemplate;

    public MaintainOperationsImpl(Token token, DeviceTemplate deviceTemplate) {
        super(deviceTemplate.getHcnetsdk());
        this.token = token;
        this.deviceTemplate = deviceTemplate;
    }

    @Override
    public boolean isOnline() {
        return getHcnetsdk().NET_DVR_RemoteControl(token.getUserId(), 20005, null, 0);
    }

    @Override
    public HikResult<?> reboot() {
        boolean rebootResult = getHcnetsdk().NET_DVR_RebootDVR(token.getUserId());
        if (!rebootResult) {
            return lastError();
        }
        return HikResult.ok();
    }

    @Override
    public HikResult<Date> getDeviceTime() {
        HCNetSDK.NET_DVR_TIME netDvrTime = new HCNetSDK.NET_DVR_TIME();
        HikResult<?> result = deviceTemplate.getDvrConfig(token, 0, HCNetSDK.NET_DVR_GET_TIMECFG, netDvrTime);
        if (!result.isSuccess()) {
            return HikResult.fail(result.getErrorCode(), result.getErrorMsg());
        }
        netDvrTime.read();

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, netDvrTime.dwYear);
        calendar.set(Calendar.MONTH, netDvrTime.dwMonth - 1);
        calendar.set(Calendar.DAY_OF_MONTH, netDvrTime.dwDay);
        calendar.set(Calendar.HOUR_OF_DAY, netDvrTime.dwHour);
        calendar.set(Calendar.MINUTE, netDvrTime.dwMinute);
        calendar.set(Calendar.SECOND, netDvrTime.dwSecond);
        return HikResult.ok(calendar.getTime());
    }

    @Override
    public HikResult<?> setDeviceTime(Date time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(time);

        HCNetSDK.NET_DVR_TIME netDvrTime = new HCNetSDK.NET_DVR_TIME();
        netDvrTime.dwYear = calendar.get(Calendar.YEAR);
        netDvrTime.dwMonth = calendar.get(Calendar.MONTH) + 1;
        netDvrTime.dwDay = calendar.get(Calendar.DAY_OF_MONTH);
        netDvrTime.dwHour = calendar.get(Calendar.HOUR_OF_DAY);
        netDvrTime.dwMinute = calendar.get(Calendar.MINUTE);
        netDvrTime.dwSecond = calendar.get(Calendar.SECOND);
        return deviceTemplate.setDvrConfig(token, 0, HCNetSDK.NET_DVR_SET_TIMECFG, netDvrTime);
    }

    @Override
    @SneakyThrows
    public HikResult<UpgradeResponse> upgradeSyncForDVR(String sdkPath) {
        HikResult<UpgradeAsyncResponse> upgradeResult = this.upgradeAsyncForDVR(sdkPath);
        if (!upgradeResult.isSuccess()) {
            return HikResult.fail(upgradeResult.getErrorCode(), upgradeResult.getErrorMsg());
        }
        UpgradeAsyncResponse asyncResponse = upgradeResult.getData();
        UpgradeResponse response = asyncResponse.getFuture().get();
        return HikResult.ok(response);
    }

    @Override
    public HikResult<UpgradeResponse> upgradeSyncForACS(String sdkPath, int deviceNo) {
        HCNetSDK.NET_DVR_UPGRADE_PARAM upgradeParam = new HCNetSDK.NET_DVR_UPGRADE_PARAM();
        upgradeParam.dwUpgradeType = 0;
        upgradeParam.sFilename = sdkPath;
        upgradeParam.pInbuffer = new IntByReference(deviceNo).getPointer();
        upgradeParam.dwBufferLen = 4;
        return upgradeSync(upgradeParam);
    }

    @Override
    @SneakyThrows
    public HikResult<UpgradeResponse> upgradeSync(HCNetSDK.NET_DVR_UPGRADE_PARAM upgradeParam) {
        HikResult<UpgradeAsyncResponse> upgradeResult = this.upgradeAsync(upgradeParam);
        if (!upgradeResult.isSuccess()) {
            return HikResult.fail(upgradeResult.getErrorCode(), upgradeResult.getErrorMsg());
        }
        UpgradeAsyncResponse asyncResponse = upgradeResult.getData();
        UpgradeResponse response = asyncResponse.getFuture().get();
        return HikResult.ok(response);
    }

    @Override
    public HikResult<UpgradeAsyncResponse> upgradeAsyncForDVR(String sdkPath) {
        HCNetSDK.NET_DVR_UPGRADE_PARAM upgradeParam = new HCNetSDK.NET_DVR_UPGRADE_PARAM();
        upgradeParam.dwUpgradeType = 0;
        upgradeParam.sFilename = sdkPath;
        return upgradeAsync(upgradeParam);
    }

    @Override
    public HikResult<UpgradeAsyncResponse> upgradeAsyncForACS(String sdkPath, int deviceNo) {
        HCNetSDK.NET_DVR_UPGRADE_PARAM upgradeParam = new HCNetSDK.NET_DVR_UPGRADE_PARAM();
        upgradeParam.dwUpgradeType = 0;
        upgradeParam.sFilename = sdkPath;
        upgradeParam.pInbuffer = new IntByReference(deviceNo).getPointer();
        upgradeParam.dwBufferLen = 4;
        return upgradeAsync(upgradeParam);
    }

    @Override
    public HikResult<UpgradeAsyncResponse> upgradeAsync(HCNetSDK.NET_DVR_UPGRADE_PARAM upgradeParam) {
        // 请求升级
        upgradeParam.write();
        final NativeLong upgradeHandle = getHcnetsdk().NET_DVR_Upgrade_V50(token.getUserId(), upgradeParam);
        if (upgradeHandle.longValue() == -1) {
            return lastError();
        }

        // 获取结果, 并关闭资源
        FutureTask<UpgradeResponse> future = new FutureTask<>(new Callable<UpgradeResponse>() {
            @Override
            public UpgradeResponse call() throws Exception {
                int state;
                int errorTimes = 0;
                do {
                    Thread.sleep(TimeUnit.SECONDS.toMillis(15));
                    state = getHcnetsdk().NET_DVR_GetUpgradeState(upgradeHandle);
                    if (state == -1) {
                        errorTimes++;
                    } else {
                        errorTimes = 0;
                    }
                } while (state == 2 || (state == -1 && errorTimes >= 3));
                UpgradeResponse response = new UpgradeResponse();
                response.setHandle(upgradeHandle.longValue());
                response.setState(state);
                if (state == -1) {
                    response.setError(lastError());
                } else {
                    // 关闭升级句柄
                    getHcnetsdk().NET_DVR_CloseUpgradeHandle(upgradeHandle);
                }
                return response;
            }
        });
        new Thread(future).start();

        UpgradeAsyncResponse response = new UpgradeAsyncResponse();
        response.setHandle(upgradeHandle.longValue());
        response.setFuture(future);
        return HikResult.ok(response);
    }


    @Override
    public HikResult<String> getConfig() {
        HCNetSDK.NET_DVR_STRING_POINTER out = new HCNetSDK.NET_DVR_STRING_POINTER();
        out.byString = new byte[10 * 1024 * 1024];
        out.write();

        IntByReference returnSize = new IntByReference();
        boolean result = getHcnetsdk().NET_DVR_GetConfigFile_V30(token.getUserId(), out.getPointer(), out.byString.length, returnSize);
        if (!result) {
            return lastError();
        }
        out.read();
        return HikResult.ok();
    }

    @Override
    public HikResult<?> getConfigFile(String file) {
        boolean result = getHcnetsdk().NET_DVR_GetConfigFile(token.getUserId(), file);
        if (!result) {
            return lastError();
        }
        return HikResult.ok();
    }

    @Override
    public HikResult<?> setConfig(String configContent) {
        boolean result = getHcnetsdk().NET_DVR_SetConfigFile_EX(token.getUserId(), configContent, configContent.getBytes().length);
        if (!result) {
            lastError();
        }
        return HikResult.ok();
    }

    @Override
    public HikResult<?> setConfigFile(String file) {
        boolean result = getHcnetsdk().NET_DVR_SetConfigFile(token.getUserId(), file);
        if (!result) {
            return lastError();
        }
        return HikResult.ok();
    }

    @Override
    public HikResult<?> remoteControl(int command, Pointer inBuffer, int inBufferSize) {
        boolean result = getHcnetsdk().NET_DVR_RemoteControl(token.getUserId(), command, inBuffer, inBufferSize);
        if (!result) {
            return lastError();
        }
        return HikResult.ok();
    }

}
