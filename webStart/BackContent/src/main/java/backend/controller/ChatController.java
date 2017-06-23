package backend.controller;

import backend.service.DatabaseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@Api(description = "聊天数据")
@RequestMapping(value = "/chats")
public class ChatController {
    @Autowired
    DatabaseService databaseService;

    @ApiOperation(value = "开启私人聊天", notes = "私聊功能")
    @ApiImplicitParam(name = "personID", value = "对方id", required = true, dataType = "Long", paramType = "body")
    @ResponseBody
    @RequestMapping(value = "/single", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void simpleChatAll(@RequestParam("personID") Long ID) {

    }

    @ApiOperation(value = "获取私聊列表", notes = "未删除的私聊信息")
    @ApiImplicitParam(name = "personID", value = "对方id", required = true, dataType = "Long", paramType = "body")
    @ResponseBody
    @RequestMapping(value = "/single", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void simpleChatGet(@RequestParam("personID") Long ID) {

    }

    @ApiOperation(value = "开启群组聊天", notes = "群聊功能")
    @ApiImplicitParam(name = "groupIDList", value = "加入群聊的群组的id集合", required = true, dataType = "List", paramType = "body")
    @ResponseBody
    @RequestMapping(value = "/group", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void groupChatStart(@RequestParam("groupIDList") List<Long> groupIDList) {

    }

    @ApiOperation(value = "获取群聊列表", notes = "未删除的群聊信息")
    @ResponseBody
    @RequestMapping(value = "/group", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void groupChatAll() {

    }

    @ApiOperation(value = "删除聊天", notes = "从列表中移除(逻辑删除)")
    @ApiImplicitParam(name = "chatID", value = "聊天id", required = true, dataType = "Long", paramType = "path")
    @ResponseBody
    @RequestMapping(value = "/{chatID}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void delChat(@PathVariable("chatID") Long chatID) {

    }

}
