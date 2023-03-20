package io.github.wanghongzhou.hcnetsdk.operations.impl;

import com.sun.jna.Pointer;
import io.github.wanghongzhou.hcnetsdk.DeviceTemplate;
import io.github.wanghongzhou.hcnetsdk.HCNetSDK;
import io.github.wanghongzhou.hcnetsdk.model.Token;
import io.github.wanghongzhou.hcnetsdk.operations.HikResult;
import io.github.wanghongzhou.hcnetsdk.operations.MaintainOperations;

import java.util.Calendar;
import java.util.Date;

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
