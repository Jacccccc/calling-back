package com.example.demo.controller;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.annotation.RequireRole;
import com.example.demo.util.CommonUtil;
import com.example.demo.util.JsonData;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @description:
 * @author: Jac
 * @time: 2022/2/18 0:08
 **/
@RestController
public class AvatarController {

    @RequestMapping("uploadAvatar")
    @RequireRole("normal")
    public JsonData uploadAvatar( @RequestParam(value = "file") MultipartFile multipartFile){
            try {
                Long id=0L;
                SaveFileFromInputStream(multipartFile.getInputStream(),"D:", CommonUtil.MD5(String.valueOf(id))+".jpg");
            } catch (IOException e) {
                e.printStackTrace();
            }
        return JsonData.buildSuccess();
    }
    public void SaveFileFromInputStream(InputStream stream, String path, String filename) throws IOException
    {
        FileOutputStream fs=new FileOutputStream( path + "/"+ filename);
        byte[] buffer =new byte[1024*1024];
        int bytesum = 0;
        int byteread = 0;
        while ((byteread=stream.read(buffer))!=-1)
        {
            bytesum+=byteread;
            fs.write(buffer,0,byteread);
            fs.flush();
        }
        fs.close();
        stream.close();
    }

}
