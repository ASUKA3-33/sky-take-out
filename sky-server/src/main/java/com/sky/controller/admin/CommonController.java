package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * 通用接口
 */
@RestController
@RequestMapping("/admin/common")
@Api(tags = "通用接口")
@Slf4j
public class CommonController {

    @Autowired
    private AliOssUtil aliOssUtil;
    /*
     * 上传文件
     * @param file 文件
     * @return 上传结果
     */
    @ApiOperation(value = "上传文件")
    @PostMapping("/upload")
    public Result<String> upload(MultipartFile file) {
        log.info("上传文件: {}", file);

        try {
            //获取原始文件名
            String fileName = file.getOriginalFilename();
            //获取文件扩展名
            String extension = fileName.substring(fileName.lastIndexOf(".") );
            //构建文件名称
            String objectName = UUID.randomUUID().toString() + extension;
            //文件的请求路径
            String filePath = aliOssUtil.upload(file.getBytes(), objectName);

            //文件上传
            aliOssUtil.upload(file.getBytes(), file.getOriginalFilename());

            return Result.success(filePath);
        } catch (IOException e) {
           log.error("上传文件失败", e);
        }
        return null;
    }

}
