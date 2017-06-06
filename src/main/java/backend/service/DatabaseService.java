package backend.service;

import backend.mdoel.*;
import backend.repository.*;
import backend.util.IdManager;
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

    private final Map<Long,Employee> userActive;

    @Autowired
    public DatabaseService(EmployeeRepository employeeRepository, NotificationRepository notificationRepository,
                           DeployOfProcessRepository deployOfProcessRepository, ChatRepository chatRepository,
                           CompanyRepository companyRepository, SectionRepository sectionRepository,
                           SessionRepository sessionRepository, TaskStageRepository taskStageRepository,
                           InstanceOfProcessRepository instanceOfProcessRepository,
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
        IdManager.IdForChat=chatRepository.findAll().size();
        IdManager.IdForCompanty=companyRepository.findAll().size();
        IdManager.IdForInstanceOfProcess=instanceOfProcessRepository.findAll().size();
        IdManager.IdForDeployOfProcess=deployOfProcessRepository.findAll().size();
        IdManager.IdForEmployee=employeeRepository.findAll().size();
        IdManager.IdForTaskStage=taskStageRepository.findAll().size();
        IdManager.IdForNotification=notificationRepository.findAll().size();
        IdManager.IdForSection=sectionRepository.findAll().size();
    }

    public void updateEmployeeCollectionData(Employee employee, Chat chat) {
        //TODO
    }

    public void updateEmployeeCollectionData(Employee employee, Notification notification) {
        employee.addNotification(notification);
        String colName=new BasicMongoPersistentEntity<>(ClassTypeInformation.from(Notification.class)).getCollection();
        DBRef notificationRef=new DBRef(mongoTemplate.getDb(),colName,notification.getID());
        Query query=Query.query(Criteria.where("_id").is(employee.getID()));
        Update update=new Update();
        update.addToSet("notifications",notificationRef);
        mongoTemplate.updateFirst(query,update,Employee.class);
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

    public void saveProcessInstance(InstanceOfProcess instanceOfProcess) {
        instanceOfProcessRepository.insert(instanceOfProcess);
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
        DeployOfProcess processDeploy =new DeployOfProcess(companyID,name,path,timestamp,timestamp,0);
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

    public JSONObject findLoginEmployee(long companyId, String name, String password, HttpServletRequest httpServletRequest) {
        JSONObject jsonObject=new JSONObject();
        List<Employee> employees=employeeRepository.findByCompanyIDAndName(companyId, name);
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

    public void testNewStruc() {
        Company company=new Company();
        company.setName("google");
        company.setIntroduction("nihao");
        Employee employee=new Employee();
        employee.setName("dog");
        employee.setPassword("dog");
        employee.setCompanyID(1);
        Section section1=new Section(0,employee,"ass","note");
        Section section2=new Section(0,employee,"fsdf","note");
        Section section3=new Section(0,employee,"afsfs","note");
        Section section4=new Section(0,employee,"dsfsss","note");
        Section section5=new Section(0,employee,"aeyys","note");
        Section section6=new Section(0,employee,"asyqq","note");
        company.setHeadSec(section1);
        section1.addChildSec(section2);
        section1.addChildSec(section3);
        section2.addChildSec(section4);
        section3.addChildSec(section5);
        section3.addChildSec(section6);
        employeeRepository.insert(employee);
        employeeRepository.insert(new Employee());
        employeeRepository.insert(new Employee());
        employeeRepository.insert(new Employee());
        employeeRepository.insert(new Employee());
        employeeRepository.insert(new Employee());
        employeeRepository.insert(new Employee());
        employeeRepository.insert(new Employee());
        employeeRepository.insert(new Employee());
        employee.addTask(new InstanceOfProcess());
        employee.addChat(new Chat());
        employee.addMail("ss");
        employee.addPhone("s");
        employee.addNotification(new Notification());
        employeeRepository.save(employee);
        sectionRepository.insert(section2);
        sectionRepository.insert(section1);
        sectionRepository.insert(section3);
        sectionRepository.insert(section5);
        sectionRepository.insert(section6);
        sectionRepository.insert(section4);
        companyRepository.save(company);
        instanceOfProcessRepository.save(new InstanceOfProcess());
        deployOfProcessRepository.save(new DeployOfProcess());
        taskStageRepository.save(new TaskStage());
        Company result=companyRepository.findOne((long) 0);
        System.out.println(result.getHeadSec().getChildrenSections().size());
    }
}
