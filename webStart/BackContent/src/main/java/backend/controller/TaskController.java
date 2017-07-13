package backend.controller;

import backend.mdoel.DeployOfProcess;
import backend.mdoel.Employee;
import backend.mdoel.InstanceOfProcess;
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

    private JSONObject infoType(Map<String,Object> typeMap) {
        JSONObject deployResult=new JSONObject();
        int type=(int) typeMap.get("type");
        String name=(String) typeMap.get("name");
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
                String data=(String) typeMap.get("data");
                long ID=(long) typeMap.get("ID");
                deployResult.put("upload",true);
                deployResult.put("deploy",true);
                deployResult.put("data",data);
                deployResult.put("ID",ID);
                deployResult.put("info",name+"流程部署成功");
                return deployResult;
            default:
                return null;
        }
    }

    @ApiOperation(value = "部署流程", notes = "上传合法xml文件")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "newTaskFile", value = "流程文件", required = true, dataType = "CommonsMultipartFile", paramType = "body"),
            @ApiImplicitParam(name = "name", value = "流程名称", required = true, dataType = "String", paramType = "body"),
            @ApiImplicitParam(name = "companyID",value = "公司id", required = true, dataType = "Long", paramType = "path")
    })
    @ResponseBody
    @RequestMapping(value = "/upload/{companyId}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void uploadProcess(@RequestParam("newTaskFile")CommonsMultipartFile file, @RequestParam("name") String name,
                              @PathVariable("companyId") Long companyId,
                              HttpServletResponse response) {
        JSONObject jsonObject;
        //TODO cirfirm role
        Map<String,Object> typeMap=activitiService.deployProcess(file, companyId, name);
        System.out.println(file);
        jsonObject=infoType(typeMap);
        ResponseJsonObj.write(response,jsonObject);
    }

    @ApiOperation(value = "启动流程", notes = "启动指定类型的流程")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "companyID",value = "公司id", required = true, dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "ID",value = "发起者id", required = true, dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "processDefID", value = "流程定义id", required = true, dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "content", value = "备注信息", dataType = "String", paramType = "body")
    })
    @ResponseBody
    @RequestMapping(value = "/start/{companyID}/{ID}/{processDefID}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void startProcess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                             @PathVariable("processDefID") Long processDefID, @PathVariable("companyID") Long companyID,
                             @PathVariable("ID") Long ID,
                             @RequestParam(value = "content", required = false) String content) {
        Employee starter=databaseService.activeUserById(ID);
        JSONObject jsonObject=activitiService.processStart(companyID,processDefID,content,starter);
        ResponseJsonObj.write(httpServletResponse,jsonObject);
    }

    @ApiOperation(value = "处理任务", notes = "对任务进行操作处理")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "companyID",value = "公司id", required = true, dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "ID",value = "发起者id", required = true, dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "processID", value = "流程实例id", required = true, dataType = "String", paramType = "path"),
            @ApiImplicitParam(name = "operationID", value = "操作id(对此步骤的操作)", required = true, dataType = "String", paramType = "body"),
            @ApiImplicitParam(name = "content", value = "备注信息json数组", dataType = "String", paramType = "body")
    })
    @ResponseBody
    @RequestMapping(value = "/operate/{companyID}/{ID}/{processID}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void operateProcess(HttpServletResponse httpServletResponse,
                               @PathVariable("processID") Long processID, @PathVariable("companyID") Long companyID,
                               @RequestParam("operationID") String operationID, @PathVariable("ID") Long ID,
                               @RequestParam(value = "content", required = false) String content) {
        Employee operator=databaseService.activeUserById(ID);
        JSONObject jsonObject=activitiService.processOperation(processID.toString(),operationID,content,operator);
        ResponseJsonObj.write(httpServletResponse,jsonObject);
    }

    @ApiOperation(value = "流程列表", notes = "根据公司id获取已部署流程")
    @ApiImplicitParam(name = "companyID", value = "公司id", required = true, dataType = "Long", paramType = "path")
    @ResponseBody
    @RequestMapping(value = "/all/{companyID}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<DeployOfProcess> processes(@PathVariable("companyID") Long companyID) {
        return databaseService.findDeployOfProcess(companyID);
    }

    @ApiOperation(value = "用户已完成流程列表", notes = "用户参与且当前已完成的流程实例")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "companyID", value = "公司id", required = true, dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "ID", value = "用户id", required = true, dataType = "Long", paramType = "path")
    })
    @ResponseBody
    @RequestMapping(value = "/over/{companyID}/{ID}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<InstanceOfProcess> finishedInstance(@PathVariable("companyID") Long companyID, @PathVariable("ID") Long ID) {
        return null;
}
        //TODO list

    @ApiOperation(value = "用户未完成流程列表", notes = "用户参与且当前未完成的流程实例")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "companyID", value = "公司id", required = true, dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "ID", value = "用户id", required = true, dataType = "Long", paramType = "path")
    })
    @ResponseBody
    @RequestMapping(value = "/working/{companyID}/{ID}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<InstanceOfProcess> workingInstance(@PathVariable("companyID") Long companyID, @PathVariable("ID") Long ID) {
        return null;
        //TODO list
    }

    @ApiOperation(value = "删除流程部署", notes = "根据id删除流程")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "companyID", value = "公司id", required = true, dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "ID", value = "流程id", required = true, dataType = "String", paramType = "path")
    })
    @ResponseBody
    @RequestMapping(value = "/{companyID}/{ID}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void deleteProcess(HttpServletResponse httpServletResponse,
                              @PathVariable("companyID") Long companyID, @PathVariable("ID") Long ID) {
        JSONObject jsonObject=activitiService.delProcess(ID,companyID);
        ResponseJsonObj.write(httpServletResponse,jsonObject);
    }

    @ApiOperation(value = "修改流程信息", notes = "根据流程id改名")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "ID", value = "流程id", required = true, dataType = "String", paramType = "path"),
            @ApiImplicitParam(name = "newName", value = "新名", required = true, dataType = "String", paramType = "body")
    })
    @ResponseBody
    @RequestMapping(value = "/{companyID}/{ID}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void updateProcess(HttpServletResponse httpServletResponse,
                              @PathVariable("companyID") Long companyID, @PathVariable("ID") Long ID, @RequestParam("newName") String newName) {
        JSONObject jsonObject=databaseService.updateProcessName(companyID,ID,newName);
        ResponseJsonObj.write(httpServletResponse,jsonObject);
    }

    @ApiOperation(value = "查看流程信息", notes = "根据流程id")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "companyID", value = "公司id", required = true, dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "ID", value = "流程id", required = true, dataType = "String", paramType = "path")
    })
    @ResponseBody
    @RequestMapping(value = "/{companyID}/{ID}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void getProcess(HttpServletResponse httpServletResponse,
                           @PathVariable("companyID") Long companyID, @PathVariable("ID") Long ID) {
        JSONObject jsonObject=databaseService.getProcessDiagram(companyID, ID);
        ResponseJsonObj.write(httpServletResponse,jsonObject);
    }
}
