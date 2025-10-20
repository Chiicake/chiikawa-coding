package com.chiikawa.chiikawacoding.service;

import com.mybatisflex.core.service.IService;
import com.chiikawa.chiikawacoding.model.entity.ChatHistory;

/**
 * 对话历史 服务层。
 *
 * @author <a href="https://github.com/Chiicake">chiicake</a>
 */
public interface ChatHistoryService extends IService<ChatHistory> {

    boolean addChatMessage(Long appId, String message, String messageType, Long userId);

    boolean deleteByAppId(Long appId);
}
