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

    @ApiOperation(value = "开启群组聊天", notes = "群聊功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "companyID",value = "公司id", required = true, dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "sectionID", value = "部门id", required = true, dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "ID",value = "人员id", required = true, dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "groupIDList", value = "加入群聊的群组的id集合", required = true, dataType = "List", paramType = "body"),
    })
    @ResponseBody
    @RequestMapping(value = "/group/{companyID}/{sectionID}/{ID}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void groupChatStart(HttpServletResponse httpServletResponse,
                               @PathVariable("companyID") Long companyID, @PathVariable("sectionID") Long sectionID, @PathVariable("ID") Long ID,
                               @RequestParam("groupIDList") List<Long> groupIDList) {
        JSONObject jsonObject=new JSONObject();
        jsonObject=databaseService.groupChatGenerate(companyID,sectionID,ID,groupIDList);
        ResponseJsonObj.write(httpServletResponse,jsonObject);
        //TODO check if leader
    }

}
