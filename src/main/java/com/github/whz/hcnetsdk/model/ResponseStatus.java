package com.github.whz.hcnetsdk.model;

import com.github.whz.hcnetsdk.util.InnerUtils;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.Map;

/**
 * 错误响应结果.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@Accessors(chain = true)
public class ResponseStatus implements Serializable {

    /**
     * 请求的地址.
     */
    private String requestURL;

    /**
     * 状态码.
     */
    private Integer statusCode;

    /**
     * 子状态码.
     */
    private String subStatusCode;

    /**
     * 状态码文字描述.
     */
    private String statusString;

    public static ResponseStatus ofXml(String xml) {
        ResponseStatus instance = new ResponseStatus();
        Map<String, String> map = InnerUtils.xmlToFlatMap(xml, "ResponseStatus");
        instance.setRequestURL(map.get("requestURL"));
        instance.setStatusCode(Integer.valueOf(map.get("statusCode")));
        instance.setSubStatusCode(map.get("subStatusCode"));
        instance.setStatusString(map.get("statusString"));
        return instance;
    }
}
