package backend.mdoel;

import backend.util.IdManager;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by lenovo on 2017/5/7.
 */

@Document
public class Employee {
    @Id
    private long ID;
    private long companyID;
    @DBRef
    private Section section;
    private int avatar;
    private String name;
    private String password;
    private String position;
    private Collection<String> phone;
    private Collection<String> email;
    @DBRef
    private Collection<Chat> chats;
    private RoleType roleType;
    @DBRef
    private Collection<Notification> notifications;
    @DBRef
    private Collection<InstanceOfProcess> instanceOfProcesses;
    private boolean gender;
    private boolean active;

    public Employee() {
        ID=IdManager.IdForEmployee++;
        companyID=233;
        avatar=2;
        password=position=name="dfsgsdfhfgjdfj";
        phone=new ArrayList<>();
        email=new ArrayList<>();
        chats=new ArrayList<>();
        notifications=new ArrayList<>();
        instanceOfProcesses =new ArrayList<>();
    }

    public Employee(long ID) {
        this.ID = ID;
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
        return section.getID();
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
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Collection<Chat> getChats() {
        return chats;
    }

    public Collection<Notification> getNotifications() {
        return notifications;
    }

    public Collection<InstanceOfProcess> getProcessDeploys() {
        return instanceOfProcesses;
    }

    public boolean addChat(Chat newChat) {
        chats.add(newChat);
        return true;
    }

    public boolean addNotification(Notification newNotification) {
        notifications.add(newNotification);
        return true;
    }

    public boolean addTask(InstanceOfProcess newProcessDeploy) {
        instanceOfProcesses.add(newProcessDeploy);
        System.out.println(instanceOfProcesses.size());
        return true;
    }

    public boolean addPhone(String newPhone) {
        if(!phone.contains(newPhone)) return false;
        phone.add(newPhone);
        return true;
    }

    public boolean addMail(String newEmail) {
        if(!email.contains(newEmail)) return false;
        email.add(newEmail);
        return true;
    }
}
