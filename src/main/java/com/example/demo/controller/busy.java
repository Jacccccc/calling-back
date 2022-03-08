package com.example.demo.controller;

import com.example.demo.util.JsonData;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description:
 * @author: Jac
 * @time: 2022/3/5 14:45
 **/

@RestController
public class busy {
    @RequestMapping("/busy")
    public JsonData isBusy(){
        return JsonData.buildError("服务器繁忙");
    }
}
