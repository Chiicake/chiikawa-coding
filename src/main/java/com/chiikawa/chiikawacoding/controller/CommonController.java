package com.chiikawa.chiikawacoding.controller;


import com.chiikawa.chiikawacoding.common.utils.AliOssUtil;
import com.chiikawa.chiikawacoding.common.baseReqAndRes.BaseResponse;
import com.chiikawa.chiikawacoding.common.utils.ResultUtils;
import com.chiikawa.chiikawacoding.exception.ErrorCode;
import com.chiikawa.chiikawacoding.model.vo.FileUploadVO;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("/common")
public class CommonController {

    @Autowired
    private AliOssUtil aliOssUtil;

    @PostMapping("/upload")
    public BaseResponse<FileUploadVO> upload(MultipartFile file){
        log.info("文件上传{}", file);

        try {
            String originalFilename = file.getOriginalFilename();
            String extention = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = originalFilename.substring(0, originalFilename.lastIndexOf("."));
            String objectName = fileName + UUID.randomUUID().toString() + extention;

            String filePath = aliOssUtil.upload(file.getBytes(), objectName);
            FileUploadVO fileUploadVO = new FileUploadVO();
            fileUploadVO.setUrl(filePath);

            return ResultUtils.success(fileUploadVO);
        } catch (IOException e) {
            log.error("文件上传失败,{}", e.toString());
        }

        return (BaseResponse<FileUploadVO>) ResultUtils.error(ErrorCode.FILE_UPLOAD_ERROR, "文件上传失败");
    }

}
