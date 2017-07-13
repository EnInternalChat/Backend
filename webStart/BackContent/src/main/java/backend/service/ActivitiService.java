package backend.service;

import backend.mdoel.DeployOfProcess;
import backend.mdoel.Employee;
import org.activiti.bpmn.exceptions.XMLException;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.*;
import org.activiti.engine.form.FormData;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.form.DateFormType;
import org.activiti.engine.impl.form.EnumFormType;
import org.activiti.engine.impl.form.StringFormType;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
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
        keywords.add("modify");
        keywords.add("apply");
        keywords.add("hr");
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
        jsonObject.put("info","操作失败，流程不存、已结束或是此流程不属于您的操作阶段，流程id: "+proId);
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

    //TODO role comfirm
    public JSONObject processStart(long companyID, long processDefID, String content, Employee starter) {
        DeployOfProcess deployOfProcess=databaseService.getDeployOfProcess(processDefID);
        Collection<String> labels=deployOfProcess.getLabels();
        String processKey=deployOfProcess.getProcessKey();
        List<ProcessDefinition> processDefinitions=repositoryService.createProcessDefinitionQuery().processDefinitionKey(processKey).list();
        if(processDefinitions.size() == 0) {
            return error(processKey);
        }
        ProcessDefinition processDefinition=processDefinitions.get(0);
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
        boolean status=databaseService.participantsGetInstance(companyID,deployOfProcess,proId,starter);
        if(!status) {
            JSONObject jsonObject=new JSONObject();
            jsonObject.put("done",false);
            jsonObject.put("info","当前公司缺乏执行相应流程的部门或人员");
            return jsonObject;
        }
        Task task = taskService.createTaskQuery().processInstanceId(proId).singleResult();
        Map<String,String> choices=new HashMap<>();
        formData=formService.getTaskFormData(task.getId());
        for(FormProperty formProperty:formData.getFormProperties()) {
            if(EnumFormType.class.isInstance(formProperty.getType())) {
                choices=(Map<String,String>) formProperty.getType().getInformation("values");
                break;
            }
        }
        String excID=task.getExecutionId();
        ExecutionEntity execution = (ExecutionEntity) runtimeService.createExecutionQuery().executionId(excID).singleResult();
        String nextActivityID=execution.getActivityId();
        String nextLabel="";
        for(String label:labels) {
            if(nextActivityID.toLowerCase().contains(label)) {
                nextLabel=label;
                break;
            }
        }
        Employee nextParticipant=databaseService.getSectionLeaderFromLabel(companyID,nextLabel,starter);
        taskService.setOwner(task.getId(),String.valueOf(nextParticipant.getID()));
        task.setOwner(String.valueOf(nextParticipant.getID()));
        System.out.println(task.getOwner()+"|"+nextParticipant.getID());
        operationTaskStage(proId,null,nextActivityID,execution.getName(),content,choices,nextParticipant,companyID,starter);
        JSONObject jsonObject=ok(proId);
        jsonObject.put("next",nextActivityID);
        return jsonObject;
    }

    private boolean operationTaskStage(String processID, String thisActivityID, String nextActivityID, String title,
                                       String content, Map<String,String> nextChoices, Employee nextParticipant,
                                       long companyID, Employee thisParticipant) {
        if(thisActivityID != null) {
            databaseService.thisTaskStageSet(processID, thisActivityID, content);//content for this, title for next
        }
        if(nextActivityID == null) {
            databaseService.nextEndSet(companyID,processID,title,"endEvent");
        } else {
            Collection<Map<String,String>> choices=new HashSet<>();
            Iterator<String> iter=nextChoices.keySet().iterator();
            while (iter.hasNext()) {
                Map<String,String> choice=new HashMap<>();
                String operationID=iter.next();
                choice.put("operationID",operationID);
                choice.put("operationName",nextChoices.get(operationID));
                choices.add(choice);
            }
            databaseService.nextTaskStageSet(companyID,processID,title,nextParticipant,nextActivityID,choices);
        }
        return true;
    }

    public JSONObject processOperation(long companyID, long processDefID, String processID, String operationID,
                                       String content, Employee operator) {
        JSONObject jsonObject=new JSONObject();
        ProcessInstance processInstance=runtimeService.createProcessInstanceQuery()
                .processInstanceId(processID).singleResult();
        if(processInstance == null || processInstance.isEnded()) {
            System.out.println("endornull:"+(processInstance == null)+"|"+processInstance.isEnded());
            return error(processID);
        }
        Task task = taskService.createTaskQuery().processInstanceId(processID).singleResult();
        System.out.println(task+"|"+task.getOwner());
        if(!task.getOwner().equals(String.valueOf(operator.getID()))) {
            return error(processID);
        }
        ExecutionEntity execution = (ExecutionEntity) runtimeService.createExecutionQuery()
                .executionId(task.getExecutionId()).singleResult();
        String thisActivityID=execution.getActivityId();
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
        processInstance=runtimeService.createProcessInstanceQuery()
                .processInstanceId(processID).singleResult();
        if(processInstance == null || processInstance.isEnded()) {
            jsonObject.put("done",true);
            operationTaskStage(processID,thisActivityID,null,"endEvent",content,null,null,companyID,null);
        } else {
            DeployOfProcess deployOfProcess=databaseService.getDeployOfProcess(processDefID);
            Collection<String> labels=deployOfProcess.getLabels();
            task=taskService.createTaskQuery().processInstanceId(processID).singleResult();
            execution = (ExecutionEntity) runtimeService.createExecutionQuery()
                    .executionId(task.getExecutionId()).singleResult();
            String nextActivityID=execution.getActivityId();
            String nextLabel="";
            for(String label:labels) {
                if(nextActivityID.toLowerCase().contains(label)) {
                    nextLabel=label;
                    break;
                }
            }
            Employee nextParticipant=databaseService.getSectionLeaderFromLabel(companyID,nextLabel,operator);
            if(nextParticipant == null) {
                jsonObject.put("done",false);
                jsonObject.put("info","执行失败没有找到下一阶段对应的流程执行人");
                return jsonObject;
            }
            Map<String,String> choices=null;
            taskService.setOwner(task.getId(),String.valueOf(nextParticipant.getID()));
            task.setOwner(String.valueOf(nextParticipant.getID()));
            formData=formService.getTaskFormData(task.getId());
            for(FormProperty formProperty:formData.getFormProperties()) {
                if(EnumFormType.class.isInstance(formProperty.getType())) {
                    choices=(Map<String,String>) formProperty.getType().getInformation("values");
                    jsonObject.put("next",choices);
                    break;
                }
            }
            jsonObject.put("done",true);
            operationTaskStage(processID,thisActivityID,nextActivityID,
                    execution.getName(),content,choices,nextParticipant,companyID,operator);
        }
        return jsonObject;
    }

    private long processToSave(String path, String name, long companyId, ProcessDefinition processDefinition, String data) {
        Set<String> labels=new HashSet<>();
        getProcessLabel(processDefinition,labels);
        DeployOfProcess deployOfProcess=new DeployOfProcess(databaseService.getIDeployOfProcess(),
                companyId,name,processDefinition.getKey(),path,data,System.currentTimeMillis(),
                System.currentTimeMillis(),0,labels);
        databaseService.saveDeployOfProcess(deployOfProcess);
        return deployOfProcess.getID();
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
        try {
            InputStream resourceAsStream=modelDiagram(processDefinition);
            byte[] b=new byte[resourceAsStream.available()];
            resourceAsStream.read(b, 0, b.length);
            BASE64Encoder encoder=new BASE64Encoder();
            String data=encoder.encode(b);
            long ID=processToSave(path,name,companyId,processDefinition,data);
            result.put("data",data);
            result.put("ID",ID);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public JSONObject delProcess(long ID, long companyID) {
        JSONObject jsonObject=new JSONObject();
        String processKey=databaseService.delDeployOfProcess(companyID,ID);
        if(processKey == null) {
            jsonObject.put("info","删除失败");
            return jsonObject;
        }
//        若删除key，则其他同key流程无法使用
//        try {
//            repositoryService.deleteDeployment(processKey);
//        } catch (ActivitiObjectNotFoundException e) {
//            e.printStackTrace();
//            System.out.println("引擎不存在此id的流程");
//        }
        jsonObject.put("info","删除成功");
        return jsonObject;
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
