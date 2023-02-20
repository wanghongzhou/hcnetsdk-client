package com.github.whz.hcnetsdk.model;

import com.sun.jna.NativeLong;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 海康设备登录响应结果
 */
@Data
@SuperBuilder
@NoArgsConstructor
@Accessors(chain = true)
public class Token implements Serializable {

    /**
     * 登录后的用户标识
     */
    private int userId;

    /**
     * 登录的设备信息
     */
    private Object deviceInfo;
}
