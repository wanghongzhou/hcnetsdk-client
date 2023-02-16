package com.github.whz.client.config;

import com.github.whz.client.HCNetSDKClientApplication;
import com.github.whz.hcnetsdk.DeviceTemplate;
import com.github.whz.hcnetsdk.HCNetSDK;
import com.github.whz.hcnetsdk.HikDeviceTemplate;
import com.github.whz.hcnetsdk.util.JnaPathUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Brian
 */
@Configuration
public class HCNetSDKConfig {

    @Value("${hcnetsdk.logPath:./logs}")
    private String logPath;

    @Bean
    public HCNetSDK hcNetSDK() {
        JnaPathUtils.initJnaLibraryPath(HCNetSDKClientApplication.class);
        HCNetSDK hcNetSDK = HCNetSDK.INSTANCE;
        hcNetSDK.NET_DVR_Init();
        return hcNetSDK;
    }

    @Bean
    public DeviceTemplate deviceTemplate(HCNetSDK hcNetSDK) {
        HikDeviceTemplate deviceTemplate = new HikDeviceTemplate(hcNetSDK);
        deviceTemplate.opsForSdk().setLogFile(3, logPath, false);
        return deviceTemplate;
    }
}
