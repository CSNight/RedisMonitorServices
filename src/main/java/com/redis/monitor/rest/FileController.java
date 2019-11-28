package com.redis.monitor.rest;

import com.redis.monitor.utils.BaseUtils;
import com.redis.monitor.utils.GUID;
import com.redis.monitor.utils.RespTemplate;
import io.swagger.annotations.Api;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@RequestMapping("files")
@RestController
@Api(tags = "文件管理API")
public class FileController {

    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public RespTemplate upload(MultipartFile file) {
        // 获取文件名
        try {
            String Dir = BaseUtils.getResourceDir() + "tmpFile/";
            File dir = new File(Dir);
            if (!dir.exists()) {
                dir.mkdir();
            }
            String fn = file.getOriginalFilename();
            assert fn != null;
            String ext = fn.substring(fn.lastIndexOf(".") + 1);
            File f = new File(Dir + GUID.getUUID() + "." + ext);
            file.transferTo(f);
            return new RespTemplate(HttpStatus.OK, f.getName());
        } catch (Exception ex) {
            return new RespTemplate(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }
}
