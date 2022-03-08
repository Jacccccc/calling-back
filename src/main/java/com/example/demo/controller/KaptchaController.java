package com.example.demo.controller;

import com.example.demo.util.CommonUtil;
import com.example.demo.util.JsonData;
import com.google.code.kaptcha.Producer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;
import sun.misc.BASE64Encoder;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.ses.v20201002.SesClient;
import com.tencentcloudapi.ses.v20201002.models.*;

@RestController
@RequestMapping("/utils")
public class KaptchaController {

    private static final long CAPTCHA_CODE_EXPIRED = 60 * 1000 * 10;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private Producer kaptchaProducer;

    /**
     * 获取图形验证码
     *
     * @param request
     * @param response
     */
    @GetMapping("kaptcha")
    public void getCaptcha(HttpServletRequest request, HttpServletResponse response) {
        String captchaText = kaptchaProducer.createText();
        //存储
        redisTemplate.opsForValue().set(getCaptchaKey(request), captchaText, CAPTCHA_CODE_EXPIRED, TimeUnit.MILLISECONDS);
        System.out.println(captchaText);
        BufferedImage bufferedImage = kaptchaProducer.createImage(captchaText);
        ServletOutputStream outputStream = null;
        try {
            outputStream = response.getOutputStream();
            ImageIO.write(bufferedImage, "jpg", outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
        }
    }

    /**
     * 获取缓存的key
     *
     * @param request
     * @return
     */
    private String getCaptchaKey(HttpServletRequest request) {
        String ip = CommonUtil.getIpAddr(request);
        String key = "user-service:kaptcha:" + CommonUtil.MD5(ip);
        return key;
    }

    /**
     * to⼿机号、邮箱,发送验证码
     * 限制IP每天发送次数三次
     *
     * @return
     */
    @GetMapping("sendEmail")
    @ResponseBody
    public JsonData sendEmail(@RequestParam(value = "email", required = true) String email, HttpServletRequest request) {
        if(email==null) return JsonData.buildError("stop do that");
        String ip = CommonUtil.getIpAddr(request);
        if (verifySendTimes(ip)){       //判断ip地址发送的次数是否超过三次
            String code=sendCode(email);
            redisTemplate.opsForValue().set(ip+"code",code,30,TimeUnit.MINUTES);
        }
        else return JsonData.buildError("当日发送次数已达三次，请明日再来！");
        return JsonData.buildSuccess();
    }

    /**
     * @param ip 标识用户
     *           限制IP每天发送次数三次
     * @return true:没有达三次可以发送，false:已达三次不可发送
     */
    public boolean verifySendTimes(String ip) {
        String countKey = "regSentTimes:" + CommonUtil.MD5(ip);
        String count = redisTemplate.opsForValue().get(countKey);
        if (count == null) redisTemplate.opsForValue().set(countKey, "1");
        else if (count.equals("3")) return false;
        else redisTemplate.opsForValue().increment(countKey);
        return true;
    }

    @RequestMapping("validate")
    public JsonData validateEmail(@RequestParam(value = "code", required = true) String code, HttpServletRequest request) {
        String co = redisTemplate.opsForValue().get(CommonUtil.getIpAddr(request) + "code");

        if (co == null || code == null) return JsonData.buildError("验证码错误");
        if (co.equals(code)) return JsonData.buildSuccess();
        else return JsonData.buildError("验证码错误");
    }
    //调用腾讯云接口发送邮件
    public String sendCode(String destination) {
        String code=generateCode();
        try {
            // 实例化一个认证对象，入参需要传入腾讯云账户secretId，secretKey,此处还需注意密钥对的保密
            // 密钥可前往https://console.cloud.tencent.com/cam/capi网站进行获取
            Credential cred = new Credential("", "");
            // 实例化一个http选项，可选的，没有特殊需求可以跳过
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint("ses.tencentcloudapi.com");
            // 实例化一个client选项，可选的，没有特殊需求可以跳过
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);
            // 实例化要请求产品的client对象,clientProfile是可选的
            SesClient client = new SesClient(cred, "ap-hongkong", clientProfile);
            // 实例化一个请求对象,每个接口都会对应一个request对象
            SendEmailRequest req = new SendEmailRequest();
            req.setFromEmailAddress("");
            String[] destination1 = {destination};
            req.setDestination(destination1);
            Template template1 = new Template();
            template1.setTemplateID(23770L);
            template1.setTemplateData("{\"code\":\""+code+"\"}");
            req.setTemplate(template1);
            req.setSubject("注册验证码");
            // 返回的resp是一个SendEmailResponse的实例，与请求对象对应
            SendEmailResponse resp = client.SendEmail(req);
            // 输出json格式的字符串回包
            System.out.println(SendEmailResponse.toJsonString(resp));
        } catch (TencentCloudSDKException e) {
            System.out.println(e.toString());
        }
        return code ;
    }
    public String generateCode(){
            String str="abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
            Random random=new Random();
            StringBuffer sb=new StringBuffer();
            for(int i=0;i<4;i++){
                int number=random.nextInt(62);
                sb.append(str.charAt(number));
            }
            return sb.toString();
        }
    @RequestMapping("logincode")
    public JsonData validateLoginCode(HttpServletRequest request) {
        String key=getCaptchaKey(request);
        String co=redisTemplate.opsForValue().get(key);
       return JsonData.buildSuccess(co);
    }
}
