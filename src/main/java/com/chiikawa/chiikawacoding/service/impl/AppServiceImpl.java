package com.chiikawa.chiikawacoding.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.chiikawa.chiikawacoding.constant.AppConstant;
import com.chiikawa.chiikawacoding.constant.UserConstant;
import com.chiikawa.chiikawacoding.core.AiCodeGeneratorFacade;
import com.chiikawa.chiikawacoding.exception.BusinessException;
import com.chiikawa.chiikawacoding.exception.ErrorCode;
import com.chiikawa.chiikawacoding.exception.ThrowUtils;
import com.chiikawa.chiikawacoding.mapper.AppMapper;
import com.chiikawa.chiikawacoding.model.dto.app.AppQueryRequest;
import com.chiikawa.chiikawacoding.model.entity.App;
import com.chiikawa.chiikawacoding.model.entity.User;
import com.chiikawa.chiikawacoding.model.enums.ChatHistoryMessageTypeEnum;
import com.chiikawa.chiikawacoding.model.enums.CodeGenTypeEnum;
import com.chiikawa.chiikawacoding.model.vo.AppVO;
import com.chiikawa.chiikawacoding.service.AppService;
import com.chiikawa.chiikawacoding.service.ChatHistoryService;
import com.chiikawa.chiikawacoding.service.UserService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 应用 服务层实现。
 *
 * @author chiikawa
 */
@Service
@Slf4j
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

    @Resource
    private UserService userService;

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Resource
    private ChatHistoryService chatHistoryService;


    @Override
    public App getAppById(long id, HttpServletRequest request) {
        App app = getById(id);
        if (app == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        }
        // 验证权限：只有管理员或应用创建者可以查看
        User loginUser = userService.getLoginUser(request);
        if (!loginUser.getUserRole().equals(UserConstant.ADMIN_ROLE) && !app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限查看该应用");
        }
        return app;
    }

    @Override
    public App getAppByIdForAdmin(long id) {
        App app = getById(id);
        if (app == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        }
        return app;
    }

    @Override
    public boolean updateApp(App app, HttpServletRequest request) {
        // 验证应用是否存在
        App oldApp = getById(app.getId());
        if (oldApp == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        }
        // 验证权限：只有应用创建者可以更新
        User loginUser = userService.getLoginUser(request);
        if (!oldApp.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限更新该应用");
        }
        // 更新应用
        App updateApp = new App();
        updateApp.setId(app.getId());
        updateApp.setAppName(app.getAppName());
        return updateById(updateApp);
    }

    @Override
    public boolean updateAppForAdmin(App app) {
        // 验证应用是否存在
        if (!exists(QueryWrapper.create().eq("id", app.getId()))) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        }
        // 管理员可以更新的字段：应用名称、应用封面、优先级
        App updateApp = new App();
        updateApp.setId(app.getId());
        updateApp.setAppName(app.getAppName());
        updateApp.setCover(app.getCover());
        updateApp.setPriority(app.getPriority());
        return updateById(updateApp);
    }

    @Override
    public boolean deleteApp(long id, HttpServletRequest request) {
        // 验证应用是否存在
        App app = getById(id);
        if (app == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        }
        // 验证权限：只有应用创建者可以删除
        User loginUser = userService.getLoginUser(request);
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限删除该应用");
        }
        return removeById(id);
    }

    @Override
    public boolean deleteAppForAdmin(long id) {
        // 验证应用是否存在
        if (!exists(QueryWrapper.create().eq("id", id))) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        }
        return removeById(id);
    }

    @Override
    public QueryWrapper getUserAppQueryWrapper(AppQueryRequest appQueryRequest, HttpServletRequest request) {
        QueryWrapper queryWrapper = new QueryWrapper();
        User loginUser = userService.getLoginUser(request);
        // 用户只能查询自己的应用
        queryWrapper.eq("userId", loginUser.getId());
        // 构建查询条件
        buildQueryWrapper(queryWrapper, appQueryRequest);
        return queryWrapper;
    }

    @Override
    public QueryWrapper getAdminAppQueryWrapper(AppQueryRequest appQueryRequest) {
        QueryWrapper queryWrapper = new QueryWrapper();
        // 管理员可以查询所有应用
        buildQueryWrapper(queryWrapper, appQueryRequest);
        // 管理员可以根据除时间外的任何字段查询
        if (appQueryRequest.getUserId() != null) {
            queryWrapper.eq("userId", appQueryRequest.getUserId());
        }
        return queryWrapper;
    }

    @Override
    public QueryWrapper getFeaturedAppQueryWrapper(AppQueryRequest appQueryRequest) {
        QueryWrapper queryWrapper = new QueryWrapper();
        // 精选应用：优先级大于0的应用
        queryWrapper.gt("priority", 0);
        // 构建查询条件
        buildQueryWrapper(queryWrapper, appQueryRequest);
        // 按优先级降序排序
        queryWrapper.orderBy("priority", false);
        return queryWrapper;
    }

    @Override
    public AppVO getAppVO(App app) {
        if (app == null) {
            return null;
        }
        AppVO appVO = new AppVO();
        BeanUtil.copyProperties(app, appVO);
        return appVO;
    }

    @Override
    public List<AppVO> getAppVOList(List<App> appList) {
        if (CollUtil.isEmpty(appList)) {
            return CollUtil.newArrayList();
        }
        return appList.stream().map(this::getAppVO).collect(Collectors.toList());
    }

    /**
     * 构建通用查询条件
     *
     * @param queryWrapper 查询包装器
     * @param appQueryRequest 查询条件
     */
    private void buildQueryWrapper(QueryWrapper queryWrapper, AppQueryRequest appQueryRequest) {
        if (appQueryRequest == null) {
            return;
        }
        // 应用id
        if (appQueryRequest.getId() != null) {
            queryWrapper.eq("id", appQueryRequest.getId());
        }
        // 应用名称（模糊查询）
        if (appQueryRequest.getAppName() != null && !appQueryRequest.getAppName().trim().isEmpty()) {
            queryWrapper.like("appName", appQueryRequest.getAppName());
        }
        // 代码生成类型
        if (appQueryRequest.getCodeGenType() != null && !appQueryRequest.getCodeGenType().trim().isEmpty()) {
            queryWrapper.eq("codeGenType", appQueryRequest.getCodeGenType());
        }
        // 排序字段
        if (appQueryRequest.getSortField() != null && !appQueryRequest.getSortField().trim().isEmpty()) {
            String sortOrder = appQueryRequest.getSortOrder();
            if ("ascend".equals(sortOrder)) {
                queryWrapper.orderBy(appQueryRequest.getSortField(), true);
            } else {
                queryWrapper.orderBy(appQueryRequest.getSortField(), false);
            }
        } else {
            // 默认按创建时间降序排序
            queryWrapper.orderBy("createTime", false);
        }
    }

    @Override
    public Flux<String> chatToGenCode(Long appId, String message, User loginUser) {
        // 1. 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "用户消息不能为空");
        // 2. 查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        // 3. 验证用户是否有权限访问该应用，仅本人可以生成代码
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限访问该应用");
        }
        // 4. 获取应用的代码生成类型
        String codeGenTypeStr = app.getCodeGenType();
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenTypeStr);
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型");
        }
        // 5. 通过校验后，添加用户消息到对话历史
        chatHistoryService.addChatMessage(appId, message, ChatHistoryMessageTypeEnum.USER.getValue(), loginUser.getId());
        // 6. 调用 AI 生成代码（流式）
        Flux<String> contentFlux = aiCodeGeneratorFacade.generateAndSaveCodeStream(message, codeGenTypeEnum, appId);
        // 7. 收集AI响应内容并在完成后记录到对话历史
        StringBuilder aiResponseBuilder = new StringBuilder();
        return contentFlux
                .map(chunk -> {
                    // 收集AI响应内容
                    aiResponseBuilder.append(chunk);
                    return chunk;
                })
                .doOnComplete(() -> {
                    // 流式响应完成后，添加AI消息到对话历史
                    String aiResponse = aiResponseBuilder.toString();
                    if (StrUtil.isNotBlank(aiResponse)) {
                        chatHistoryService.addChatMessage(appId, aiResponse, ChatHistoryMessageTypeEnum.AI.getValue(), loginUser.getId());
                    }
                })
                .doOnError(error -> {
                    // 如果AI回复失败，也要记录错误消息
                    String errorMessage = "AI回复失败: " + error.getMessage();
                    chatHistoryService.addChatMessage(appId, errorMessage, ChatHistoryMessageTypeEnum.AI.getValue(), loginUser.getId());
                });
    }

    @Override
    public String deployApp(Long appId, User loginUser) {
        // 1. 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        // 2. 查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        // 3. 验证用户是否有权限部署该应用，仅本人可以部署
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限部署该应用");
        }
        // 4. 检查是否已有 deployKey
        String deployKey = app.getDeployKey();
        // 没有则生成 6 位 deployKey（大小写字母 + 数字）
        if (StrUtil.isBlank(deployKey)) {
            deployKey = RandomUtil.randomString(6);
        }
        // 5. 获取代码生成类型，构建源目录路径
        String codeGenType = app.getCodeGenType();
        String sourceDirName = codeGenType + "_" + appId;
        String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;
        // 6. 检查源目录是否存在
        File sourceDir = new File(sourceDirPath);
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用代码不存在，请先生成代码");
        }
        // 7. 复制文件到部署目录
        String deployDirPath = AppConstant.CODE_DEPLOY_ROOT_DIR + File.separator + deployKey;
        try {
            FileUtil.copyContent(sourceDir, new File(deployDirPath), true);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "部署失败：" + e.getMessage());
        }
        // 8. 更新应用的 deployKey 和部署时间
        App updateApp = new App();
        updateApp.setId(appId);
        updateApp.setDeployKey(deployKey);
        updateApp.setDeployedTime(LocalDateTime.now());
        boolean updateResult = this.updateById(updateApp);
        ThrowUtils.throwIf(!updateResult, ErrorCode.OPERATION_ERROR, "更新应用部署信息失败");
        // 9. 返回可访问的 URL
        return String.format("%s/%s/", AppConstant.CODE_DEPLOY_HOST, deployKey);
    }


    /**
     * 删除应用时关联删除对话历史
     *
     * @param id 应用ID
     * @return 是否成功
     */
    @Override
    public boolean removeById(Serializable id) {
        if (id == null) {
            return false;
        }
        // 转换为 Long 类型
        Long appId = Long.valueOf(id.toString());
        if (appId <= 0) {
            return false;
        }
        // 先删除关联的对话历史
        try {
            chatHistoryService.deleteByAppId(appId);
        } catch (Exception e) {
            // 记录日志但不阻止应用删除
            log.error("删除应用关联对话历史失败: {}", e.getMessage());
        }
        // 删除应用
        return super.removeById(id);
    }

}
