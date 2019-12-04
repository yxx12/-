package com.changgou.controller;

import com.changgou.util.FileDFSClient;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@CrossOrigin  // 跨域访问的问题：不同域，协议、地址、端口
public class FielController {
    @PostMapping("/upload")
    public String upload(MultipartFile file) throws IOException {
        byte[] bytes = file.getBytes();
        String filename = file.getOriginalFilename();
        String file_ext_name = StringUtils.getFilenameExtension(filename);

        String[] upload = FileDFSClient.upload(bytes, file_ext_name, null);
        String url = FileDFSClient.getUrl();
        url = url + "/" + upload[0] + "/" + upload[1];
        return url;
    }

  /*  @PostMapping("/download")
    public Result download( String group_name,
                           String remote_filename) {
        try {
            byte[] download = FileDFSClient.download(group_name, remote_filename);
            FileOutputStream fileOutputStream = new FileOutputStream("E:\\ 1.jpg");
            IOUtils.write(download, fileOutputStream);
            return new Result(true, StatusCode.OK, "下载成功");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Result(false, StatusCode.ERROR, "下载失败");
    }*/
}
