package backend.service;

import backend.mdoel.DeployOfProcess;
import backend.mdoel.Employee;
import backend.mdoel.InstanceOfProcess;
import org.activiti.bpmn.exceptions.XMLException;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.*;
import org.activiti.engine.form.FormData;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.form.DateFormType;
import org.activiti.engine.impl.form.EnumFormType;
import org.activiti.engine.impl.form.StringFormType;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.util.json.JSONObject;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.image.impl.DefaultProcessDiagramGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import sun.misc.BASE64Encoder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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

    Set<String> keywords;

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
        keywords=new HashSet<>();
        keyWordsSet();
    }

    private void keyWordsSet() {
        keywords.add("leader");
        keywords.add("hr");
        keywords.add("modify");
        keywords.add("applyUserId");
        keywords.add("manager");
        keywords.add("finance");
        keywords.add("design");
        keywords.add("tech");
        keywords.add("market");
        keywords.add("net");
        keywords.add("develop");
        keywords.add("service");
        keywords.add("politic");
        keywords.add("usercenter");
        keywords.add("devcenter");
        keywords.add("servicecenter");
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

    private boolean getProcessLabel(ProcessDefinition processDefinition, Set<String> labels) {
        ProcessDefinitionEntity def = (ProcessDefinitionEntity) ((RepositoryServiceImpl)repositoryService).getDeployedProcessDefinition(processDefinition.getId());
        List<ActivityImpl> activitiList = def.getActivities();
        System.out.println("size: "+activitiList.size());
        for(ActivityImpl activity:activitiList) {
            String id=activity.getId();
            System.out.print(id+" out: ");
            id=id.toLowerCase();
            for(String word:keywords) {
                if(id.contains(word)) {
                    labels.add(word);
                }
            }
            List<PvmTransition> outTransitions=activity.getOutgoingTransitions();
            for(PvmTransition pvmTransition:outTransitions) {
                System.out.print("【"+pvmTransition.getDestination().getId()+"】");
            }
            System.out.println();
        }
        return true;
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
//
//        identityService.setAuthenticatedUserId(String.valueOf(starter.getID()));
//        List<Task> tasks = taskService.createTaskQuery().processInstanceId(proId).list();
//        for (Task task:tasks) {
//            Map<String, Object> vars=taskService.getVariables(task.getId());
//            task.setAssignee(String.valueOf(starter.getID()));
//            System.out.println("vars:"+vars);
//            System.out.println("ActivityId"+processInstance.getActivityId());
//        }
//        ProcessDefinitionEntity def = (ProcessDefinitionEntity) ((RepositoryServiceImpl)repositoryService).getDeployedProcessDefinition(processInstance.getProcessDefinitionId());
//        List<ActivityImpl> activitiList = def.getActivities();
//        System.out.println(activitiList.size());
//        for(ActivityImpl activity:activitiList) {
//            String id=activity.getId();
//            System.out.print(id+" out: ");
//            List<PvmTransition> outTransitions=activity.getOutgoingTransitions();
//            for(PvmTransition pvmTransition:outTransitions) {
//                    System.out.print("【("+pvmTransition.getSkipExpression()+")");
//                    System.out.print("("+pvmTransition.getProperty("type")+")");
//                    System.out.print("("+pvmTransition.getProperty("conditionText")+")|-");
//                    System.out.print(pvmTransition.getDestination().getId()+"】");
//            }
//            System.out.println();
//        }

        InstanceOfProcess instanceOfProcess=databaseService
                .saveProcessInstance(processKey,proId,processInstance.getName(),starter);
        databaseService.addUpdateInstanceOfProcess(starter,instanceOfProcess);
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

    private boolean processToSave(String path, String name, long companyId, ProcessDefinition processDefinition) {
        Set<String> labels=new HashSet<>();
        getProcessLabel(processDefinition,labels);
        DeployOfProcess deployOfProcess=new DeployOfProcess(databaseService.getIDeployOfProcess(),
                companyId,name,path,System.currentTimeMillis(),
                System.currentTimeMillis(),0);
        databaseService.saveDeployOfProcess(deployOfProcess);
        return true;
    }

    public Map<String, Object> deployProcess(CommonsMultipartFile file, long companyId, String name) {
        Map<String, Object> result=new HashMap<>();
        result.put("name",name+".bpmn20.xml");
        if(file.isEmpty()) {
            result.put("type",0);
            return result;
        }
        String path= this.getClass().getResource("/").getPath()+"upload"+File.separator;
        File dir=new File(path);
        if(!dir.isDirectory()) dir.mkdir();
        System.out.println("path:"+path);
        File processFile=new File(path, name+".bpmn20.xml");
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
                deployment = repositoryService.createDeployment().addClasspathResource("upload"+File.separator+processFile.getName()).deploy();
                processDefinition=repositoryService.createProcessDefinitionQuery()
                        .deploymentId(deployment.getId()).singleResult();
            } catch (XMLException e) {
                result.put("type",2);
                processFile.delete();
                return result;
            }
        } catch (IOException e) {
            result.put("type",2);
            processFile.delete();
            return result;
        }
        if(processDefinition == null) {
            result.put("type",2);
            processFile.delete();
            return result;
        }
        System.out.println("Found process definition ["
                + processDefinition.getName() + "] with id ["
                + processDefinition.getId() + "]");
        result.put("type",3);
        processToSave(processFile.getAbsolutePath(),name,companyId,processDefinition);
        try {
            InputStream resourceAsStream=modelDiagram(processDefinition);
            byte[] b=new byte[resourceAsStream.available()];
            resourceAsStream.read(b, 0, b.length);
            BASE64Encoder encoder=new BASE64Encoder();
            String data=encoder.encode(b);
            result.put("data",data);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public InputStream modelDiagram(ProcessDefinition processDefinition) {
        ProcessDefinitionEntity entity=(ProcessDefinitionEntity) processDefinition;
        BpmnModel bpmnModel=repositoryService.getBpmnModel(entity.getId());
        List  activeActivityIds = new ArrayList<>(0);
        List highLightedFlows=new ArrayList<>(0);
        InputStream in = new DefaultProcessDiagramGenerator().generateDiagram(bpmnModel, "png", activeActivityIds,
                highLightedFlows,"宋体","宋体","宋体",null, 1.0);
        return in;
    }
}
