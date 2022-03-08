package com.example.demo.service;

import com.example.demo.util.JsonData;
import org.springframework.stereotype.Service;

import java.util.List;


public interface SearchService {
    JsonData searchPostByTag(List<String> tags);
}
