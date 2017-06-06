package backend.mdoel;

import backend.util.IdManager;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
        ID=IdManager.IdForInstanceOfProcess++;
        this.startPerson = new ArrayList<>();
        this.stages = new ArrayList<>();
        over=false;
    }

    public InstanceOfProcess(String processKey, String processID, String processName, Employee starter) {
        this();
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
