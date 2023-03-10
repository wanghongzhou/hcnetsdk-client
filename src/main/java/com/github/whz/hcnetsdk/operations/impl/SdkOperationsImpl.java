package com.github.whz.hcnetsdk.operations.impl;


import com.github.whz.hcnetsdk.HCNetSDK;
import com.github.whz.hcnetsdk.operations.HikResult;
import com.github.whz.hcnetsdk.operations.SdkOperations;

/**
 * sdk本地功能.
 */
public class SdkOperationsImpl extends AbstractOperations implements SdkOperations {

    public SdkOperationsImpl(HCNetSDK hcNetSDK) {
        super(hcNetSDK);
    }

    @Override
    public String getVersion() {
        int buildVersion = getHcnetsdk().NET_DVR_GetSDKBuildVersion();
        return (buildVersion >> 24) + "." + (buildVersion << 8 >> 24) + "." + (buildVersion << 16 >> 16);
    }

    @Override
    public HikResult<HCNetSDK.NET_DVR_SDKABL> getAbility() {
        HCNetSDK.NET_DVR_SDKABL ability = new HCNetSDK.NET_DVR_SDKABL();
        if (!getHcnetsdk().NET_DVR_GetSDKAbility(ability)) {
            return lastError();
        }
        ability.read();
        return HikResult.ok(ability);
    }

    @Override
    public HikResult<HCNetSDK.NET_DVR_SDKSTATE> getState() {
        HCNetSDK.NET_DVR_SDKSTATE sdkState = new HCNetSDK.NET_DVR_SDKSTATE();
        if (!getHcnetsdk().NET_DVR_GetSDKState(sdkState)) {
            return lastError();
        }
        sdkState.read();
        return HikResult.ok(sdkState);
    }

    @Override
    public HikResult<Void> setLogFile(int logLevel, String logDir, boolean autoDel) {
        if (!getHcnetsdk().NET_DVR_SetLogToFile(logLevel, logDir, autoDel)) {
            return lastError();
        }
        return HikResult.ok();
    }

    @Override
    public HikResult<Void> setTimeout(int connectTimeoutMs, int recvTimeoutMs, int reconnectIntervalMs) {
        if (!getHcnetsdk().NET_DVR_SetConnectTime(connectTimeoutMs, 3)) {
            return lastError();
        }
//        if (!getHcnetsdk().NET_DVR_SetRecvTimeOut(recvTimeoutMs)) {
//            return lastError();
//        }
        if (!getHcnetsdk().NET_DVR_SetReconnect(reconnectIntervalMs, reconnectIntervalMs > 0)) {
            return lastError();
        }
        return HikResult.ok();
    }
}
