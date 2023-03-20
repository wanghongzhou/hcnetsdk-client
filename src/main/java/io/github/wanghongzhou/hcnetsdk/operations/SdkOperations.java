package io.github.wanghongzhou.hcnetsdk.operations;

import com.sun.jna.Pointer;
import io.github.wanghongzhou.hcnetsdk.HCNetSDK;

/**
 * sdk本地功能.
 */
public interface SdkOperations extends Operations {

    /**
     * 获取sdk版本信息
     */
    String getVersion();

    /**
     * 获取sdk的能力
     */
    HikResult<HCNetSDK.NET_DVR_SDKABL> getAbility();

    /**
     * 获取sdk当前状态信息.
     */
    HikResult<HCNetSDK.NET_DVR_SDKSTATE> getState();

    /**
     * 设置sdk日志输出, 参考: NET_DVR_SetLogToFile
     */
    HikResult<Void> setLogFile(int logLevel, String logDir, boolean autoDel);

    /**
     * 设置sdk超时配置.
     *
     * @param connectTimeoutMs    连接超时时间，默认3000毫秒, 取值范围[300,75000]
     * @param reconnectIntervalMs 重连时间间隔, 默认5000毫秒, 最小为 3000 毫秒
     */
    HikResult<Void> setTimeout(int connectTimeoutMs, int reconnectIntervalMs);

    /**
     * 设置异常回调
     *
     * @param nMessage           消息，Linux下该参数保留
     * @param hWnd               接收异常信息消息的窗口句柄，Linux下该参数保留
     * @param fExceptionCallBack 接收异常消息的回调函数，回调当前异常的相关信息
     * @param pUser              用户数据
     */
    HikResult<Void> setExceptionCallBack(int nMessage, int hWnd, HCNetSDK.FExceptionCallBack fExceptionCallBack, Pointer pUser);
}
