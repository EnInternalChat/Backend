package backend.controller;

import backend.service.DatabaseService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by lenovo on 2017/5/2.
 */
@Controller
@CrossOrigin
@RequestMapping(value = "/notifications")
public class NotificationController {
    @Autowired
    DatabaseService databaseService;

    @ApiOperation(value = "查看我发出的通知", notes = "根据id查看我发出的通知")
    @ApiImplicitParam(name = "ID", value = "发送者id", required = true, dataType = "Long", paramType = "path")
    @ResponseBody
    @RequestMapping(value = "/sent/{ID}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<Map<String,Object>> sentNotifications(@PathVariable("ID") Long id) {
        List<Map<String,Object>> notifications=new ArrayList<>();
        return notifications;
        //TODO fix?
    }

    @ApiOperation(value = "查看我收到的通知", notes = "根据id查看我收到的通知")
    @ApiImplicitParam(name = "ID", value = "接收者id", required = true, dataType = "Long", paramType = "path")
    @ResponseBody
    @RequestMapping(value = "/received/{ID}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<Map<String,Object>> rcvdNotifications(@PathVariable("ID") Long id) {
        List<Map<String,Object>> notifications=new ArrayList<>();
        return notifications;
        //TODO fix it
    }


}
