package backend.controller;

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

@RestController
@CrossOrigin
@Api(description = "聊天数据")
@RequestMapping(value = "/chats")
public class ChatController {
    @Autowired
    DatabaseService databaseService;

    @ApiOperation(value = "开启私人聊天", notes = "私聊功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "companyID",value = "公司id", required = true, dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "personID", value = "对方id", required = true, dataType = "Long", paramType = "body")
    })
    @ResponseBody
    @RequestMapping(value = "/single/{companyID}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void simpleChatAll(@PathVariable("companyID") Long companyID, @RequestParam("personID") Long ID) {

    }

    @ApiOperation(value = "开启群组聊天", notes = "群聊功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "companyID",value = "公司id", required = true, dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "groupIDList", value = "加入群聊的群组的id集合", required = true, dataType = "List", paramType = "body"),
    })
    @ResponseBody
    @RequestMapping(value = "/group/{companyID}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void groupChatStart(HttpServletResponse httpServletResponse,
                               @PathVariable("companyID") Long companyID, @RequestParam("groupIDList") List<Long> groupIDList) {
        JSONObject jsonObject=new JSONObject();
        Long chatID=databaseService.groupChatGenerate(companyID,groupIDList);
        jsonObject.put("chatID",chatID);
        ResponseJsonObj.write(httpServletResponse,jsonObject);
    }

    @ApiOperation(value = "删除聊天", notes = "从列表中移除(逻辑删除)")
    @ApiImplicitParam(name = "chatID", value = "聊天id", required = true, dataType = "Long", paramType = "path")
    @ResponseBody
    @RequestMapping(value = "/{chatID}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void delChat(@PathVariable("chatID") Long chatID) {

    }

}
