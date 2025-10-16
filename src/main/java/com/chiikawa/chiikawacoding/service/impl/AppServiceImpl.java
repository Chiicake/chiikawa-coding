package com.chiikawa.chiikawacoding.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.chiikawa.chiikawacoding.constant.UserConstant;
import com.chiikawa.chiikawacoding.exception.BusinessException;
import com.chiikawa.chiikawacoding.exception.ErrorCode;
import com.chiikawa.chiikawacoding.mapper.AppMapper;
import com.chiikawa.chiikawacoding.model.dto.app.AppQueryRequest;
import com.chiikawa.chiikawacoding.model.entity.App;
import com.chiikawa.chiikawacoding.model.entity.User;
import com.chiikawa.chiikawacoding.model.vo.AppVO;
import com.chiikawa.chiikawacoding.service.AppService;
import com.chiikawa.chiikawacoding.service.UserService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 应用 服务层实现。
 *
 * @author chiikawa
 */
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

    @Resource
    private UserService userService;

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
}
