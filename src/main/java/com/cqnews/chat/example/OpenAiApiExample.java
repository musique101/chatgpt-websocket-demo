package com.cqnews.chat.example;

import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.image.CreateImageRequest;
import com.theokanning.openai.service.OpenAiService;

import java.time.Duration;

class OpenAiApiExample {

    public static void main(String... args) {
        OpenAiService service = new OpenAiService("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", Duration.ofSeconds(60L));

        System.out.println("\nCreating completion...");
        CompletionRequest completionRequest = CompletionRequest.builder()
                .model("text-davinci-003")//text-davinci-003,text-curie-001,text-babbage-001,text-ada-001
                .temperature(0.9)
                .prompt("java概念")
                .user("hlw").stream(true)
                .maxTokens(2048)
                .topP(0.9)
                .build();

        CreateImageRequest createImageRequest = CreateImageRequest.builder()
                .prompt("猫咪").user("hlw").size("512x512").build();
        try {
            service.createCompletion(completionRequest);
//            System.out.println(service.createImage(createImageRequest));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("发呆中");
        }
    }
}
