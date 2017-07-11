package backend.mdoel;

import backend.serial.NotificationSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Collection;
import java.util.HashSet;

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
        rcvSecID=new HashSet<>();
    }

    public Collection<Long> getRcvSecID() {
        return rcvSecID;
    }

    public Notification(long ID, long companyID, long senderID, String content, String senderName, String title, long sentTime) {
        this();
        this.ID = ID;
        this.companyID = companyID;
        this.senderID = senderID;
        this.content = content;
        this.senderName = senderName;
        this.title = title;
        this.sentTime=sentTime;
    }

    public void addSec(long secID) {
        if(!rcvSecID.contains(secID)) {
            rcvSecID.add(secID);
        }
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
