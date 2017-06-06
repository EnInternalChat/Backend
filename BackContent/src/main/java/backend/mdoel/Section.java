package backend.mdoel;

import backend.serial.SectionSerializer;
import backend.util.IdManager;
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
    @DBRef
    private Employee leader;
    @DBRef
    private Collection<Employee> members;
    @DBRef
    private Collection<Section> childrenSections;
    private String name;
    private String note;

    public Section() {
        ID=IdManager.IdForSection++;
        members=new HashSet<>();
        childrenSections=new HashSet<>();
    }

    public Section(long companyID, Employee leader, String name, String note) {
        this();
        this.companyID = companyID;
        this.leader = leader;
        this.name = name;
        this.note = note;
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

//    public long getParrentSectionID() {
//        return parrentSectionID;
//    }

    public Collection<Employee> getMembers() {
        return members;
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
