package com.cqnews.chat.service;


import com.cqnews.chat.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(url = "https://api.openai.com/v1", configuration = FeignConfig.class, name = "openai")
public interface OpanAIFeign {


    @PostMapping("/completions")
    String stream(@RequestBody Map<String, Object> parm);
}
