package com.cqnews.chat.controller;


import com.cqnews.chat.service.OpanAIFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class OpenAIController {

    @Autowired
    private OpanAIFeign opanAIFeign;


    @PostMapping("/test")
    public String test(){
        System.out.println("\nCreating completion...");
//        CompletionRequest completionRequest = CompletionRequest.builder()
//                .model("text-davinci-003")//text-davinci-003,text-curie-001,text-babbage-001,text-ada-001
//                .temperature(0.9)
//                .prompt("java概念")
//                .user("hlw").stream(true)
//                .maxTokens(2048)
//                .topP(0.9)
//                .build();
        Map<String, Object> parm = new HashMap<>();
        parm.put("model", "text-davinci-003");
        parm.put("temperature", 0.9);
        parm.put("prompt", "java概念");
        parm.put("max_tokens", 2048);
        parm.put("stream", true);
        parm.put("user", "hlw");
        parm.put("echo", true);
        return opanAIFeign.stream(parm);
    }

}
