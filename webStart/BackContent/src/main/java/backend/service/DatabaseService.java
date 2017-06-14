package backend.service;

import backend.mdoel.*;
import backend.repository.*;
import cn.jiguang.common.ClientConfig;
import cn.jiguang.common.resp.APIConnectionException;
import cn.jiguang.common.resp.APIRequestException;
import cn.jpush.api.JPushClient;
import cn.jpush.api.push.PushResult;
import cn.jpush.api.push.model.Platform;
import cn.jpush.api.push.model.PushPayload;
import cn.jpush.api.push.model.audience.Audience;
import com.mongodb.DBRef;
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
        idManager = idManagerRepository.findOne(0);
        if(idManager == null) {
            idManager =new IdManager(0,0,0,0,0,0,0,0);
            idManagerRepository.save(idManager);
            //TODO fix all id bug
        }
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

    public void sendAlertNtf() {
        PushPayload payload = PushPayload.newBuilder()
                .setPlatform(Platform.all())
                .setAudience(Audience.alias("testuser"))
                .setNotification(cn.jpush.api.push.model.notification.Notification.alert("您有一条消息来自服务器后台"))
                .build();
        try {
            PushResult result = jPushClient.sendPush(payload);
            System.out.println("Got result - " + result);
        } catch (APIConnectionException e) {
            // Connection error, should retry later
            System.out.println("Connection error, should retry later");
        } catch (APIRequestException e) {
            // Should review the error, and fix the request
            System.out.println("Should review the error, and fix the request");
            System.out.println("HTTP Status: " + e.getStatus());
            System.out.println("Error Code: " + e.getErrorCode());
            System.out.println("Error Message: " + e.getErrorMessage());
        }
    }

    public void updateEmployeeCollectionData(Employee employee, Chat chat) {
        //TODO
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

    public Map<String,Object> colEmployeeData(long companyID) {
        Map<String,Object> result=new HashMap<>();
        List<Employee> employees=employeeRepository.findByCompanyID(companyID);
        result.put("total",employees.size());
        result.put("employees",employees);
        return result;//TODO different data
    }

    public Map<String, Object> colSecEmployeeData(long companyID, long sectionID) {
        Map<String,Object> result=new HashMap<>();
        List<Employee> employees=employeeRepository.findBySectionIDAndCompanyID(companyID,sectionID);
        result.put("total",employees.size());
        result.put("employees",employees);//TODO frontend different data
        return result;
    }

    public boolean addNewProcess(String token, String name, String path) {
        long timestamp=System.currentTimeMillis();
        long companyID=0;
        //TODO getid
        DeployOfProcess processDeploy =new DeployOfProcess(getIDeployOfProcess(),companyID,name,path,timestamp,timestamp,0);
        deployOfProcessRepository.save(processDeploy);
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
