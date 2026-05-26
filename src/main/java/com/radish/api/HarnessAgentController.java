package com.radish.api;

import io.agentscope.core.agent.Event;
import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.Msg;
import io.agentscope.harness.agent.HarnessAgent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

/**
 * Harness Agent 对话接口控制器
 * 提供流式输出的对话 API
 *
 * @author luoyuheng
 * @date 2026-05-26
 */
@Slf4j
@RestController
@RequestMapping("/api/harness")
public class HarnessAgentController {

    private final HarnessAgent harnessAgent;

    public HarnessAgentController(HarnessAgent harnessAgent) {
        this.harnessAgent = harnessAgent;
    }

    /**
     * 对话接口 - 流式输出
     * POST /api/harness/chat
     *
     * @param request 对话请求
     * @return 流式响应
     */
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatResponse> chat(@RequestBody ChatRequest request) {
        log.info("收到对话请求，userId: {}, sessionId: {}, message: {}",
                request.getUserId(), request.getSessionId(), request.getMessage());

        // 构建运行时上下文
        RuntimeContext context = RuntimeContext.builder()
                .userId(request.getUserId())
                .sessionId(request.getSessionId())
                .build();

        // 构建用户消息
        Msg userMsg = Msg.builder()
                .textContent(request.getMessage())
                .build();

        // 调用 HarnessAgent 的流式接口
        return harnessAgent.stream(userMsg, context)
                .map(event -> convertEventToResponse(event, request));
    }

    /**
     * 将 Event 转换为 ChatResponse
     */
    private ChatResponse convertEventToResponse(Event event, ChatRequest request) {
        ChatResponse response = new ChatResponse();
        response.setUserId(request.getUserId());
        response.setSessionId(request.getSessionId());
        response.setEventType(event.getType().name());
        response.setLast(event.isLast());

        if (event.getMessage() != null) {
            response.setContent(event.getMessage().getTextContent());
            response.setMessageId(event.getMessage().getId());
        }

        return response;
    }

    /**
     * 对话请求 DTO
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ChatRequest {
        private String userId;
        private String sessionId;
        private String message;
    }

    /**
     * 对话响应 DTO
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ChatResponse {
        private String userId;
        private String sessionId;
        private String eventType;
        private String content;
        private String messageId;
        private boolean last;
    }
}
