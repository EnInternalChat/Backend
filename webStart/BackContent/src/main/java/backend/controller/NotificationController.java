package backend.controller;

import backend.mdoel.Notification;
import backend.service.DatabaseService;
import backend.util.ResponseJsonObj;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.activiti.engine.impl.util.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.List;

/**
 * Created by lenovo on 2017/5/2.
 */
@RestController
@CrossOrigin
@Api(description = "通知数据")
@RequestMapping(value = "/notifications")
public class NotificationController {
    @Autowired
    DatabaseService databaseService;

    @ApiOperation(value = "查看发出的通知", notes = "根据id查看我发出的通知")
    @ApiImplicitParam(name = "ID", value = "发送者id", required = true, dataType = "Long", paramType = "path")
    @ResponseBody
    @RequestMapping(value = "/sent/{ID}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Collection<Notification> sentNotifications(@PathVariable("ID") Long id) {
        return databaseService.getSent(id);
        //TODO fix?
    }

    @ApiOperation(value = "查看已读(收到)的通知", notes = "根据id查看我已读(收到)的通知")
    @ApiImplicitParam(name = "ID", value = "接收者id", required = true, dataType = "Long", paramType = "path")
    @ResponseBody
    @RequestMapping(value = "/received/read/{ID}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Collection<Notification> rcvdReadNotifications(@PathVariable("ID") Long id) {
        return databaseService.getRead(id);
        //TODO fix it
    }

    @ApiOperation(value = "查看未读(收到)的通知", notes = "根据id查看我未读(收到)的通知")
    @ApiImplicitParam(name = "ID", value = "接收者id", required = true, dataType = "Long", paramType = "path")
    @ResponseBody
    @RequestMapping(value = "/received/unread/{ID}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Collection<Notification> rcvdUnreadReadNotifications(@PathVariable("ID") Long id) {
        return databaseService.getUnread(id);
        //TODO fix it
    }

    @ApiOperation(value = "发布通知", notes = "向下级部门发送通知")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "receivers", value = "接收部门id列表", required = true, dataType = "List<Long>", paramType = "body"),
            @ApiImplicitParam(name = "title", value = "标题", required = true, dataType = "String", paramType = "body"),
            @ApiImplicitParam(name = "content", value = "内容", required = true, dataType = "String", paramType = "body")
    })
    @ResponseBody
    @RequestMapping(value = "", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void sendNotification(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                                 @RequestParam("receivers") List<Long> receivers, @RequestParam("title") String title,
                                 @RequestParam("content") String content){
        long id=(long) httpServletRequest.getSession().getAttribute("user");
        JSONObject jsonObject=databaseService.sentTotification(id,receivers,title,content);
        ResponseJsonObj.write(httpServletResponse,jsonObject);
    }

    @ApiOperation(value = "查看通知", notes = "未读通知状态会变为已读")
    @ApiImplicitParam(name = "ID", value = "通知id", required = true, dataType = "Long", paramType = "path")
    @ResponseBody
    @RequestMapping(value = "/{ID}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void readNotification(@PathVariable("ID") Long ID) {
        //TODO check ID owner before put
    }

    @ApiOperation(value = "删除通知", notes = "通知状态变为逻辑删除")
    @ApiImplicitParam(name = "ID", value = "通知id", required = true, dataType = "Long", paramType = "path")
    @ResponseBody
    @RequestMapping(value = "/{ID}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void delNotification(@PathVariable("ID") Long ID) {
        //TODO check ID owner before del
    }

}
