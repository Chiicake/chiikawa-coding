package com.chiikawa.chiikawacoding.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.chiikawa.chiikawacoding.model.entity.App;
import com.chiikawa.chiikawacoding.mapper.AppMapper;
import com.chiikawa.chiikawacoding.service.AppService;
import org.springframework.stereotype.Service;

/**
 * 应用 服务层实现。
 *
 * @author <a href="https://github.com/Chiicake">chiicake</a>
 */
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App>  implements AppService{

}
