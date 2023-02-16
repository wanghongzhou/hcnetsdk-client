package com.github.whz.hcnetsdk.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 人证机刷证事件数据.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@Accessors(chain = true)
public class FreshCardEvent implements Serializable {

    private IDCardInfo cardInfo;

    private DeviceInfo deviceInfo;
}
