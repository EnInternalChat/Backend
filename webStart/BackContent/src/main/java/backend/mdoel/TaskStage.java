package backend.mdoel;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lenovo on 2017/5/31.
 */
@Document
public class TaskStage {
    @Id
    private long ID;
    private String activityID;
    private String title;
    private String content;
    private String processID;
    private long startTime;
    private long finishTime;
    private Map<String,Object> personData;
    private Collection<Map<String,String>> choices;

    public TaskStage() {
        personData=new HashMap<>();
        choices=new ArrayList<>();
    }

    public TaskStage(long ID, String activityID, String title, String processID, long startTime) {
        this();
        this.ID = ID;
        this.activityID = activityID;
        this.title = title;
        this.processID = processID;
        this.startTime = startTime;
    }

    public TaskStage(long ID, String activityID, String processID,
                     long startTime, String title, Employee person,
                     Collection<Map<String, String>> choices) {
        this();
        this.ID=ID;
        this.activityID = activityID;
        this.processID=processID;
        this.startTime = startTime;
        this.title=title;
        this.choices = choices;
        personData.put("ID",person.getID());
        personData.put("name",person.getName());
    }

    public Map<String, Object> getPersonData() {
        return personData;
    }

    public long getID() {
        return ID;
    }

    public long getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(long finishTime) {
        this.finishTime = finishTime;
    }

    public String getActivityID() {
        return activityID;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getStartTime() {
        return startTime;
    }

    public String getProcessID() {
        return processID;
    }

    public Collection<Map<String, String>> getChoices() {
        return choices;
    }

    public void setPersonData(Employee person) {
        personData.put("ID",person.getID());
        personData.put("name",person.getName());
    }
}
