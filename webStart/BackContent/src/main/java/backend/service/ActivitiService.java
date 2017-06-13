package backend.service;

import backend.mdoel.Employee;
import backend.mdoel.InstanceOfProcess;
import org.activiti.bpmn.exceptions.XMLException;
import org.activiti.engine.*;
import org.activiti.engine.form.FormData;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.form.DateFormType;
import org.activiti.engine.impl.form.EnumFormType;
import org.activiti.engine.impl.form.StringFormType;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.util.json.JSONObject;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by lenovo on 2017/5/29.
 */

@Service
public class ActivitiService {
    DatabaseService databaseService;
    ProcessEngineConfiguration cfg;
    RepositoryService repositoryService;
    HistoryService historyService;
    TaskService taskService;
    IdentityService identityService;
    RuntimeService runtimeService;
    FormService formService;

    @Autowired
    public ActivitiService(DatabaseService databaseService, ProcessEngineConfiguration cfg,
                           RepositoryService repositoryService, HistoryService historyService,
                           TaskService taskService, IdentityService identityService,
                           RuntimeService runtimeService, FormService formService) {
        this.databaseService = databaseService;
        this.cfg = cfg;
        this.repositoryService = repositoryService;
        this.historyService = historyService;
        this.taskService = taskService;
        this.identityService = identityService;
        this.runtimeService=runtimeService;
        this.formService=formService;
    }

    private JSONObject ok(String proId) {
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("done",true);
        jsonObject.put("info","启动（操作）成功，流程ID: "+proId);
        return jsonObject;
    }

    private JSONObject error(String proId) {
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("done",false);
        jsonObject.put("info","操作失败，流程不存在或已结束，流程id: "+proId);
        return jsonObject;
    }

    private List<ActivityImpl> activityImplList(String processInstanceId) {
        ProcessInstance processInstance=runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId).singleResult();
        String definitionId=processInstance.getProcessDefinitionId();
        ProcessDefinitionEntity def = (ProcessDefinitionEntity) ((RepositoryServiceImpl)repositoryService)
                .getDeployedProcessDefinition(definitionId);
        List<ActivityImpl> activitiList = def.getActivities();
        return activitiList;
    }

//    private List<Map<String,String>> nextOperationChoice(String processInstanceId) {
//        List<Map<String,String>> choice=new ArrayList<>();
//        choice.add(new HashMap<String, String>());
//        choice.add(new HashMap<String, String>());
//        List<ActivityImpl> activitiList=activityImplList(processInstanceId);
//        for (ActivityImpl activity:activitiList) {
//            if(processInstanceId.equals(activity.getId())) {
//
//                break;
//            }
//        }
//    }

    public void test() {
        ProcessInstance processInstance=runtimeService.startProcessInstanceByKey("timerExample");
        String proId=processInstance.getId();
        System.out.println("def id: "+proId);
        processInstance=runtimeService.startProcessInstanceByKey("leave");
        proId=processInstance.getId();
        System.out.println("def id: "+proId);
    }

    //TODO role comfirm
    public JSONObject processStart(String processKey, String content, Employee starter) {
        System.out.println("employee: "+starter.hashCode());
        ProcessDefinition processDefinition=repositoryService.createProcessDefinitionQuery().processDefinitionKey(processKey).singleResult();
        FormData formData=formService.getStartFormData(processDefinition.getId());
        Map<String,String> variables=new HashMap<>();
        for(FormProperty formProperty:formData.getFormProperties()) {
            System.out.println(formProperty.getName());
            if(StringFormType.class.isInstance(formProperty.getType())) {
                variables.put(formProperty.getId(),content);
            } else if(DateFormType.class.isInstance((formProperty.getType()))) {
                SimpleDateFormat simpleDateFormat=new SimpleDateFormat(formProperty.getType().getInformation("datePattern").toString());
                variables.put(formProperty.getId(),simpleDateFormat.format(new Date(System.currentTimeMillis())));
            } else System.out.println("<form type not supported>");
        }
        ProcessInstance processInstance=formService.submitStartFormData(processDefinition.getId(),variables);
        String proId=processInstance.getId();
//        identityService.setAuthenticatedUserId(String.valueOf(starter.getID()));
//        List<Task> tasks = taskService.createTaskQuery().processInstanceId(proId).list();
//        for (Task task:tasks) {
//            Map<String, Object> variables=taskService.getVariables(task.getId());
//            task.setAssignee(String.valueOf(starter.getID()));
//            System.out.println(variables);
//            System.out.println(processInstance.getActivityId());
//        }
//        ProcessDefinitionEntity def = (ProcessDefinitionEntity) ((RepositoryServiceImpl)repositoryService).getDeployedProcessDefinition(processInstance.getProcessDefinitionId());
//        List<ActivityImpl> activitiList = def.getActivities();
//        System.out.println(activitiList.size());
//        for(ActivityImpl activity:activitiList) {
//            String id=activity.getId();
//            System.out.print(id+" out: ");
//            List<PvmTransition> outTransitions=activity.getOutgoingTransitions();
//            for(PvmTransition pvmTransition:outTransitions) {
//                    System.out.print("("+pvmTransition.getSkipExpression()+")");
//                    System.out.print("("+pvmTransition.getProperty("type")+")");
//                    System.out.print("("+pvmTransition.getProperty("conditionText")+")");
//
//                System.out.print(pvmTransition.getDestination().getId()+" ");
//            }
//
//            System.out.println();
//        }

        InstanceOfProcess instanceOfProcess=databaseService
                .saveProcessInstance(processKey,proId,processInstance.getName(),starter);
        databaseService.updateEmployeeCollectionData(starter,instanceOfProcess);
        return ok(proId);
    }

    public JSONObject processOperation(String processKey, String processID, String operationID, String content, Employee operator) {
        ProcessInstance processInstance=runtimeService.createProcessInstanceQuery()
                .processInstanceId(processID).singleResult();
        if(processInstance == null || processInstance.isEnded()) {
            return error(processID);
        }
        Task task = taskService.createTaskQuery().processInstanceId(processID).singleResult();
        task.setAssignee(operator.getName());
        Map<String, Object> variables=new HashMap<>();
        FormData formData=formService.getTaskFormData(task.getId());
        for(FormProperty formProperty:formData.getFormProperties()) {
            System.out.println(formProperty.getName());
            if(StringFormType.class.isInstance(formProperty.getType())) {
                variables.put(formProperty.getId(),content);
            } else if(DateFormType.class.isInstance((formProperty.getType()))) {
                SimpleDateFormat simpleDateFormat=new SimpleDateFormat(formProperty.getType().getInformation("datePattern").toString());
                variables.put(formProperty.getId(),simpleDateFormat.format(new Date(System.currentTimeMillis())));
            } else if(EnumFormType.class.isInstance(formProperty.getType())) {
                variables.put(formProperty.getId(),operationID);
            } else System.out.println("<form type not supported>");
        }
        taskService.complete(task.getId(),variables);
        JSONObject jsonObject=ok(processID);
        processInstance=runtimeService.createProcessInstanceQuery()
                .processInstanceId(processID).singleResult();
        if(processInstance == null || processInstance.isEnded()) {
            jsonObject.put("end",true);
        } else {
            jsonObject.put("end",false);
            System.out.println("same id: "+processID+" "+processInstance.getId());
            task=taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
            formData=formService.getTaskFormData(task.getId());
            for(FormProperty formProperty:formData.getFormProperties()) {
                if(EnumFormType.class.isInstance(formProperty.getType())) {
                    Map<String,String> choices=(Map<String,String>) formProperty.getType().getInformation("values");
                    System.out.println("enum map: "+choices);
                    jsonObject.put("next",choices);
                    break;
                }
            }
        }
        return jsonObject;
    }

    public Map<String, Object> deployProcess(CommonsMultipartFile file, long companyId) {
        Map<String, Object> result=new HashMap<>();
        result.put("name",file.getName());
        if(file.isEmpty()) {
            result.put("type",0);
            return result;
        }
        String path= this.getClass().getResource("/").getPath()+ File.separator+"tmp"+File.separator;
        File dir=new File(path);
        if(!dir.isDirectory()) dir.mkdir();
        System.out.println(path);
        File processFile=new File(path, file.getName());
        if(processFile.exists()) {
            result.put("type",1);
            return result;
        }
        ProcessDefinition processDefinition;
        try {
            processFile.createNewFile();
            file.transferTo(processFile);
            Deployment deployment;
            try {
                deployment = repositoryService.createDeployment().addClasspathResource("tmp"+File.separator+processFile.getName()).deploy();
                processDefinition=repositoryService.createProcessDefinitionQuery()
                        .deploymentId(deployment.getId()).singleResult();
            } catch (XMLException e) {
                result.put("type",2);
                return result;
            }
        } catch (IOException e) {
            result.put("type",2);
            return result;
        }
        System.out.println("Found process definition ["
                + processDefinition.getName() + "] with id ["
                + processDefinition.getId() + "]");
        //TODO add operation to mongo
        result.put("type",3);
        return result;
    }
}
