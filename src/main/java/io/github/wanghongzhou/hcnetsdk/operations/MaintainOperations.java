package io.github.wanghongzhou.hcnetsdk.operations;

import com.sun.jna.Pointer;

import java.util.Date;

/**
 * 设备维护.
 */
public interface MaintainOperations extends Operations {

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
