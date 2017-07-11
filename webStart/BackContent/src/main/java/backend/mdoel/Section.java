package backend.mdoel;

import backend.serial.SectionSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by lenovo on 2017/5/14.
 * database model,not for front end
 */

@Document
@JsonSerialize(using = SectionSerializer.class)
public class Section {
    @Id
    private long ID;
    private long companyID;
    private long parrentSecID;
    private String chatID;
    @DBRef
    private Employee leader;
    @DBRef
    private Collection<Employee> members;
    @DBRef
    private Collection<Section> childrenSections;
    @DBRef
    private Collection<Chat> relatedGroupChats;
    private String name;
    private String note;
    private String label;

    public Section() {
        members=new HashSet<>();
        childrenSections=new HashSet<>();
        relatedGroupChats=new HashSet<>();
    }

    //add new formal
    public Section(long ID, long companyID, long parrentSecID, String name, String note) {
        this();
        this.ID = ID;
        this.companyID = companyID;
        this.name = name;
        this.note=note;
        this.parrentSecID=parrentSecID;
    }

    public Section(long ID) {
        this();
        this.ID=ID;
        label="";
    }

    public String getLabel() {
        return label;
    }

    public Collection<Chat> getRelatedGroupChats() {
        return relatedGroupChats;
    }

    public boolean addgroupChat(Chat chat) {
        relatedGroupChats.add(chat);
        return true;
    }

    public long getParrentSecID() {
        return parrentSecID;
    }

    public String getChatID() {
        return chatID;
    }

    public void setChatID(String chatID) {
        this.chatID = chatID;
    }

    public Collection<Section> getChildrenSections() {
        return childrenSections;
    }

    public void setChildrenSectionsID(Set<Section> childrenSections) {
        this.childrenSections = childrenSections;
    }

    public long getCompanyID() {
        return companyID;
    }

    public void setCompanyID(long companyID) {
        this.companyID = companyID;
    }

    public void setID(long ID) {
        this.ID = ID;
    }

    public void setLeaderID(Employee leader) {
        this.leader = leader;
    }

//    public void setParrentSectionID(long parrentSectionID) {
//        this.parrentSectionID = parrentSectionID;
//    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public long getID() {
        return ID;
    }

    public Employee getLeader() {
        return leader;
    }

    public Collection<Employee> getMembers() {
        return members;
    }

    public boolean delMember(Employee employee) {
        members.remove(employee);
        return true;
    }
    public boolean addMember(Employee employee) {
        members.add(employee);
        return true;
    }

    public String getName() {
        return name;
    }

    public String getNote() {
        return note;
    }

    public boolean addChildSec(Section section) {
        childrenSections.add(section);
        return true;
    }

    public boolean deleteChildSec(Section section) {
        childrenSections.remove(section);
        return true;
    }
}
