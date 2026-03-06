package com.engine.interfaces.yll2.wuling.archive.web;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.customization.yll.common.util.ParamUtil;
import com.customization.yll.common.web.WebExceptionHandler;
import com.customization.yll.common.web.exception.WebParamException;
import com.engine.common.util.ServiceUtil;
import com.engine.interfaces.yll2.wuling.archive.model.dto.HistoryArchivePushResult;
import com.engine.interfaces.yll2.wuling.archive.model.vo.ArchiveHistoryPushStatus;
import com.engine.interfaces.yll2.wuling.archive.service.ArchiveService;
import com.engine.interfaces.yll2.wuling.archive.service.impl.ArchiveServiceImpl;
import org.apache.commons.lang.StringUtils;
import weaver.integration.logging.Logger;
import weaver.integration.logging.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 档案系统对接相关接口
 * @author yaolilin
 */
public class ArchiveAction {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private ArchiveService service;

    @Path("/status")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response receiveStatus(@Context HttpServletRequest request) {
        try {
            JSONObject params = new JSONObject(ParamUtil.requestJson2Map(request));
            log.info("接收报文："+ params.toJSONString());
            getService().recordStatus(params);
        } catch (Exception e) {
            return WebExceptionHandler.handle(e);
        }
        return Response.ok().build();
    }

    @Path("/feedback")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response fallback(@Context HttpServletRequest request) {
        return receiveStatus(request);
    }

    @Path("/push")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response push(@FormParam("requestId") String requestId, @FormParam("configId") String configId
            ,@FormParam("isHistoryWorkflow") String isHistoryWorkflow) {
        try {
            if (StrUtil.isEmpty(requestId) || StrUtil.isEmpty(configId)) {
                throw new WebParamException.BodyParamException("configId 和 requestId 参数必传");
            }
            getService().push(Integer.parseInt(configId),Integer.parseInt(requestId),
                    "1".equals(isHistoryWorkflow));
        } catch (Exception e) {
            return WebExceptionHandler.handle(e);
        }
        return Response.ok().build();
    }

    @Path("/push-history")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response pushHistory(@FormParam("workflowIds") String workflowIds) {
        try {
            if (StringUtils.isBlank(workflowIds)) {
                throw new WebParamException.BodyParamException("[workflowIds] 参数必传");
            }
            List<Integer> ids = Arrays.stream(workflowIds.split(","))
                    .map(Integer::parseInt).collect(Collectors.toList());
            HistoryArchivePushResult result = getService().pushHistoryWorkflow(ids);
            return Response.ok().entity(result).build();
        } catch (Exception e) {
            return WebExceptionHandler.handle(e);
        }
    }

    @Path("/push-history-status")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response pushHistoryStatus() {
        try {
            ArchiveHistoryPushStatus status = getService().getHistoryPushStatus();
            return Response.ok().entity(status).build();
        } catch (Exception e) {
            return WebExceptionHandler.handle(e);
        }
    }

    @Path("/stop-history-push")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response stopHistoryPush() {
        try {
            getService().stopHistoryPush();
            return Response.ok().build();
        } catch (Exception e) {
            return WebExceptionHandler.handle(e);
        }
    }

    private ArchiveService getService() {
        if (this.service == null) {
            this.service = ServiceUtil.getService(ArchiveServiceImpl.class);
        }
        return service;
    }
}
