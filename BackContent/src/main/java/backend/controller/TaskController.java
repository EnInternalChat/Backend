package backend.controller;

import backend.mdoel.Employee;
import backend.service.ActivitiService;
import backend.service.DatabaseService;
import backend.util.ResponseJsonObj;
import org.activiti.engine.impl.util.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lenovo on 2017/5/2.
 */

@Controller
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

    @ResponseBody
    @RequestMapping(value = "/start", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void process(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                        @RequestParam("processKey") String processKey,
                        @RequestParam(value = "content", required = false) Collection<Map<String,String>> content) {
        //TODO content
        long id=(long) httpServletRequest.getSession().getAttribute("user");
        Employee starter=databaseService.activeUserById(id);
        JSONObject jsonObject=activitiService.processStart(processKey,content,starter);
        ResponseJsonObj.write(httpServletResponse,jsonObject);
    }

    @ResponseBody
    @RequestMapping(value = "/operate", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void process(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                        @RequestParam("processKey") String processKey,
                        @RequestParam("processID") Long processID,
                        @RequestParam("operationID") String operationID,
                        @RequestParam(value = "content", required = false) Collection<Map<String,String>> content
                        ) {
        long id=(long) httpServletRequest.getSession().getAttribute("user");
        Employee operator=databaseService.activeUserById(id);
        activitiService.processOperation(processKey,processID.toString(),operationID,operator);
    }
}
