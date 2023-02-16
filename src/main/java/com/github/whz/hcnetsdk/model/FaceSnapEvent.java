package com.github.whz.hcnetsdk.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 人脸抓拍事件.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@Accessors(chain = true)
public class FaceSnapEvent implements Serializable {

    private FaceSnapInfo faceSnapInfo;
    private DeviceInfo deviceInfo;
}
