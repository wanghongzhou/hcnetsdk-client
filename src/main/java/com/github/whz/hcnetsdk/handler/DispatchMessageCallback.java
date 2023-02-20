package com.github.whz.hcnetsdk.handler;

import com.github.whz.hcnetsdk.HCNetSDK;
import com.sun.jna.Pointer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 基于分发的海康设备消息回调处理类.
 */
public class DispatchMessageCallback implements HCNetSDK.FMSGCallBack {

    public static final DispatchMessageCallback INSTANCE = new DispatchMessageCallback();

    private final List<Handler> handlers = new CopyOnWriteArrayList<>();

    public DispatchMessageCallback addHandler(Handler handler) {
        handlers.add(handler);
        return this;
    }

    @Override
    public void invoke(int lCommand, HCNetSDK.NET_DVR_ALARMER pAlarmer, Pointer pAlarmInfo, int dwBufLen, Pointer pUser) {
        for (Handler handler : handlers) {
            if (handler.accept(lCommand)) {
                handler.invoke(lCommand, pAlarmer, pAlarmInfo, dwBufLen, pUser);
                return;
            }
        }
    }
}
