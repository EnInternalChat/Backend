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

import javax.servlet.http.HttpServletResponse;
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

    @ApiOperation(value = "查看发出的通知列表", notes = "根据id查看我发出的通知")
    @ApiImplicitParam(name = "ID", value = "发送者id", required = true, dataType = "Long", paramType = "path")
    @ResponseBody
    @RequestMapping(value = "/sent/{ID}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<Notification> sentNotifications(@PathVariable("ID") Long id) {
        return databaseService.getSent(id);
        //TODO fix?
    }

    @ApiOperation(value = "查看已读(收到)的通知列表", notes = "根据id查看我已读(收到)的通知")
    @ApiImplicitParam(name = "ID", value = "接收者id", required = true, dataType = "Long", paramType = "path")
    @ResponseBody
    @RequestMapping(value = "/received/read/{ID}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<Notification> rcvdReadNotifications(@PathVariable("ID") Long id) {
        return databaseService.getRead(id);
        //TODO fix it
    }

    @ApiOperation(value = "查看未读(收到)的通知列表", notes = "根据id查看我未读(收到)的通知")
    @ApiImplicitParam(name = "ID", value = "接收者id", required = true, dataType = "Long", paramType = "path")
    @ResponseBody
    @RequestMapping(value = "/received/unread/{ID}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<Notification> rcvdUnreadReadNotifications(@PathVariable("ID") Long id) {
        return databaseService.getUnread(id);
        //TODO fix it
    }

    @ApiOperation(value = "发布通知", notes = "向下级部门发送通知")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "ID", value = "发送者id", required = true, dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "receivers", value = "接收部门id列表", required = true, dataType = "List<Long>", paramType = "body"),
            @ApiImplicitParam(name = "title", value = "标题", required = true, dataType = "String", paramType = "body"),
            @ApiImplicitParam(name = "content", value = "内容", required = true, dataType = "String", paramType = "body")
    })
    @ResponseBody
    @RequestMapping(value = "/send/{ID}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void sendNotification(HttpServletResponse httpServletResponse,
                                 @RequestParam("receivers") List<Long> receivers, @RequestParam("title") String title,
                                 @RequestParam("content") String content, @PathVariable("ID") Long id){
        JSONObject jsonObject=databaseService.sentTotification(id,receivers,title,content);
        ResponseJsonObj.write(httpServletResponse,jsonObject);
    }

    @ApiOperation(value = "查看通知", notes = "未读通知状态会变为已读")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "ID", value = "通知id", required = true, dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "notificationID", value = "查看者id", required = true, dataType = "Long", paramType = "path")
    })
    @ResponseBody
    @RequestMapping(value = "/{ID}/{notificationID}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Notification readNotification(@PathVariable("ID") Long ID, @PathVariable("notificationID") Long notificationID) {
        Notification notification=databaseService.readNotification(notificationID,ID);
        return notification;
        //TODO check ID owner before put
    }

    @ApiOperation(value = "删除已读通知", notes = "通知状态变为逻辑删除")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "ID", value = "通知id", required = true, dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "notificationID", value = "查看者id", required = true, dataType = "Long", paramType = "path")
    })
    @ResponseBody
    @RequestMapping(value = "/received/{ID}/{notificationID}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void delRcvdNotification(HttpServletResponse httpServletResponse,
                                @PathVariable("ID") Long ID, @PathVariable("notificationID") Long notificationID) {
        JSONObject jsonObject=databaseService.logicDelReadNotification(notificationID,ID);
        ResponseJsonObj.write(httpServletResponse,jsonObject);
        //TODO check ID owner before del
    }


    @ApiOperation(value = "删除已发送通知", notes = "通知状态变为逻辑删除")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "ID", value = "通知id", required = true, dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "notificationID", value = "发送者id", required = true, dataType = "Long", paramType = "path")
    })
    @ResponseBody
    @RequestMapping(value = "/sent/{ID}/{notificationID}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void delSentNotification(HttpServletResponse httpServletResponse,
                                @PathVariable("ID") Long ID, @PathVariable("notificationID") Long notificationID) {
        JSONObject jsonObject=databaseService.logicDelSentNotification(notificationID,ID);
        ResponseJsonObj.write(httpServletResponse,jsonObject);
        //TODO check ID owner before del
    }
}
