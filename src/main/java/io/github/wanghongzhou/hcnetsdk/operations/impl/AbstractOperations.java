package io.github.wanghongzhou.hcnetsdk.operations.impl;

import com.sun.jna.ptr.IntByReference;
import io.github.wanghongzhou.hcnetsdk.HCNetSDK;
import io.github.wanghongzhou.hcnetsdk.operations.HikResult;
import io.github.wanghongzhou.hcnetsdk.operations.Operations;

public abstract class AbstractOperations implements Operations {

    protected final HCNetSDK hcNetSDK;

    public AbstractOperations(HCNetSDK hcNetSDK) {
        this.hcNetSDK = hcNetSDK;
    }

    @Override
    public HCNetSDK getHcnetsdk() {
        return this.hcNetSDK;
    }

    public <T> HikResult<T> lastError() {
        int code = hcNetSDK.NET_DVR_GetLastError();
        if (code == 0) {
            return null;
        }
        if (code == 3) {
            return HikResult.fail(code, "sdk not init.");
        }
        return HikResult.fail(code, hcNetSDK.NET_DVR_GetErrorMsg(new IntByReference(code)));
    }
}
