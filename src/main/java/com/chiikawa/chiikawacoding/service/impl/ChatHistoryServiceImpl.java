package com.chiikawa.chiikawacoding.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.chiikawa.chiikawacoding.model.entity.ChatHistory;
import com.chiikawa.chiikawacoding.mapper.ChatHistoryMapper;
import com.chiikawa.chiikawacoding.service.ChatHistoryService;
import org.springframework.stereotype.Service;

/**
 * 对话历史 服务层实现。
 *
 * @author <a href="https://github.com/Chiicake">chiicake</a>
 */
@Service
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory>  implements ChatHistoryService{

}
