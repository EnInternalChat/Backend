package backend.mdoel;

import backend.serial.InstanceOfProcessSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
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
@JsonSerialize(using = InstanceOfProcessSerializer.class)
public class InstanceOfProcess {
    @Id
    private long ID;
    private long companyID;
    private long updateTime;
    private long processDefID;
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
        updateTime=System.currentTimeMillis();
    }

    public InstanceOfProcess(long ID, long companyID, long processDefID, String processID, String processName, Employee starter) {
        this();
        this.ID=ID;
        this.companyID=companyID;
        this.processDefID=processDefID;
        this.processID = processID;
        this.processName = processName;
        startPerson.put("ID", starter.getID());
        startPerson.put("name", starter.getName());
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public long getProcessDefID() {
        return processDefID;
    }

    public void reNewUpdateTime() {
        updateTime=System.currentTimeMillis();
    }

    public boolean addStage(TaskStage taskStage) {
        stages.add(taskStage);
        return true;
    }

    public long getID() {
        return ID;
    }

    public long getCompanyID() {
        return companyID;
    }

    public String getProcessID() {
        return processID;
    }

    public String getProcessName() {
        return processName;
    }

    public Map<String, Object> getStartPerson() {
        return startPerson;
    }

    public void setOver() {
        this.over = true;
    }

    public boolean isOver() {
        return over;
    }

    public Collection<TaskStage> getStages() {
        return stages;
    }
}
