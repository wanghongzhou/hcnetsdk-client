package com.github.whz.hcnetsdk.handler;

import com.github.whz.hcnetsdk.HCNetSDK;
import com.sun.jna.NativeLong;
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
    public void invoke(NativeLong lCommand, HCNetSDK.NET_DVR_ALARMER pAlarmer, HCNetSDK.RECV_ALARM pAlarmInfo, int dwBufLen, Pointer pUser) {
        int cmd = lCommand.intValue();
        for (Handler handler : handlers) {
            if (handler.accept(cmd)) {
                handler.invoke(lCommand, pAlarmer, pAlarmInfo, dwBufLen, pUser);
                return;
            }
        }
    }
}
