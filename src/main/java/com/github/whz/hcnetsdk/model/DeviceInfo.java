package com.github.whz.hcnetsdk.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 设备信息
 */
@Data
@SuperBuilder
@NoArgsConstructor
@Accessors(chain = true)
public class DeviceInfo implements Serializable {

    /**
     * 登录标识
     */
    private Long userId;

    /**
     * 设备ip地址
     */
    private String deviceIp;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 设备序列号
     */
    private String serialNumber;

    /**
     * 设备mac地址
     */
    private String deviceMacAddr;
}
