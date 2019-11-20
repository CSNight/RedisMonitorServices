package com.csnight.redis.monitor.rest;

import com.csnight.redis.monitor.aop.LogBack;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;

@RequestMapping("files")
@RestController
public class FileController {

    @LogBack
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public String upload(MultipartFile file) throws IOException {
        // 获取文件名
        String fileName = file.getOriginalFilename();
        File f = new File(fileName);
        // 在file文件夹中创建名为fileName的文件
        OutputStreamWriter op = new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8);
        // 获取文件输入流
        InputStreamReader inputStreamReader = new InputStreamReader(file.getInputStream());
        char[] bytes = new char[12];
        while (inputStreamReader.read(bytes) != -1) {
            op.write(bytes);
        }
        // 关闭输出流
        op.close();
        // 关闭输入流
        inputStreamReader.close();
        return "上传成功";
    }
}
