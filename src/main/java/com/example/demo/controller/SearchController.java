package com.example.demo.controller;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.annotation.RequireRole;
import com.example.demo.service.SearchService;
import com.example.demo.util.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @description:
 * @author: Jac
 * @time: 2022/2/9 17:35
 **/

@RestController
public class SearchController {

    @Autowired
    SearchService searchService;
    @RequestMapping("searchPost")
    public JsonData searchByTag(@RequestBody JSONObject object){
        List<String> tags=object.getObject("tags",List.class);
        return searchService.searchPostByTag(tags);
    }
}
