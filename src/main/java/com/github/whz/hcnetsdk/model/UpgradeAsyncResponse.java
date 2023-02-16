package com.github.whz.hcnetsdk.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.concurrent.Future;

/**
 * 设备升级结果.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@Accessors(chain = true)
public class UpgradeAsyncResponse implements Serializable {

    /**
     * 升级的句柄
     */
    private long handle;

    /**
     * 异步升级结果
     */
    private Future<UpgradeResponse> future;
}
