package com.cqnews.chat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ChatGPTDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChatGPTDemoApplication.class, args);
	}


	//TODO 超时关闭无用连接

}
