package com.chiikawa.chiikawacoding.controller;


import com.chiikawa.chiikawacoding.common.baseReqAndRes.BaseResponse;
import com.chiikawa.chiikawacoding.common.utils.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping("/")
    public BaseResponse<String> healthCheck() {
        return ResultUtils.success("ok");
    }
}
