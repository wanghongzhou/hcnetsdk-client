package io.github.wanghongzhou.client.config;

import io.github.wanghongzhou.client.HCNetSDKClientApplication;
import io.github.wanghongzhou.hcnetsdk.DeviceTemplate;
import io.github.wanghongzhou.hcnetsdk.HCNetSDK;
import io.github.wanghongzhou.hcnetsdk.HikDeviceTemplate;
import io.github.wanghongzhou.hcnetsdk.util.JnaPathUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;

/**
 * @author Brian
 */
@Configuration
public class HCNetSDKConfig {

    @Value("${hcnetsdk.log.level:3}")
    private int logLevel;

    @Value("${hcnetsdk.log.path:./logs}")
    private String logPath;

    @Value("${hcnetsdk.log.autoDelete:false}")
    private boolean autoDelete;

    @Value("${hcnetsdk.timeout.connectTimeoutMs:3000}")
    private int connectTimeoutMs;

    @Value("${hcnetsdk.timeout.reconnectIntervalMs:5000}")
    private int reconnectIntervalMs;

    @Bean
    public HCNetSDK hcNetSDK() {
        JnaPathUtils.initJnaLibraryPath(HCNetSDKClientApplication.class);
        HCNetSDK hcNetSDK = HCNetSDK.INSTANCE;
        hcNetSDK.NET_DVR_Init();
        return hcNetSDK;
    }

    @Bean
    public DeviceTemplate deviceTemplate(HCNetSDK hcNetSDK, @Autowired(required = false) HCNetSDK.FExceptionCallBack fExceptionCallBack) {
        HikDeviceTemplate deviceTemplate = new HikDeviceTemplate(hcNetSDK);
        deviceTemplate.opsForSdk().setLogFile(logLevel, logPath, autoDelete);
        deviceTemplate.opsForSdk().setTimeout(connectTimeoutMs, reconnectIntervalMs);
        if (Objects.nonNull(fExceptionCallBack)) {
            deviceTemplate.opsForSdk().setExceptionCallBack(0, 0, fExceptionCallBack, null);
        }
        return deviceTemplate;
    }
}
