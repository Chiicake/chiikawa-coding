package com.chiikawa.chiikawacoding.service;

import com.chiikawa.chiikawacoding.model.dto.app.AppQueryRequest;
import com.chiikawa.chiikawacoding.model.vo.AppVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.chiikawa.chiikawacoding.model.entity.App;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * 应用 服务层。
 *
 * @author chiikawa
 */
public interface AppService extends IService<App> {

    /**
     * 根据id获取应用（用户视角）
     *
     * @param id 应用id
     * @param request 请求信息
     * @return 应用详情
     */
    App getAppById(long id, HttpServletRequest request);

    /**
     * 根据id获取应用（管理员视角）
     *
     * @param id 应用id
     * @return 应用详情
     */
    App getAppByIdForAdmin(long id);

    /**
     * 更新应用（用户）
     *
     * @param app 更新后的应用信息
     * @param request 请求信息
     * @return 是否更新成功
     */
    boolean updateApp(App app, HttpServletRequest request);

    /**
     * 更新应用（管理员）
     *
     * @param app 更新后的应用信息
     * @return 是否更新成功
     */
    boolean updateAppForAdmin(App app);

    /**
     * 删除应用（用户）
     *
     * @param id 应用id
     * @param request 请求信息
     * @return 是否删除成功
     */
    boolean deleteApp(long id, HttpServletRequest request);

    /**
     * 删除应用（管理员）
     *
     * @param id 应用id
     * @return 是否删除成功
     */
    boolean deleteAppForAdmin(long id);

    /**
     * 获取用户的应用列表查询包装器
     *
     * @param appQueryRequest 查询条件
     * @param request 请求信息
     * @return 查询条件对应的查询包装器
     */
    QueryWrapper getUserAppQueryWrapper(AppQueryRequest appQueryRequest, HttpServletRequest request);

    /**
     * 获取管理员的应用查询包装器
     *
     * @param appQueryRequest 查询条件
     * @return 查询条件对应的查询包装器
     */
    QueryWrapper getAdminAppQueryWrapper(AppQueryRequest appQueryRequest);

    /**
     * 获取精选应用查询包装器
     *
     * @param appQueryRequest 查询条件
     * @return 查询条件对应的查询包装器
     */
    QueryWrapper getFeaturedAppQueryWrapper(AppQueryRequest appQueryRequest);

    /**
     * 获取应用VO
     *
     * @param app 应用实体
     * @return 应用VO
     */
    AppVO getAppVO(App app);

    /**
     * 获取应用VO列表
     *
     * @param appList 应用实体列表
     * @return 应用VO列表
     */
    List<AppVO> getAppVOList(List<App> appList);
}
