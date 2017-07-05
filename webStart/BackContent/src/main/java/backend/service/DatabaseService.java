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
    private final IdManagerRepository idManagerRepository;

    private final Map<Long,Employee> userActive;
    private IdManager idManager;

    private static String APP_KEY="f784911007eb5e69ef4a773f";
    private static String MASTER_SECRET="d4c4f6da868458e48f4de0e8";
    private JPushClient jPushClient;
    private JMessageClient jMessageClient;

    @Autowired
    public DatabaseService(EmployeeRepository employeeRepository, NotificationRepository notificationRepository,
                           DeployOfProcessRepository deployOfProcessRepository, ChatRepository chatRepository,
                           CompanyRepository companyRepository, SectionRepository sectionRepository,
                           SessionRepository sessionRepository, TaskStageRepository taskStageRepository,
                           InstanceOfProcessRepository instanceOfProcessRepository, IdManagerRepository idManagerRepository,
                           MongoTemplate mongoTemplate) {
        userActive=new HashMap<>();
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
        this.idManagerRepository = idManagerRepository;
        jPushClient=new JPushClient(MASTER_SECRET,APP_KEY,null, ClientConfig.getInstance());
        jMessageClient=new JMessageClient(APP_KEY,MASTER_SECRET);
        idManager = idManagerRepository.findOne(0);
        int ICompany=companyRepository.findAll().size();
        int IEmployee=employeeRepository.findAll().size();
        int INotification=notificationRepository.findAll().size();
        int ISection=sectionRepository.findAll().size();
        idManager=idManagerRepository.findOne(0);
        System.out.println(ICompany+" "+IEmployee+" "+INotification+" "+ISection);
        if(idManager == null) {
            idManager =new IdManager(0,0,ICompany,IEmployee,0,INotification,ISection,0);
            idManagerRepository.save(idManager);
            //TODO fix all id bug
        }
        idManager.setICompany(ICompany);
        idManager.setIEmployee(IEmployee);
        idManager.setINotification(INotification);
        idManager.setISection(ISection);
        idManagerRepository.save(idManager);
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

    public JSONObject updateEmployeePersonal(Long companyID, Long sectionID, long id,
                                             String newPwd, String email1, String email2,
                                             String phone1, String phone2) {
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
            jsonObject.put("info","修改对象: "+employee.getName()+".没有任何信息被修改");
        } else {
            jsonObject.put("info", "修改对象: "+employee.getName()+"."+info);
        }
        employeeRepository.save(employee);
        return jsonObject;
        //TODO update failed
    }
    public long getIDeployOfProcess() {
        long id= idManager.getIDeployOfProcess();
        idManagerRepository.save(idManager);
        return id;
    }

    private long getIChat() {
        long id= idManager.getIChat();
        idManagerRepository.save(idManager);
        return id;
    }

    private long getICompany() {
        long id= idManager.getICompany();
        idManagerRepository.save(idManager);
        return id;
    }

    private long getIEmployee() {
        long id= idManager.getIEmployee();
        idManagerRepository.save(idManager);
        return id;
    }

    private long getIInstanceOfProcess() {
        long id= idManager.getIInstanceOfProcess();
        idManagerRepository.save(idManager);
        return id;
    }

    private long getINotification() {
        long id= idManager.getINotification();
        idManagerRepository.save(idManager);
        return id;
    }

    private long getISection() {
        long id= idManager.getISection();
        idManagerRepository.save(idManager);
        return id;
    }

    private long getITaskStage() {
        long id= idManager.getITaskStage();
        idManagerRepository.save(idManager);
        return id;
    }

    private Collection<Notification> basicNoteGet(long id, int type) {
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
        return notifications;
    }

    public Collection<Notification> getSent(long id) {
        return basicNoteGet(id,0);
    }

    public Collection<Notification> getUnread(long id) {
        return basicNoteGet(id,1);
    }

    public Collection<Notification> getRead(long id) {
        return basicNoteGet(id,2);
    }

    public Employee findEmployeeById(long id) {
        return employeeRepository.findOne(id);
    }

    public String groupChatGenerate(long companyID, List<Long> groups) {
        Collections.sort(groups);
        String mark="";
        for(Long id:groups) {
            mark=mark+id+"+";
        }
        mark.substring(0,mark.length()-1);
        List<Chat> chats=chatRepository.findByMark(mark);
        Chat chat=null;
        if(chats.size() == 0) {
            chat=new Chat(getIChat(),companyID,mark);

            //TODO create;
            chatRepository.save(chat);
        } else {
            chat=chats.get(0);
        }
        return chat.getTrdPartyID();
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
                senderID, content, employee.getName(), title);
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
                //push
            }
            for(Employee receiver:receivers) {
                pushAlert(receiver.getName(),"您有一条新通知!");
            }
        }
        jsonObject.put("info","通知发送成功!总计发送给"+secTotal+"个部门的"+rcvTotal+"个人!");
        return jsonObject;
    }

    private void pushAlert(String target, String content) {
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

    public void updateEmployeeCollectionData(Employee employee, InstanceOfProcess instanceOfProcess) {
        employee.addTask(instanceOfProcess);
        String colName=new BasicMongoPersistentEntity<>(ClassTypeInformation.from(InstanceOfProcess.class)).getCollection();
        DBRef instanceOfProcessRef=new DBRef(mongoTemplate.getDb(),colName,instanceOfProcess.getID());
        Query query=Query.query(Criteria.where("_id").is(employee.getID()));
        Update update=new Update();
        update.addToSet("instanceOfProcesses",instanceOfProcessRef);
        mongoTemplate.updateFirst(query,update,Employee.class);
    }

    public InstanceOfProcess saveProcessInstance(String processKey, String processID, String processName, Employee starter) {
        InstanceOfProcess instanceOfProcess=new InstanceOfProcess(getIInstanceOfProcess(),processKey,processID,processName,starter);
        instanceOfProcessRepository.insert(instanceOfProcess);
        return instanceOfProcess;
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

    public boolean addNewProcess(String token, String name, String path) {
        long timestamp=System.currentTimeMillis();
        long companyID=0;
        //TODO getid and fixed
        //DeployOfProcess processDeploy =new DeployOfProcess(getIDeployOfProcess(),companyID,name,path,timestamp,timestamp,0);
        //deployOfProcessRepository.save(processDeploy);
        return true;
    }

    public void doWork() {
        ArrayList<String> test=new ArrayList<>();
        test.add("1111");
        test.add("e35564543");
        Employee employee=employeeRepository.findOne((long) 28);
        employee.setPassword("sssssssssss");
        employeeRepository.save(employee);
    }

    public Section findSecByID(long id) {
        return sectionRepository.findOne(id);
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
            HttpSession httpSession=httpServletRequest.getSession();
            httpSession.setAttribute("user",employee.getID());
            userActive.put(employee.getID(),employee);
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
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("info",name+":退出成功！");
        return jsonObject;
    }
}
