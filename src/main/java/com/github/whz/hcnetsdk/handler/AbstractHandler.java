package com.github.whz.hcnetsdk.handler;

import com.github.whz.hcnetsdk.HCNetSDK.NET_DVR_ALARMER;
import com.github.whz.hcnetsdk.model.DeviceInfo;

public abstract class AbstractHandler implements Handler {

    // 解析设备信息
    protected DeviceInfo resolveDeviceInfo(NET_DVR_ALARMER alarm) {
        DeviceInfo deviceInfo = new DeviceInfo();
        if (alarm.byUserIDValid == 1) {
            deviceInfo.setUserId((long) alarm.lUserID);
        }
        if (alarm.byDeviceIPValid == 1) {
            deviceInfo.setDeviceIp(new String(alarm.sDeviceIP).trim());
        }
        if (alarm.byDeviceNameValid == 1) {
            deviceInfo.setDeviceIp(new String(alarm.sDeviceName).trim());
        }
        if (alarm.bySerialValid == 1) {
            deviceInfo.setSerialNumber(new String(alarm.sSerialNumber).trim());
        }
        if (alarm.byMacAddrValid == 1) {
            deviceInfo.setDeviceMacAddr(new String(alarm.byMacAddr).trim());
        }
        return deviceInfo;
    }
}
