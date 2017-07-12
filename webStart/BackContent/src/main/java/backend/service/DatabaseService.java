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
    private final Map<Long,HttpSession> sessionActive;
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
            result.put("id",employee.getID());
        } catch (APIConnectionException e) {
            result.put("info","员工添加成功,极光注册失败");
            System.out.println("Connection error. Should retry later. ");
        } catch (APIRequestException | NullPointerException e) {
            result.put("info","员工添加成功,极光注册失败");
            System.out.println("Error Message: " + e.getMessage());
        }
        return result;
    }

    public boolean saveDeployOfProcess(DeployOfProcess deployOfProcess) {
        deployOfProcessRepository.save(deployOfProcess);
        Company company=companyRepository.findOne(deployOfProcess.getCompanyID());
        company.addDeployOfProcess(deployOfProcess);
        addUpdateDeployOfProcess(company,deployOfProcess);
        return true;
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

    public Long groupChatGenerate(long companyID, long sectionID, long ID, List<Long> groups) {
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
        Employee owner=employeeRepository.findOne(ID);
        Chat chat=new Chat(getIChat(),companyID,mark,System.currentTimeMillis());
        chat.setTrdPartyID((long) -1);
        CreateGroupResult createGroupResult;
        try {
            createGroupResult=jMessageClient.createGroup(owner.getName(),mark,"none",owner.getName());
            if(createGroupResult.isResultOK()) {
                Long Gid=createGroupResult.getGid();
                chat.setTrdPartyID(Gid);
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

    public void addUpdateInstanceOfProcess(Employee employee, InstanceOfProcess instanceOfProcess) {
        employee.addTask(instanceOfProcess);
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
