package com.cqnews.chat.service;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.cqnews.chat.common.ChatGPTSession;
import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.image.CreateImageRequest;
import com.theokanning.openai.service.OpenAiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;


/**
 * @author websocket服务
 */
@ServerEndpoint(value = "/imserver/{username}")
@Component
public class WebSocketServer {

    private static final Logger log = LoggerFactory.getLogger(WebSocketServer.class);

    private static final String apiKey = "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";

    /**
     * 记录当前在线连接数
     */
    public static final Map<String, Session> sessionMap = new ConcurrentSkipListMap<String, Session>(){
        {put("ChatGPT", new ChatGPTSession());}
    };


    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("username") String username) {
        if ("ChatGPT".equals(username)){
            try {
                CloseReason closeReason = new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE,"ChatGPT为系统机器人，请重新命名！");
                session.close(closeReason);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        //判断不能重名
        if(repeatUsername(username)){
            try {
                CloseReason closeReason = new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE,"已存在相同昵称，请重新命名！");
                session.close(closeReason);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        sessionMap.put(username, session);
        log.info("有新用户加入，username={}, 当前在线人数为：{}", username, sessionMap.size());
        sendOnlineList();
    }


    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(Session session) {
        String username = "";
        for (Map.Entry<String, Session> next : sessionMap.entrySet()) {
            String key = next.getKey();
            Session value = next.getValue();
            if (session.getId().equals(value.getId())) {
                username = key;
                sessionMap.remove(key);
            }
        }
        log.info("有一连接关闭，移除username={}的用户session, 当前在线人数为：{}", username, sessionMap.size());
        sendOnlineList();
    }

    /**
     * 收到客户端消息后调用的方法
     * 后台收到客户端发送过来的消息
     * onMessage 是一个消息的中转站
     * 接受 浏览器端 socket.send 发送过来的 json数据
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message, Session session, @PathParam("username") String username) {
        log.info("服务端收到用户username={}的消息:{}", username, message);
        JSONObject obj = JSONUtil.parseObj(message);
        String toUsername = obj.getStr("to"); // to表示发送给哪个用户
        String text = obj.getStr("text"); // 发送的消息文本
        Session toSession = sessionMap.get(toUsername); // 根据 to用户名来获取 session，再通过session发送消息文本
        if (toSession != null && !"ChatGPT".equals(toUsername)) {
            // 服务器端 再把消息组装一下，组装后的消息包含发送人和发送的文本内容
            JSONObject jsonObject = new JSONObject();
            jsonObject.set("code", 0);
            jsonObject.set("from", username);
            jsonObject.set("text", text);
            this.sendMessage(jsonObject.toString(), toSession);
            log.info("发送给用户username={}，消息：{}", toUsername, jsonObject.toString());
        } else if("ChatGPT".equals(toUsername)){
            //获取返回消息
            String resultMess = resultMess(apiKey, text);
            this.sendMessage(resultMess, session);
            log.info("发送给用户username={}，消息：{}", toUsername, resultMess);
        } else {
            log.info("发送失败，未找到用户username={}的session", toUsername);
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("发生错误");
        error.printStackTrace();
    }

    /**
     * 服务端发送消息给客户端
     */
    private void sendMessage(String message, Session toSession) {
        try {
            log.info("服务端给客户端[{}]发送消息{}", toSession.getId(), message);
            toSession.getBasicRemote().sendText(message);
        } catch (Exception e) {
            log.error("服务端发送消息给客户端失败", e);
        }
    }

    /**
     * 服务端发送消息给所有客户端
     */
    private void sendAllMessage(String message) {
        try {
            for (Session session : sessionMap.values()) {
                if(session.getContainer() == null){
                    continue;
                }
                log.info("服务端给客户端[{}]发送消息{}", session.getId(), message);
                session.getBasicRemote().sendText(message);
            }
        } catch (Exception e) {
            log.error("服务端发送消息给客户端失败", e);
        }
    }

    private boolean repeatUsername(String username){
        for (Object key : sessionMap.keySet()) {
            if(key.equals(username)) {
                return true;
            }
        }
        return false;
    }


    private String resultMess(String apiKey, String text){
        //新建chatgpt连接
        OpenAiService service = new OpenAiService(apiKey, Duration.ofSeconds(60L));
        String result = "";
        if(text.startsWith("image#")){
            String substring = text.substring(5);
            //图像生成模式
            CreateImageRequest createImageRequest = CreateImageRequest.builder()
                    .prompt(substring).user("hlw").size("512x512").build();
            try {
                result = service.createImage(createImageRequest).getData().get(0).getUrl();
            }catch (Exception e){
                log.error(e.getMessage());
                result = "ChatGPT发呆中....，请稍后再试";
            }
        }else {
            //默认聊天模式
            CompletionRequest completionRequest = CompletionRequest.builder()
                    .model("text-davinci-003").prompt(text).user("hlw").maxTokens(2048)
                    .temperature(0.9).build();
            try {
                result = service.createCompletion(completionRequest).getChoices().get(0).getText();
            }catch (Exception e){
                log.error(e.getMessage());
                result = "ChatGPT发呆中....，请稍后再试";
            }
        }
        //返回消息给用户
        JSONObject jsonObject = new JSONObject();
        jsonObject.set("code", 0);
        jsonObject.set("from", "ChatGPT");
        jsonObject.set("text", result);
        return jsonObject.toString();
    }


    private void sendOnlineList() {
        JSONObject result = new JSONObject();
        JSONArray array = new JSONArray();
        result.set("code", 0);
        result.set("users", array);
        for (Object key : sessionMap.keySet()) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.set("username", key);
            array.add(jsonObject);
        }
        sendAllMessage(JSONUtil.toJsonStr(result));
    }
}

