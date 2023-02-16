package com.github.whz.hcnetsdk.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 设备透传响应数据.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@Accessors(chain = true)
public class PassThroughResponse implements Serializable {

    /**
     * 执行透传成功返回数据.
     */
    private byte[] bytes;

    /**
     * 透传执行失败返回的详细错误信息.
     */
    private ResponseStatus status;

    /**
     * 返回字符串的数据.
     */
    public String getStringData() {
        if (bytes != null) {
            return new String(bytes).trim();
        }
        return null;
    }
}
