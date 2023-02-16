package com.github.whz.hcnetsdk.model;

import com.github.whz.hcnetsdk.operations.HikResult;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 设备升级结果
 */
@Data
@SuperBuilder
@NoArgsConstructor
@Accessors(chain = true)
public class UpgradeResponse implements Serializable {

    /**
     * 升级的句柄
     */
    private long handle;

    /**
     * 升级状态
     */
    private int state;

    /**
     * 升级错误, 当state = -1时
     */
    private HikResult<?> error;

    /**
     * 是否升级成功.
     */
    public boolean isUpgradeSuccess() {
        return state == 1;
    }
}
