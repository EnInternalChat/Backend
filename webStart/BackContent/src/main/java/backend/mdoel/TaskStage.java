package backend.mdoel;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * Created by lenovo on 2017/5/31.
 */
@Document
public class TaskStage {
    @Id
    private long ID;
    private String activityID;
    private String content;
    private long startTime;
    private long finishTime;
    @DBRef
    private Collection<Employee> person;
    private Collection<Map<String,String>> choices;

    public TaskStage() {
    }

    public TaskStage(long ID) {
        this.ID=ID;
        person=new ArrayList<>();
        choices=new ArrayList<>();
    }

    public TaskStage(long ID, String activityID, long startTime, Collection<Map<String, String>> choices) {
        this(ID);
        this.activityID = activityID;
        this.startTime = startTime;
        this.choices = choices;
    }
}
