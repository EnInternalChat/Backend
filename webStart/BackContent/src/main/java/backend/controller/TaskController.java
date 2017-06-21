package backend.controller;

import backend.mdoel.DeployOfProcess;
import backend.mdoel.Employee;
import backend.service.ActivitiService;
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
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lenovo on 2017/5/2.
 */

@RestController
@CrossOrigin
@Api(description = "任务流程")
@RequestMapping(value = "/tasks")
public class TaskController {
    DatabaseService databaseService;
    ActivitiService activitiService;

    @Autowired
    public TaskController(DatabaseService databaseService, ActivitiService activitiService) {
        this.databaseService = databaseService;
        this.activitiService = activitiService;
    }

    private Map<String, Object> infoType() {
        Map<String, Object> deployResult=new HashMap<>();
        deployResult.put("upload",false);
        deployResult.put("deploy",false);
        deployResult.put("info","非法操作!用户状态存在问题!");
        return deployResult;
    }

    private Map<String, Object> infoType(int type, String name) {
        Map<String, Object> deployResult=new HashMap<>();
        switch (type) {
            case 0:
                deployResult.put("upload",false);
                deployResult.put("deploy",false);
                deployResult.put("info",name+": 流程文件传输失败或传输文件本身为空");
                return deployResult;
            case 1:
                deployResult.put("upload",true);
                deployResult.put("deploy",false);
                deployResult.put("info",name+": 已存在同名流程");
                return deployResult;
            case 2:
                deployResult.put("upload",true);
                deployResult.put("deploy",false);
                deployResult.put("info",name+": 流程部署失败，请检查流程文件内容、格式、命名是否合法");
                return deployResult;
            case 3:
                deployResult.put("upload",true);
                deployResult.put("deploy",true);
                deployResult.put("info",name+"流程部署成功");
            default:
                return null;
        }
    }

    @ApiOperation(value = "部署流程", notes = "上传合法xml文件")
    @ApiImplicitParam(name = "newTaskFile", value = "流程文件", required = true, dataType = "CommonsMultipartFile", paramType = "body")
    @ResponseBody
    @RequestMapping(value = "/upload", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Map<String, Object> uploadProcess(@RequestParam("newTaskFile")CommonsMultipartFile file, HttpServletRequest request) {
        Employee employee=(Employee) request.getSession().getAttribute("user");
        if(employee == null) {
            return infoType();
        }
        //TODO cirfirm role
        long companyId=employee.getCompanyID();
        Map<String, Object> typeMap=activitiService.deployProcess(file, companyId);
        int type=(int) typeMap.get("type");
        String name=typeMap.get("name").toString();
        return infoType(type,name);
    }

    @ApiOperation(value = "启动流程", notes = "启动指定类型的流程")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processKey", value = "流程id", required = true, dataType = "String", paramType = "path"),
            @ApiImplicitParam(name = "content", value = "备注信息", dataType = "String", paramType = "body")
    })
    @ResponseBody
    @RequestMapping(value = "/start/{processKey}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void process(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                        @PathVariable("processKey") String processKey,
                        @RequestParam(value = "content", required = false) String content) {
        //TODO content
        long id=(long) httpServletRequest.getSession().getAttribute("user");
        Employee starter=databaseService.activeUserById(id);
        JSONObject jsonObject=activitiService.processStart(processKey,content,starter);
        ResponseJsonObj.write(httpServletResponse,jsonObject);
    }

    @ApiOperation(value = "处理任务", notes = "对任务进行操作处理")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processKey", value = "流程id", required = true, dataType = "String", paramType = "body"),
            @ApiImplicitParam(name = "processID", value = "流程实例id", required = true, dataType = "String", paramType = "body"),
            @ApiImplicitParam(name = "operationID", value = "操作id(对此步骤的操作)", required = true, dataType = "String", paramType = "path"),
            @ApiImplicitParam(name = "content", value = "备注信息json数组", dataType = "String", paramType = "body")
    })
    @ResponseBody
    @RequestMapping(value = "/operate/{processID}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void process(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                        @RequestParam("processKey") String processKey,
                        @PathVariable("processID") Long processID,
                        @RequestParam("operationID") String operationID,
                        @RequestParam(value = "content", required = false) String content) {
        long id=(long) httpServletRequest.getSession().getAttribute("user");
        Employee operator=databaseService.activeUserById(id);
        JSONObject jsonObject=activitiService.processOperation(processKey,processID.toString(),operationID,content,operator);
        ResponseJsonObj.write(httpServletResponse,jsonObject);
    }

    @ApiOperation(value = "流程列表", notes = "根据公司id获取已部署流程")
    @ApiImplicitParam(name = "companyID", value = "公司id", required = true, dataType = "Long", paramType = "path")
    @ResponseBody
    @RequestMapping(value = "/all/{companyID}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<DeployOfProcess> processes(@PathVariable("companyID") Long companyID) {
        return null;
        //TODO list
    }

    @ApiOperation(value = "删除流程部署", notes = "根据id删除流程")
    @ApiImplicitParam(name = "processKey", value = "流程id", required = true, dataType = "String", paramType = "path")
    @ResponseBody
    @RequestMapping(value = "/{processKey}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void deleteProcess(@PathVariable("processKey") String processKey) {
        return;
        //TODO del
    }

    @ApiOperation(value = "修改流程信息", notes = "根据流程id改名")
    @ApiImplicitParam(name = "processKey", value = "流程id", required = true, dataType = "String", paramType = "path")
    @ResponseBody
    @RequestMapping(value = "/{processKey}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void updateProcess(@PathVariable("processKey") String processKey) {
        return;
        //TODO put
    }

    @ApiOperation(value = "查看流程信息", notes = "根据流程id")
    @ApiImplicitParam(name = "processKey", value = "流程id", required = true, dataType = "String", paramType = "path")
    @ResponseBody
    @RequestMapping(value = "/{processKey}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void getProcess(@PathVariable("processKey") String processKey) {
        return;
        //TODO get
    }
}