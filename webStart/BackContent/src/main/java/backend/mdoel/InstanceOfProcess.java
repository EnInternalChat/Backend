package backend.mdoel;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.*;

/**
 * Created by lenovo on 2017/5/31.
 */
@Document
public class InstanceOfProcess {
    @Id
    private long ID;
    private long companyID;
    private String processID;
    private String processName;
    private Map<String, Object> startPerson;
    private boolean over;
    @DBRef
    private Collection<TaskStage> stages;

    public InstanceOfProcess() {
        startPerson=new HashMap<>();
        stages=new ArrayList<>();
        over=false;
    }

    public InstanceOfProcess(long ID, long companyID, String processID, String processName, Employee starter) {
        this();
        this.ID=ID;
        this.companyID=companyID;
        this.processID = processID;
        this.processName = processName;
        startPerson.put("ID", starter.getID());
        startPerson.put("name", starter.getName());
    }

    public boolean addStage(TaskStage taskStage) {
        stages.add(taskStage);
        return true;
    }

    public long getID() {
        return ID;
    }
}
