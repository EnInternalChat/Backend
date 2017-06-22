package backend.mdoel;

import backend.serial.EmployeeSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * Created by lenovo on 2017/5/7.
 */

@Document
@JsonSerialize(using = EmployeeSerializer.class)
public class Employee {
    @Id
    private long ID;
    private long companyID;
    private long sectionID;
    private int avatar;
    private String name;
    private String pwd;
    private String position;
    private Collection<String> phone;
    private Collection<String> email;
    @DBRef
    private Collection<Map<String,Object>> chats;
    @DBRef
    private Collection<Notification> notificationsSent;
    @DBRef
    private Collection<Notification> notificationsRcvdUnread;
    @DBRef
    private Collection<Notification> notificationsRcvdRead;
    @DBRef
    private Collection<InstanceOfProcess> instanceOfProcesses;
    private boolean gender;
    private boolean active;
    private boolean leader;

    public Employee() {
        phone=new ArrayList<>();
        email=new ArrayList<>();
        chats=new ArrayList<>();
        notificationsSent=new ArrayList<>();
        notificationsRcvdRead=new ArrayList<>();
        notificationsRcvdUnread=new ArrayList<>();
        instanceOfProcesses =new ArrayList<>();
    }

    //add new formal
    public Employee(long ID, long companyID, long sectionID, String name, String password, String position, boolean gender, String mail, String number) {
        this();
        this.ID = ID;
        this.companyID = companyID;
        this.sectionID = sectionID;
        this.name = name;
        this.pwd = password;
        this.position = position;
        this.gender = gender;
        active=true;
        leader=false;
        email.add(mail);
        phone.add(number);
    }

    public Employee(long ID) {
        this();
        this.ID = ID;
        avatar=2;
    }

    public boolean isLeader() {
        return leader;
    }

    public void setLeader(boolean leader) {
        this.leader = leader;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public long getCompanyID() {
        return companyID;
    }

    public void setCompanyID(long companyID) {
        this.companyID = companyID;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public long getSectionID() {
        return sectionID;
    }

    public int getAvatar() {
        return avatar;
    }

    public void setAvatar(int avatar) {
        this.avatar = avatar;
    }

    public boolean isGender() {
        return gender;
    }

    public void setGender(boolean gender) {
        this.gender = gender;
    }

    public Collection<String> getPhone() {
        return phone;
    }

    public void setPhone(Collection<String> phone) {
        this.phone = phone;
    }

    public Collection<String> getEmail() {
        return email;
    }

    public void setEmail(Collection<String> email) {
        this.email = email;
    }

    public long getID() {
        return ID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return pwd;
    }

    public void setPassword(String password) {
        this.pwd = password;
    }

    public Collection<Notification> getNotificationsSent() {
        return notificationsSent;
    }

    public Collection<Notification> getNotificationsRcvdUnread() {
        return notificationsRcvdUnread;
    }

    public Collection<Notification> getNotificationsRcvdRead() {
        return notificationsRcvdRead;
    }

    public boolean sendNotification(Notification notification) {
        if(!notificationsSent.contains(notification)) return false;
        notificationsSent.add(notification);
        return true;
    }

    public boolean rcvdNotification(Notification notification) {
        if(!notificationsRcvdUnread.contains(notification)) return false;
        notificationsRcvdUnread.add(notification);
        return true;
    }

    public Collection<InstanceOfProcess> getInstanceOfProcesses() {
        return instanceOfProcesses;
    }

    public Collection<InstanceOfProcess> getProcessDeploys() {
        return instanceOfProcesses;
    }

    public boolean addTask(InstanceOfProcess newProcessDeploy) {
        instanceOfProcesses.add(newProcessDeploy);
        System.out.println(instanceOfProcesses.size());
        return true;
    }

    private boolean basicUpdateContact(String new1, String new2, boolean type) {
        boolean update=false;
        if(type) {
            phone.clear();
        } else {
            email.clear();
        }
        if(new1 != null) {
            update=true;
            if(type) phone.add(new1);
            else email.add(new1);
        }
        if(new2 != null) {
            update=true;
            if(type) phone.add(new2);
            else email.add(new2);
        }
        return update;
    }
    public boolean updatePhone(String newPhone1, String newPhone2) {
        return basicUpdateContact(newPhone1,newPhone2,true);
    }

    public boolean updateMail(String newEmail1, String newEmail2) {
        return basicUpdateContact(newEmail1,newEmail2,false);
    }

    @Override
    public String toString() {
        String result="[ID:"+ID+"|comp:"+companyID+"|"
                +"secID:"+sectionID+"|name: "+name+"|password: "+pwd+"|pos:"+position+"|"
                +"emailSize:"+email.size()+"|phonesize:"+phone.size()+"|"
                +"sentNoteSize:"+notificationsSent.size()+"|unread:"+notificationsRcvdUnread.size()
                +"|read:"+notificationsRcvdRead.size()+"]";
        return result;
    }
}
