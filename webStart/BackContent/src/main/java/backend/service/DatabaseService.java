package backend.service;

import backend.mdoel.*;
import backend.repository.*;
import cn.jiguang.common.ClientConfig;
import cn.jiguang.common.resp.APIConnectionException;
import cn.jiguang.common.resp.APIRequestException;
import cn.jmessage.api.JMessageClient;
import cn.jmessage.api.common.model.RegisterInfo;
import cn.jmessage.api.group.CreateGroupResult;
import cn.jmessage.api.group.GroupInfoResult;
import cn.jmessage.api.group.GroupListResult;
import cn.jmessage.api.user.UserInfoResult;
import cn.jmessage.api.user.UserListResult;
import cn.jmessage.api.user.UserStateResult;
import cn.jpush.api.JPushClient;
import cn.jpush.api.push.PushResult;
import cn.jpush.api.push.model.Platform;
import cn.jpush.api.push.model.PushPayload;
import cn.jpush.api.push.model.audience.Audience;
import com.mongodb.DBRef;
import com.mongodb.WriteResult;
import org.activiti.engine.impl.util.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.BasicMongoPersistentEntity;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.util.ClassTypeInformation;
import org.springframework.session.SessionRepository;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.util.*;

/**
 * Created by lenovo on 2017/5/7.
 */

@Service
public class DatabaseService {
    private final EmployeeRepository employeeRepository;
    private final NotificationRepository notificationRepository;
    private final DeployOfProcessRepository deployOfProcessRepository;
    private final ChatRepository chatRepository;
    private final CompanyRepository companyRepository;
    private final SectionRepository sectionRepository;
    private final SessionRepository sessionRepository;
    private final TaskStageRepository taskStageRepository;
    private final InstanceOfProcessRepository instanceOfProcessRepository;
    private final MongoTemplate mongoTemplate;

    private final Map<Long,Employee> userActive;
    private final Map<Long,HttpSession> sessionActive;

    private static String APP_KEY="f784911007eb5e69ef4a773f";
    private static String MASTER_SECRET="d4c4f6da868458e48f4de0e8";
    private JPushClient jPushClient;
    private JMessageClient jMessageClient;

    @Autowired
    public DatabaseService(EmployeeRepository employeeRepository, NotificationRepository notificationRepository,
                           DeployOfProcessRepository deployOfProcessRepository, ChatRepository chatRepository,
                           CompanyRepository companyRepository, SectionRepository sectionRepository,
                           SessionRepository sessionRepository, TaskStageRepository taskStageRepository,
                           InstanceOfProcessRepository instanceOfProcessRepository, MongoTemplate mongoTemplate) {
        userActive=new HashMap<>();
        sessionActive=new HashMap<>();
        this.employeeRepository = employeeRepository;
        this.notificationRepository = notificationRepository;
        this.deployOfProcessRepository = deployOfProcessRepository;
        this.chatRepository = chatRepository;
        this.companyRepository=companyRepository;
        this.sectionRepository=sectionRepository;
        this.sessionRepository=sessionRepository;
        this.taskStageRepository=taskStageRepository;
        this.instanceOfProcessRepository=instanceOfProcessRepository;
        this.mongoTemplate=mongoTemplate;
        jPushClient=new JPushClient(MASTER_SECRET,APP_KEY,null, ClientConfig.getInstance());
        jMessageClient=new JMessageClient(APP_KEY,MASTER_SECRET);
        int ICompany=companyRepository.findAll().size();
        int IEmployee=employeeRepository.findAll().size();
        int INotification=notificationRepository.findAll().size();
        int ISection=sectionRepository.findAll().size();
        System.out.println(ICompany+" "+IEmployee+" "+INotification+" "+ISection);
        //initJiguangDel();
        //initJiguangCreate();
    }

    private void initJiguangDel() {
        //chat server user set
        try {
            GroupListResult groupListResult=jMessageClient.getGroupListByAppkey(0,100);
            List<GroupInfoResult> groupInfoResultList=groupListResult.getGroups();
            for(GroupInfoResult info:groupInfoResultList) {
                if(info.getName().contains("testgroup")) {
                    continue;
                }
                jMessageClient.deleteGroup(info.getGid());
            }
            UserListResult userList=jMessageClient.getUserList(0,200);//TODO 200?
            UserInfoResult[] userListUsers=userList.getUsers();
            for(UserInfoResult info:userListUsers) {
                if(info.getUsername().contains("testuser")) {
                    continue;
                }
                jMessageClient.deleteUser(info.getUsername());
            }

        } catch (APIConnectionException e) {
            System.out.println("Connection error, should retry later");
        } catch (APIRequestException e) {
            System.out.println("HTTP Status: " + e.getStatus());
            System.out.println("Error Code: " + e.getErrorCode());
            System.out.println("Error Message: " + e.getErrorMessage());
        }
    }

    private void initJiguangCreate() {
        try {
            List<Employee> employeeList=employeeRepository.findAll();
            List<RegisterInfo> users = new ArrayList<>();
            for(Employee employee:employeeList) {
                RegisterInfo user = RegisterInfo.newBuilder()
                        .setUsername(employee.getName())
                        .setPassword(employee.getPassword())
                        .build();
                users.add(user);
            }
            if(users.size() != 0) {
                RegisterInfo[] regUsers = new RegisterInfo[users.size()];
                jMessageClient.registerUsers(users.toArray(regUsers));
                for (Employee employee : employeeList) {
                    int avatar = employee.getAvatar();
                    String username = employee.getName();
                    int gender = employee.isGender() ? 1 : 0;
                    jMessageClient.updateUserInfo(username, avatar + "", null, null, gender, null, null);
                }
            }
        } catch (APIConnectionException e) {
            System.out.println("Connection error, should retry later");
        } catch (APIRequestException e) {
            System.out.println("HTTP Status: " + e.getStatus());
            System.out.println("Error Code: " + e.getErrorCode());
            System.out.println("Error Message: " + e.getErrorMessage());
        }
    }

    public Collection<Employee> em() {
        return employeeRepository.findAll();
    }

    public JSONObject statusSet(long companyID, long sectionID, long id) {
        JSONObject jsonObject=new JSONObject();
        Company company=companyRepository.findOne(companyID);
        if(company == null) {
            jsonObject.put("info","当前公司id不存在");
            return jsonObject;
        }
        Section section=sectionRepository.findOne(sectionID);
        if(section == null || section.getCompanyID() != companyID) {
            jsonObject.put("info","当前部门id不存在或当前id部门不属于此公司");
            return jsonObject;
        }
        Employee employee=employeeRepository.findOne(id);
        if(employee == null || employee.getSectionID() !=sectionID) {
            jsonObject.put("info","当前员工id不存在或当前id员工不属于此部门");
            return jsonObject;
        }
        boolean status=employee.isActive();
        employee.setActive(!status);
        employeeRepository.save(employee);
        if(employee == userActive.get(id)) {
            HttpSession httpSession=sessionActive.get(id);
            httpSession.invalidate();
            sessionRepository.delete(httpSession.getId());
            userActive.remove(id);
            sessionActive.remove(id);
        }
        if(status) {
            jsonObject.put("info","用户已被禁用");
        } else {
            jsonObject.put("info","用户已被激活");
        }
        return jsonObject;
    }

    public Employee getSectionLeaderFromLabel(long companyID, String label, Employee finder) {
        Company company=companyRepository.findOne(companyID);
        Employee leader;
        if(company == null) {
            System.out.println("当前公司id不存在");
            return null;
        }
        if(label.equals("leader")) {
            Section section=sectionRepository.findOne(finder.getSectionID());
            leader=section.getLeader();
            return leader;
        } else if(label.equals("modify") || label.equals("apply")){
            return finder;
        }
        List<Section> sections=sectionRepository.findByCompanyIDAndLabel(companyID,label);
        if(sections.size() == 0 || sections.get(0).getCompanyID() != companyID) {
            System.out.println("当前部门id不存在或当前id部门不属于此公司");
            System.out.println("size:"+sections.size());
            return null;
        }
        leader=sections.get(0).getLeader();
        return leader;
    }

    public JSONObject updateEmployeePersonal(Long companyID, Long sectionID, long id,
                                             String newPwd, String email1, String email2,
                                             String phone1, String phone2, Integer avatar,
                                             Long newSectionID) {
        JSONObject jsonObject=new JSONObject();
        String info="";
        Employee employee=employeeRepository.findOne(id);
        if(employee == null) {
            jsonObject.put("info", "当前用户id不存在");
            return jsonObject;
        } else if(employee.getCompanyID() != companyID || employee.getSectionID() != sectionID) {
            jsonObject.put("info", "当前公司不存在此id用户");
            return jsonObject;
        }
        if(newSectionID != null) {
            employee.setSectionID(newSectionID);
            Section oldSection=sectionRepository.findOne(employee.getSectionID());
            Section newSection=sectionRepository.findOne(newSectionID);
            oldSection.delMember(employee);
            newSection.addMember(employee);
            delUpdateMembers(oldSection,employee);
            addUpdateMembers(newSection,employee);
            info+="|用户部门修改成功";
        }
        if(avatar != null) {
            employee.setAvatar(avatar);
            info+="|头像修改成功";
        }
        if(newPwd != null) {
            employee.setPassword(newPwd);
            info+="|密码修改成功";
        }
        if(employee.updatePhone(phone1,phone2)) {
            info+="|电话联系方式修改成功";
        }
        if(employee.updateMail(email1,email2)){
            info+="|邮件联系方式修改成功";
        }
        if(info == "") {
            jsonObject.put("info","修改对象: "+employee.getName()+"|没有任何信息被修改");
        } else {
            jsonObject.put("info", "修改对象: "+employee.getName()+"|"+info);
        }
        employeeRepository.save(employee);
        return jsonObject;
        //TODO update failed
    }

    public JSONObject delEmployee(Long companyID, Long sectionID, long id) {
        JSONObject result=new JSONObject();
        Company company=companyRepository.findOne(companyID);
        if(company == null) {
            result.put("info","当前公司id不存在");
            return result;
        }
        Section section=sectionRepository.findOne(sectionID);
        if(section == null || section.getCompanyID() != companyID) {
            result.put("info","当前部门id不存在或当前id部门不属于此公司");
            return result;
        }
        Employee employee=employeeRepository.findOne(id);
        if(employee == null || employee.getSectionID() !=sectionID) {
            result.put("info","当前员工id不存在或当前id员工不属于此部门");
            return result;
        }
        section.delMember(employee);
        delUpdateMembers(section,employee);
        employeeRepository.delete(id);
        try {
            jMessageClient.deleteUser(employee.getName());
            result.put("info","删除成功");
        } catch (APIConnectionException e) {
            result.put("info","员工删除成功,极光删除失败");
            System.out.println("Connection error. Should retry later. ");
        } catch (APIRequestException | NullPointerException e) {
            result.put("info","员工删除成功,极光删除失败");
            System.out.println("Error Message: " + e.getMessage());
        }
        return result;
    }

    public JSONObject addEmployee(Long companyID, Long sectionID, String name, String password, boolean gender) {
        JSONObject result=new JSONObject();
        Company company=companyRepository.findOne(companyID);
        if(company == null) {
            result.put("info","当前公司id不存在");
            return result;
        }
        Section section=sectionRepository.findOne(sectionID);
        if(section == null || section.getCompanyID() != companyID) {
            result.put("info","当前部门id不存在或当前id部门不属于此公司");
            return result;
        }
        if(name.length() < 4) {
            result.put("info","用户名不得少于4个字符");
            return result;
        }
        Employee employee=new Employee(getIEmployee(),companyID,sectionID,name,password,gender);
        employeeRepository.save(employee);
        section.addMember(employee);
        addUpdateMembers(section,employee);
        try {
            RegisterInfo registerInfo=RegisterInfo.newBuilder()
                    .setUsername(name)
                    .setPassword(password)
                    .build();
            List<RegisterInfo> registerInfos=new ArrayList<>();
            registerInfos.add(registerInfo);
            RegisterInfo[] regUsers=new RegisterInfo[1];
            jMessageClient.registerUsers(registerInfos.toArray(regUsers));
            jMessageClient.updateUserInfo(name,"1",null,null,1,null,null);
            result.put("info","员工添加成功");
            result.put("ID",employee.getID());
        } catch (APIConnectionException e) {
            result.put("info","员工添加成功,极光注册失败");
            System.out.println("Connection error. Should retry later. ");
        } catch (APIRequestException | NullPointerException e) {
            result.put("info","员工添加成功,极光注册失败");
            System.out.println("Error Message: " + e.getMessage());
        }
        return result;
    }

    public DeployOfProcess getDeployOfProcess(long ID) {
        DeployOfProcess deployOfProcess=deployOfProcessRepository.findOne(ID);
        return deployOfProcess;
    }

    public boolean saveDeployOfProcess(DeployOfProcess deployOfProcess) {
        deployOfProcessRepository.save(deployOfProcess);
        Company company=companyRepository.findOne(deployOfProcess.getCompanyID());
        company.addDeployOfProcess(deployOfProcess);
        addUpdateDeployOfProcess(company,deployOfProcess);
        return true;
    }

    public String delDeployOfProcess(long companyID, long ID) {
        DeployOfProcess deployOfProcess=deployOfProcessRepository.findOne(ID);
        if(deployOfProcess == null) return null;
        String processKey=deployOfProcess.getProcessKey();
        Company company=companyRepository.findOne(companyID);
        deployOfProcessRepository.delete(deployOfProcess);
        delUpdateDeployOfProcess(company,deployOfProcess);
        File file=new File(deployOfProcess.getPath()+File.separator+deployOfProcess.getName()+".bpmn20.xml");
        if(file.exists()) {
            if(file.delete()) {
                System.out.println("delete:"+file.getName());
            }
        }
        return processKey;
    }

    public JSONObject updateProcessName(long companyID, long ID, String newName) {
        JSONObject jsonObject=new JSONObject();
        DeployOfProcess deployOfProcess=deployOfProcessRepository.findOne(ID);
        deployOfProcess.setName(newName);
        deployOfProcessRepository.save(deployOfProcess);
        String newFileName=deployOfProcess.getPath()+File.separator+newName+".bpmn20.xml";
        File newFile=new File(newFileName);
        if(newFile.exists()) {
            jsonObject.put("info","修改失败, 已存在此流程文件");
            return jsonObject;
        } else {
            File file=new File(deployOfProcess.getPath()+File.separator+deployOfProcess.getName()+File.separator+".bpmn20.xml");
            file.renameTo(newFile);
        }
        jsonObject.put("info","修改成功");
        return jsonObject;
    }

    public List<DeployOfProcess> findDeployOfProcess(long companyID) {
        List<DeployOfProcess> deployOfProcesses=deployOfProcessRepository.findByCompanyID(companyID);
        Collections.sort(deployOfProcesses, new Comparator<DeployOfProcess>() {
            @Override
            public int compare(DeployOfProcess o1, DeployOfProcess o2) {
                if(o1.getUpdateTime()<o2.getUpdateTime()) return 1;
                else if(o1.getUpdateTime()>o2.getUpdateTime()) return -1;
                else return 0;
            }
        });
        return deployOfProcesses;
    }

    public JSONObject getProcessDiagram(long companyID, long ID) {
        JSONObject jsonObject=new JSONObject();
        DeployOfProcess deployOfProcess=deployOfProcessRepository.findOne(ID);
        jsonObject.put("data",deployOfProcess.getData());
        return jsonObject;
    }

    public boolean participantsGetInstance(long companyID, DeployOfProcess deployOfProcess, String processID, Employee applyer) {
        Collection<String> labels=deployOfProcess.getLabels();
        String processName=deployOfProcess.getName();
        InstanceOfProcess instance;
        instance=new InstanceOfProcess(getIInstanceOfProcess(),companyID,deployOfProcess.getID(),processID,processName,applyer);
        instanceOfProcessRepository.save(instance);
        Set<Employee> participants=new HashSet<>();
        for(String label:labels) {
            Employee participant;
            if(label.equals("modify") || label.equals("apply")) {
                participant=applyer;
            } else {
                participant= getSectionLeaderFromLabel(companyID, label, applyer);
            }
            if(participant != null) {
                participants.add(participant);
            } else {
                instanceOfProcessRepository.delete(instance);
                return false;
            }
        }
        for(Employee participant:participants) {
            addUpdateInstanceOfProcess(participant,instance);
        }
        return true;
    }

    public boolean thisTaskStageSet(String processID, String activityID, String content) {
        List<TaskStage> taskStages=taskStageRepository
                .findByProcessIDAndActivityID(processID,activityID);
        if(taskStages.size() == 0 || taskStages.size() > 1) {
            System.out.println("taskStages Count Error:"+taskStages.size());
            return false;
        }
        TaskStage taskStage=taskStages.get(0);
        taskStage.setFinishTime(System.currentTimeMillis());
        taskStage.setContent(content);
        taskStageRepository.save(taskStage);
        return true;
    }

    public boolean nextTaskStageSet(long companyID, String processID, String title, Employee employee,
                                    String activityID, Collection<Map<String, String>> choices) {
        TaskStage taskStage=new TaskStage(getITaskStage(),activityID,processID,
                System.currentTimeMillis(),title,employee,choices);
        List<InstanceOfProcess> instanceOfProcesses=instanceOfProcessRepository
                .findByCompanyIDAndAndProcessID(companyID,processID);
        if(instanceOfProcesses.size() == 0) return false;
        InstanceOfProcess instance=instanceOfProcesses.get(0);
        taskStageRepository.save(taskStage);
        instance.addStage(taskStage);
        addUpdateTaskStage(instance,taskStage);
        return true;
    }

    public boolean nextEndSet(long companyID, String processID, String title, String activityID) {
        TaskStage taskStage=new TaskStage(getITaskStage(),activityID,title,processID,System.currentTimeMillis());
        List<InstanceOfProcess> instanceOfProcesses=instanceOfProcessRepository
                .findByCompanyIDAndAndProcessID(companyID,processID);
        if(instanceOfProcesses.size() == 0) return false;
        InstanceOfProcess instance=instanceOfProcesses.get(0);
        instance.setOver();
        instance.addStage(taskStage);
        instanceOfProcessRepository.save(instance);
        addUpdateTaskStage(instance,taskStage);
        return true;
    }

    public Employee findInstanceStarter(long companyID, String processID) {
        List<InstanceOfProcess> instanceOfProcesses=instanceOfProcessRepository
                .findByCompanyIDAndAndProcessID(companyID,processID);
        if(instanceOfProcesses.size() == 0) return null;
        InstanceOfProcess instance=instanceOfProcesses.get(0);
        long starterID=(long)instance.getStartPerson().get("ID");
        Employee starter=employeeRepository.findOne(starterID);
        return starter;
    }

    public List<InstanceOfProcess> getInstances(long companyID, long ID) {
        Company company=companyRepository.findOne(companyID);
        if(company == null) {
            return null;
        }
        Employee employee=employeeRepository.findOne(ID);
        Collection<InstanceOfProcess> instanceOfProcessList=employee.getInstanceOfProcesses();
        List<InstanceOfProcess> instanceOfProcesses=new ArrayList<>();
        instanceOfProcesses.addAll(instanceOfProcessList);
        Collections.sort(instanceOfProcesses, new Comparator<InstanceOfProcess>() {
            @Override
            public int compare(InstanceOfProcess o1, InstanceOfProcess o2) {
                long o1t=o1.getUpdateTime(), o2t=o2.getUpdateTime();
                if(o1t<o2t) return 1;
                else if (o1t>o2t)return -1;
                else return 0;
            }
        });
        return instanceOfProcesses;
    }

    public long getIDeployOfProcess() {
        Query query = new Query(Criteria.where("_id").is(0));
        Update update = new Update().inc("IDeployOfProcess", 1);
        IdManager m=mongoTemplate.findAndModify(query, update, IdManager.class);
        return m.getIDeployOfProcess();
    }

    private long getIChat() {
        Query query = new Query(Criteria.where("_id").is(0));
        Update update = new Update().inc("IChat", 1);
        IdManager m=mongoTemplate.findAndModify(query, update, IdManager.class);
        return m.getIChat();
    }

    private long getICompany() {
        Query query = new Query(Criteria.where("_id").is(0));
        Update update = new Update().inc("ICompany", 1);
        IdManager m=mongoTemplate.findAndModify(query, update, IdManager.class);
        return m.getICompany();
    }

    private long getIEmployee() {
        Query query = new Query(Criteria.where("_id").is(0));
        Update update = new Update().inc("IEmployee", 1);
        IdManager m=mongoTemplate.findAndModify(query, update, IdManager.class);
        System.out.println("em:"+m.getIEmployee()+"|"+employeeRepository.findAll().size());
        return m.getIEmployee();
    }

    private long getIInstanceOfProcess() {
        Query query = new Query(Criteria.where("_id").is(0));
        Update update = new Update().inc("IInstanceOfProcess", 1);
        IdManager m=mongoTemplate.findAndModify(query, update, IdManager.class);
        return m.getIInstanceOfProcess();
    }

    private long getINotification() {
        Query query = new Query(Criteria.where("_id").is(0));
        Update update = new Update().inc("INotification", 1);
        IdManager m=mongoTemplate.findAndModify(query, update, IdManager.class);
        return m.getINotification();
    }

    private long getISection() {
        Query query = new Query(Criteria.where("_id").is(0));
        Update update = new Update().inc("ISection", 1);
        IdManager m=mongoTemplate.findAndModify(query, update, IdManager.class);
        return m.getISection();
    }

    private long getITaskStage() {
        Query query = new Query(Criteria.where("_id").is(0));
        Update update = new Update().inc("ITaskStage", 1);
        IdManager m=mongoTemplate.findAndModify(query, update, IdManager.class);
        return m.getITaskStage();
    }

    private List<Notification> basicNoteGet(long id, int type) {
        Employee employee=employeeRepository.findOne(id);
        if(employee == null) {
            System.out.println("employee null");
            return null;
        }
        Collection<Notification> notifications;
        if(type == 0) {
            notifications=employee.getNotificationsSent();
        } else if(type == 1) {
            notifications=employee.getNotificationsRcvdUnread();
        } else {
            notifications=employee.getNotificationsRcvdRead();
        }
        if(notifications == null) {
            System.out.println("notifications null");
            return null;
        }
        List<Notification> notificationList=new ArrayList<>();
        notificationList.addAll(notifications);
        Collections.sort(notificationList, new Comparator<Notification>() {
            @Override
            public int compare(Notification o1, Notification o2) {
                if(o1.getSentTime()<o2.getSentTime()) return 1;
                else if(o1.getSentTime()>o2.getSentTime()) return -1;
                else return 0;
            }
        });
        return notificationList;
    }

    public List<Notification> getSent(long id) {
        return basicNoteGet(id,0);
    }

    public List<Notification> getUnread(long id) {
        return basicNoteGet(id,1);
    }

    public List<Notification> getRead(long id) {
        return basicNoteGet(id,2);
    }

    public Employee findEmployeeById(long id) {
        return employeeRepository.findOne(id);
    }

    public Notification readNotification(long notificationID, long id) {
        Employee employee=employeeRepository.findOne(id);
        Notification notification=notificationRepository.findOne(notificationID);
        employee.readNotification(notification);
        addUpdateNotificationsRcvdRead(employee,notification);
        delUpdateNotificationsRcvdUnread(employee,notification);
        return notification;
    }

    public JSONObject logicDelReadNotification(long notificationID, long id) {
        JSONObject result=new JSONObject();
        Employee employee=employeeRepository.findOne(id);
        Notification notification=notificationRepository.findOne(notificationID);
        employee.delNotification(notification);
        delUpdateNotificationsRcvdRead(employee,notification);
        result.put("info","已读通知删除成功");
        return result;
    }

    public JSONObject logicDelSentNotification(long notificationID, long id) {
        JSONObject result=new JSONObject();
        Employee employee=employeeRepository.findOne(id);
        Notification notification=notificationRepository.findOne(notificationID);
        employee.delNotification(notification);
        delUpdateNotificationsSent(employee,notification);
        result.put("info","已发送通知删除成功");
        return result;
    }

    public JSONObject addNewSection(long companyID, long sectionID, String name, String note) {
        JSONObject result=new JSONObject();
        Company company=companyRepository.findOne(companyID);
        if(company == null) {
            result.put("info","当前公司id不存在");
            return result;
        }
        if(sectionID == -1) {
            Section section=new Section(getISection(),companyID,sectionID,name,note);
            company.setHeadSec(section);
            companyRepository.save(company);
            sectionRepository.save(section);
            result.put("info","添加本公司第一个部门成功");
            return result;
        }
        Section parrentSection=sectionRepository.findOne(sectionID);
        if(parrentSection == null) {
            result.put("info","当前部门id不存在");
            return result;
        }
        if(parrentSection.getCompanyID() != companyID) {
            result.put("info","当前部门不属于此公司");
            return result;
        }
        Section section=new Section(getISection(),companyID,sectionID,name,note);
        parrentSection.addChildSec(section);
        sectionRepository.save(section);
        addUpdateChildrenSecs(parrentSection,section);
        result.put("info","部门添加成功");
        result.put("sectionID",section.getID());
        return result;
    }

    public JSONObject delSection(long companyID, long sectionID) {
        JSONObject result=new JSONObject();
        Company company=companyRepository.findOne(companyID);
        if(company == null) {
            result.put("info","删除失败,当前公司id不存在");
            return result;
        }
        Section section=sectionRepository.findOne(sectionID);
        if(section == null) {
            result.put("info",sectionID+": 当前部门id不存在");
            return result;
        }
        if(section.getParrentSecID() != -1) {
            Section parrentSec=sectionRepository.findOne(section.getParrentSecID());
            if(parrentSec == null) {
                result.put("info",section.getParrentSecID()+": 当前部门id不存在");
                return result;
            } else {
                parrentSec.deleteChildSec(section);
                delUpdateChildrenSecs(parrentSec,section);
            }
        } else {
            company.setHeadSec(null);
            companyRepository.save(company);
        }
        if(section.getCompanyID() != companyID) {
            result.put("info","删除失败,当前公司不存在此部门id");
            return result;
        }
        sectionRepository.delete(sectionID);
        result.put("info","删除成功");
        return result;
    }

    public JSONObject modifySectionData(long companyID, long sectionID, String description, String name, Long leaderID) {
        String info="";
        JSONObject result=new JSONObject();
        Company company=companyRepository.findOne(companyID);
        if(company == null) {
            result.put("info","当前公司id不存在");
            return result;
        }
        Section section=sectionRepository.findOne(sectionID);
        if(section == null) {
            result.put("info",sectionID+": 当前部门id不存在");
            return result;
        }
        if(name != null) {
            section.setName(name);
            info+="部门名称修改成功|";
        }
        if(description != null) {
            section.setNote(description);
            info+="部门描述修改成功|";
        }
        if(leaderID != null) {
            Employee newLeader=employeeRepository.findOne(leaderID);
            if(newLeader == null) {
                info+="部长设置失败,当前不存在此用户";
                sectionRepository.save(section);
                result.put("info",info);
                return result;
            } else {
                Employee oldLeader=section.getLeader();
                if(oldLeader != null) {
                    oldLeader.setLeader(false);
                }
                newLeader.setLeader(true);
                section.setLeaderID(newLeader);
                info+="新部长设置成功";//TODO employee belong to section?
            }
        }
        sectionRepository.save(section);
        if(info == "") {
            result.put("info","没有任何信息被修改");
        }
        result.put("info",info);
        return result;
    }

    public JSONObject groupChatGenerate(long companyID, long sectionID, long ID, List<Long> groups) {
        JSONObject result=new JSONObject();
        List<Section> sections=new ArrayList<>();
        String mark="";
        for(Long id:groups) {
            Section section=sectionRepository.findOne(id);
            sections.add(section);
            if(mark.length()<16) {
                for(Employee member:section.getMembers()) {
                    mark+=member.getName()+"、";
                    if(mark.length()>16) break;
                }
            }
        }
        mark=mark.substring(0,mark.length()-1);
        result.put("name",mark);
        Employee owner=employeeRepository.findOne(ID);
        Chat chat=new Chat(getIChat(),companyID,mark,System.currentTimeMillis());
        chat.setTrdPartyID((long) -1);
        result.put("chatID",-1);
        CreateGroupResult createGroupResult;
        try {
            createGroupResult=jMessageClient.createGroup(owner.getName(),mark,"none",owner.getName());
            if(createGroupResult.isResultOK()) {
                Long Gid=createGroupResult.getGid();
                chat.setTrdPartyID(Gid);
                result.put("chatID",Gid);
                ArrayList<String> addMembers=new ArrayList<>();
                for(Section section:sections) {
                    section.addgroupChat(chat);
                    addUpdateRelatedGroupChats(section,chat);
                    Collection<Employee> members=section.getMembers();
                    for(Employee member:members) {
                        addMembers.add(member.getName());
                        System.out.println(member.getName());
                    }
                }
                addMembers.remove(owner.getName());
                String[] addList=new String[addMembers.size()];
                jMessageClient.addOrRemoveMembers(Gid,addMembers.toArray(addList),null);
                chatRepository.save(chat);
            }
        } catch (APIConnectionException e) {
            System.out.println("Connection error. Should retry later. ");
        } catch (APIRequestException | NullPointerException e) {
            System.out.println("Error Message: " + e.getMessage());
        }
        return result;
    }

    public void testChat() {
        CreateGroupResult createGroupResult=null;
        try {
            GroupListResult groupListResult=jMessageClient.getGroupListByAppkey(0,100);
            List<GroupInfoResult> groupInfoResultList=groupListResult.getGroups();
            for(GroupInfoResult infoResult:groupInfoResultList) {
                jMessageClient.deleteGroup(infoResult.getGid());
            }

            List<RegisterInfo> users = new ArrayList<>();

            RegisterInfo user = RegisterInfo.newBuilder()
                    .setUsername("caocun")
                    .setPassword("60ed7ebb60f7320349843364a64ea6bb")
                    .build();
            RegisterInfo user1 = RegisterInfo.newBuilder()
                    .setUsername("adminfdafdaf")
                    .setPassword("21232f297a57a5a743894a0e4a801fc3")
                    .build();
            RegisterInfo user2 = RegisterInfo.newBuilder()
                    .setUsername("llllll")
                    .setPassword("21232f297a57a5a743894a0e4a801fc3")
                    .build();
            users.add(user);
            users.add(user1);
            users.add(user2);
            RegisterInfo[] regUsers = new RegisterInfo[users.size()];
            String res = jMessageClient.registerUsers(users.toArray(regUsers));
            System.out.println(res);

            UserInfoResult res1 = jMessageClient.getUserInfo("caocun");
            System.out.println(res1.getUsername()+" "+res1.getAppkey());

            UserStateResult result = jMessageClient.getUserState("caocun");
            System.out.println(result.getLogin()+" "+result.getOnline());

            UserListResult listResult=jMessageClient.getUserList(0,20);
            System.out.println("user total:"+listResult.getTotal());

            jMessageClient.deleteUser("caocun");
            System.out.println("delete:"+res1.getUsername()+" "+res1.getAppkey());

            user = RegisterInfo.newBuilder()
                    .setUsername("caocun")
                    .setPassword("60ed7ebb60f7320349843364a64ea6bb")
                    .build();
            users.clear();
            users.add(user);
            regUsers = new RegisterInfo[users.size()];
            jMessageClient.registerUsers(users.toArray(regUsers));
            res1 = jMessageClient.getUserInfo("caocun");
            System.out.println(res1);

            jMessageClient.addFriends("caocun","adminfdafdaf","llllll","testuser");
            UserInfoResult[] userInfoArray=jMessageClient.getFriendsInfo("caocun");
            System.out.println("len 0:"+userInfoArray.length);
            jMessageClient.deleteFriends("caocun","adminfdafdaf");
            userInfoArray=jMessageClient.getFriendsInfo("caocun");
            System.out.println("len 1:"+userInfoArray.length);

            createGroupResult=jMessageClient.createGroup("caocun","testQun","none","adminfdafdaf","llllll");
            System.out.println(createGroupResult.getGid());

            ArrayList<String> add=new ArrayList<>();
            ArrayList<String> del=new ArrayList<>();
            add.add("admin");
            del.add("llllll");
            String[] addList=new String[add.size()];
            String[] delList=new String[del.size()];
            jMessageClient.addOrRemoveMembers(createGroupResult.getGid(),add.toArray(addList),del.toArray(delList));
            del.clear();
            del.add("caocun");
            delList=new String[del.size()];
            jMessageClient.addOrRemoveMembers(createGroupResult.getGid(),null,del.toArray(delList));
        } catch (APIConnectionException e) {
            System.out.println("Connection error. Should retry later. ");
        } catch (APIRequestException | NullPointerException e) {
            System.out.println("Error response from JPush server. Should review and fix it. ");
            System.out.println("Error Message: " + e.getMessage());
            try {
                jMessageClient.deleteUser("adminfdafdaf");
                jMessageClient.deleteUser("llllll");
                jMessageClient.deleteUser("caocun");
                if(createGroupResult != null) {
                    jMessageClient.deleteGroup(createGroupResult.getGid());
                }
            } catch (Exception eee) {
                System.out.println("end error");
            }
        }
    }

    public JSONObject sentTotification(long senderID, Collection<Long> rcvdSecsID, String title, String content) {
        JSONObject jsonObject=new JSONObject();
        Employee employee=activeUserById(senderID);
        if(employee == null) {
            jsonObject.put("info","发送用户不在线, 发送失败");
            return jsonObject;
        }
        Notification notification=new Notification(getINotification(), employee.getCompanyID(),
                senderID, content, employee.getName(),
                title, System.currentTimeMillis());
        for(long rcvdID:rcvdSecsID) {
            notification.addSec(rcvdID);
        }
        notificationRepository.insert(notification);
        //send
        employee.sendNotification(notification);
        String colName=new BasicMongoPersistentEntity<>(ClassTypeInformation.from(Notification.class)).getCollection();
        DBRef notificationRef=new DBRef(mongoTemplate.getDb(),colName,notification.getID());
        Query query=Query.query(Criteria.where("_id").is(employee.getID()));
        Update update=new Update();
        update.addToSet("notificationsSent",notificationRef);
        mongoTemplate.updateFirst(query,update,Employee.class);
        //receive
        int secTotal=0,rcvTotal=0;
        for(long secID:rcvdSecsID) {
            Section section=sectionRepository.findOne(secID);
            if(section != null) secTotal++;
            else continue;
            if(secID == employee.getSectionID()) {
                section.delMember(employee);
            }
            Collection<Employee> receivers=section.getMembers();
            if(secID == employee.getSectionID()) receivers.remove(employee);
            for(Employee receiver:receivers) {
                receiver.rcvdNotification(notification);
                query=Query.query(Criteria.where("_id").is(receiver.getID()));
                update=new Update();
                update.addToSet("notificationsRcvdUnread",notificationRef);
                WriteResult writeResult=mongoTemplate.updateFirst(query,update,Employee.class);
                //TODO writeResult error?
                System.out.println("id:"+employee.getID()+"|"+writeResult);
                rcvTotal++;
            }
            for(Employee receiver:receivers) {
                pushAlert(receiver.getName(),"您有一条新通知!");
            }
        }
        jsonObject.put("info","通知发送成功!总计发送给"+secTotal+"个部门的"+rcvTotal+"个人!");
        return jsonObject;
    }

    public void pushAlert(String target, String content) {
        PushPayload payload = PushPayload.newBuilder()
                .setPlatform(Platform.all())
                .setAudience(Audience.alias(target))
                .setNotification(cn.jpush.api.push.model.notification.Notification.alert(content))
                .build();
        try {
            PushResult result = jPushClient.sendPush(payload);
            System.out.println("Got result - " + result);
        } catch (APIConnectionException e) {
            System.out.println("Connection error, should retry later");
        } catch (APIRequestException e) {
            System.out.println("Error Message: " + e.getErrorMessage());
        }
    }

    public void addUpdateInstanceOfProcess(Employee employee, InstanceOfProcess instanceOfProcess) {
        employee.addInstanceWorking(instanceOfProcess);
        String colName=new BasicMongoPersistentEntity<>(ClassTypeInformation.from(InstanceOfProcess.class)).getCollection();
        addCollectionDataBasic(Employee.class,employee.getID(),colName,instanceOfProcess.getID(),"instanceOfProcesses");
    }

    private void addUpdateNotificationsRcvdRead(Employee employee, Notification notification) {
        String colName=new BasicMongoPersistentEntity<>(ClassTypeInformation.from(Notification.class)).getCollection();
        addCollectionDataBasic(Employee.class,employee.getID(),colName,notification.getID(),"notificationsRcvdRead");
    }

    private void addUpdateNotificationsRcvdUnread(Employee employee, Notification notification) {
        String colName=new BasicMongoPersistentEntity<>(ClassTypeInformation.from(Notification.class)).getCollection();
        addCollectionDataBasic(Employee.class,employee.getID(),colName,notification.getID(),"notificationsRcvdUnread");
    }

    private void addUpdateNotificationsSent(Employee employee, Notification notification) {
        String colName=new BasicMongoPersistentEntity<>(ClassTypeInformation.from(Notification.class)).getCollection();
        addCollectionDataBasic(Employee.class,employee.getID(),colName,notification.getID(),"notificationsSent");
    }

    private void addUpdateChat(Employee employee, Chat chat) {
        String colName=new BasicMongoPersistentEntity<>(ClassTypeInformation.from(Chat.class)).getCollection();
        addCollectionDataBasic(Employee.class,employee.getID(),colName,chat.getID(),"groupChats");
    }

    private void addUpdateMembers(Section section, Employee employee) {
        String colName=new BasicMongoPersistentEntity<>(ClassTypeInformation.from(Employee.class)).getCollection();
        addCollectionDataBasic(Section.class,section.getID(),colName,employee.getID(),"members");
    }

    private void addUpdateChildrenSecs(Section section, Section child) {
        String colName=new BasicMongoPersistentEntity<>(ClassTypeInformation.from(Section.class)).getCollection();
        addCollectionDataBasic(Section.class,section.getID(),colName,child.getID(),"childrenSections");
    }

    private void addUpdateRelatedGroupChats(Section section, Chat chat) {
        String colName=new BasicMongoPersistentEntity<>(ClassTypeInformation.from(Chat.class)).getCollection();
        addCollectionDataBasic(Section.class,section.getID(),colName,chat.getID(),"relatedGroupChats");
    }

    private void addUpdateDeployOfProcess(Company company, DeployOfProcess deployOfProcess) {
        String colName=new BasicMongoPersistentEntity<>(ClassTypeInformation.from(DeployOfProcess.class)).getCollection();
        addCollectionDataBasic(Company.class,company.getID(),colName,deployOfProcess.getID(),"deployOfProcesses");
    }

    private void addUpdateTaskStage(InstanceOfProcess instanceOfProcess, TaskStage taskStage) {
        String colName=new BasicMongoPersistentEntity<>(ClassTypeInformation.from(TaskStage.class)).getCollection();
        addCollectionDataBasic(InstanceOfProcess.class,instanceOfProcess.getID(),colName,taskStage.getID(),"stages");
    }

    private void addCollectionDataBasic(Class mainType, long mainID, String colName, long dataID, String columnName) {
        DBRef dbRef=new DBRef(mongoTemplate.getDb(),colName,dataID);
        Query query=Query.query(Criteria.where("_id").is(mainID));
        Update update=new Update();
        update.addToSet(columnName,dbRef);
        mongoTemplate.updateFirst(query,update,mainType);
    }

    private void delUpdateNotificationsRcvdUnread(Employee employee, Notification notification) {
        delCollectionDataBasic(Employee.class,employee.getID(),notification.getID(),"notificationsRcvdUnread");
    }

    private void delUpdateNotificationsRcvdRead(Employee employee, Notification notification) {
        delCollectionDataBasic(Employee.class,employee.getID(),notification.getID(),"notificationsRcvdRead");
    }

    private void delUpdateNotificationsSent(Employee employee, Notification notification) {
        delCollectionDataBasic(Employee.class,employee.getID(),notification.getID(),"notificationsSent");
    }

    private void delUpdateChat(Employee employee, Notification notification) {
        delCollectionDataBasic(Employee.class,employee.getID(),notification.getID(),"groupChats");
    }

    private void delUpdateMembers(Section section, Employee employee) {
        delCollectionDataBasic(Section.class,section.getID(),employee.getID(),"members");
    }

    private void delUpdateChildrenSecs(Section section, Section child) {
        delCollectionDataBasic(Section.class,section.getID(),child.getID(),"childrenSections");
    }

    private void delUpdateRelatedGroupChats(Section section, Chat chat) {
        delCollectionDataBasic(Section.class,section.getID(),chat.getID(),"relatedGroupChats");
    }

    private void delUpdateDeployOfProcess(Company company, DeployOfProcess deployOfProcess) {
        delCollectionDataBasic(Company.class,company.getID(),deployOfProcess.getID(),"deployOfProcesses");
    }

    private void delCollectionDataBasic(Class mainType, long mainID, long dataID, String columnName)  {
        Query query=Query.query(Criteria.where("_id").is(mainID).and(columnName+".$id").is(dataID));
        Update update=new Update();
        update.unset(columnName+".$");
        mongoTemplate.updateFirst(query,update,mainType);
        query=Query.query(Criteria.where("_id").is(mainID));
        update=new Update();
        update.pull(columnName,null);
        mongoTemplate.updateFirst(query,update,mainType);
    }

    public Employee activeUserById(long id) {
        return userActive.get(id);
    }

    public Collection<Employee> employeesCompany(long companyID, Pageable pageable) {
        if(pageable != null) {
            return employeeRepository.findByCompanyID(companyID,pageable);
        } else {
            return employeeRepository.findByCompanyID(companyID);
        }
    }

    public Collection<Employee> employeesSection(long companyID, long sectionID, Pageable pageable) {
        if(pageable != null) {
            return employeeRepository.findByCompanyIDAndSectionID(companyID,sectionID,pageable);
        } else {
            return employeeRepository.findByCompanyIDAndSectionID(companyID, sectionID);
        }
    }

    public Section findSecByID(long companyID, long sectionID) {
        Company company=companyRepository.findOne(companyID);
        if(company == null) return null;
        Section section=sectionRepository.findOne(sectionID);
        if(section == null || section.getCompanyID() != companyID) return null;
        return sectionRepository.findOne(sectionID);
    }

    public Company findComById(long id) {
        return companyRepository.findOne(id);
    }

    public JSONObject findLoginEmployee(String name, String password, HttpServletRequest httpServletRequest) {
        JSONObject jsonObject=new JSONObject();
        List<Employee> employees=employeeRepository.findByName(name);
        if(employees.size() == 0) {
            jsonObject.put("status",false);
            jsonObject.put("info","此公司当前不存在该用户");
            return jsonObject;
        }
        Employee employee=employees.get(0);
        if(!employee.getPassword().equals(password)) {
            jsonObject.put("status",false);
            jsonObject.put("info","用户名或密码错误");
        } else {
            if(httpServletRequest.getHeader("Platform") == "web") {
                if(!employee.isLeader()) {
                    jsonObject.put("info","您不是部长或管理员, 无法登陆web端");
                    return jsonObject;
                }
            } else if(httpServletRequest.getHeader("Platform") == "app") {
                if(employee.getSectionID() == -1) {
                    jsonObject.put("info","管理员无法登陆app端");
                    return jsonObject;
                }
            }
            if(!employee.isActive()) {
                jsonObject.put("info","您的账号已被禁用，请联系管理员");
                return jsonObject;
            }
            HttpSession httpSession=httpServletRequest.getSession();
            httpSession.setAttribute("user",employee.getID());
            userActive.put(employee.getID(),employee);
            sessionActive.put(employee.getID(),httpSession);
            System.out.println("employee: "+employee.hashCode()+" "+activeUserById(employee.getID()).hashCode());
            jsonObject.put("status",true);
            jsonObject.put("info","登陆成功");
            jsonObject.put("companyID",employee.getCompanyID());
            jsonObject.put("ID",employee.getID());
            jsonObject.put("sectionID",employee.getSectionID());
            jsonObject.put("avatar",employee.getAvatar());
        }
        return jsonObject;
    }

    public JSONObject logout(HttpServletRequest httpServletRequest) {
        HttpSession httpSession=httpServletRequest.getSession();
        long id=(long)httpSession.getAttribute("user");
        Employee employee=userActive.get(id);
        String name=employee.getName();
        httpSession.invalidate();
        sessionRepository.delete(httpSession.getId());
        userActive.remove(id);
        sessionActive.remove(id);
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("info",name+":退出成功！");
        logStatusCheck();
        return jsonObject;
    }

    private void logStatusCheck() {
        Collection<Employee> activeEmployees=userActive.values();
        System.out.println("---------------");
        for(Employee employee:activeEmployees) {
            System.out.println("id:"+employee.getID()+"|name:"+employee.getName());
        }
        System.out.println("---------------");
    }
}
