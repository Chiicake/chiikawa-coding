package com.chiikawa.chiikawacoding.model.dto.app;

import com.chiikawa.chiikawacoding.common.baseReqAndRes.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 查询应用请求
 *
 * @author chiikawa
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AppQueryRequest extends PageRequest implements Serializable {

    /**
     * 应用id
     */
    private Long id;

    /**
     * 应用名称（模糊查询）
     */
    private String appName;

    /**
     * 创建用户id
     */
    private Long userId;

    /**
     * 代码生成类型（枚举）
     */
    private String codeGenType;

    /**
     * 是否为精选应用（管理员查询用）
     */
    private Boolean isFeatured;

    private static final long serialVersionUID = 1L;
}