package com.github.whz.hcnetsdk.operations;

import com.github.whz.hcnetsdk.HCNetSDK;
import com.github.whz.hcnetsdk.model.UpgradeAsyncResponse;
import com.github.whz.hcnetsdk.model.UpgradeResponse;
import com.sun.jna.Pointer;
import lombok.SneakyThrows;

import java.util.Date;

/**
 * 设备维护.
 */
public interface MaintainOperations extends Operations{

    /**
     * 是否在线.
     */
    boolean isOnline();

    /**
     * 重启设备.
     */
    HikResult<?> reboot();

    /**
     * 设备校时.
     */
    HikResult<Date> getDeviceTime();

    /**
     * 设备校时.
     */
    HikResult<?> setDeviceTime(Date time);

    /**
     * 升级设备(同步)
     */
    @SneakyThrows
    HikResult<UpgradeResponse> upgradeSync(HCNetSDK.NET_DVR_UPGRADE_PARAM upgradeParam);

    /**
     * 升级设备(异步).
     */
    HikResult<UpgradeAsyncResponse> upgradeAsync(HCNetSDK.NET_DVR_UPGRADE_PARAM upgradeParam);

    /**
     * 升级普通设备(同步).
     */
    @SneakyThrows
    HikResult<UpgradeResponse> upgradeSyncForDVR(String sdkPath);

    /**
     * 升级普通设备(异步).
     */
    HikResult<UpgradeAsyncResponse> upgradeAsyncForDVR(String sdkPath);

    /**
     * 升级门禁/人证机设备(同步).
     */
    HikResult<UpgradeResponse> upgradeSyncForACS(String sdkPath, int deviceNo);

    /**
     * 升级门禁/人证机器设备(异步).
     */
    HikResult<UpgradeAsyncResponse> upgradeAsyncForACS(String sdkPath, int deviceNo);

    /**
     * 获取配置.
     */
    HikResult<String> getConfig();

    /**
     * 导出设备配置.
     */
    HikResult<?> getConfigFile(String file);

    /**
     * 获取配置.
     */
    HikResult<?> setConfig(String configContent);

    /**
     * 设置配置.
     */
    HikResult<?> setConfigFile(String file);

    /**
     * 远程控制.
     */
    HikResult<?> remoteControl(int command, Pointer inBuffer, int inBufferSize);
}
