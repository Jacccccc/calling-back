package com.example.demo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;

@SpringBootApplication
@MapperScan("com.example.demo.dao")
@EnableAspectJAutoProxy
@EnableCaching
@ServletComponentScan
public class GetApplication {

	public static void main(String[] args) {
		SpringApplication.run(GetApplication.class, args);
	}

}
