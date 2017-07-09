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
    private String processKey;
    private String processID;
    private String processName;
    private Collection<Map<String, Object>> startPerson;
    private boolean over;
    @DBRef
    private Collection<TaskStage> stages;

    public InstanceOfProcess() {
    }

    public InstanceOfProcess(long ID) {
        this.ID=ID;
        this.startPerson = new HashSet<>();
        this.stages = new HashSet<>();
        over=false;
    }

    public InstanceOfProcess(long ID, String processKey, String processID, String processName, Employee starter) {
        this(ID);
        this.processKey = processKey;
        this.processID = processID;
        this.processName = processName;
        HashMap<String,Object> starterMap=new HashMap<>();
        starterMap.put("ID",starter.getID());
        starterMap.put("name",starter.getName());
        startPerson.add(starterMap);
    }

    public boolean addStage(TaskStage taskStage) {
        stages.add(taskStage);
        return true;
    }

    public long getID() {
        return ID;
    }
}
