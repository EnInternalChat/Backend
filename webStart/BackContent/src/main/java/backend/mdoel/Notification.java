package backend.mdoel;

import backend.serial.NotificationSerializer;
import backend.serial.SectionSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by lenovo on 2017/5/14.
 */

@Document
@JsonSerialize(using = NotificationSerializer.class)
public class Notification {
    @Id
    private long ID;
    private long companyID;
    private long senderID;
    private long sentTime;
    private String content;
    private String senderName;
    private String title;
    private Collection<Long> rcvSecID;

    public Notification() {
    }

    public Collection<Long> getRcvSecID() {
        return rcvSecID;
    }

    public Notification(long ID) {
        sentTime=System.currentTimeMillis();
        this.ID=ID;
        this.sentTime = sentTime;
        rcvSecID=new ArrayList<>();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getCompanyID() {
        return companyID;
    }

    public void setCompanyID(long companyID) {
        this.companyID = companyID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public void setSenderID(long senderID) {
        this.senderID = senderID;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public long getID() {
        return ID;
    }

    public long getSenderID() {
        return senderID;
    }

    public long getSentTime() {
        return sentTime;
    }

    public String getContent() {
        return content;
    }

    public String getSenderName() {
        return senderName;
    }
}
