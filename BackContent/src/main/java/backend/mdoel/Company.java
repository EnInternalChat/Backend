package backend.mdoel;

import backend.serial.CompanySerializer;
import backend.util.IdManager;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by lenovo on 2017/5/14.
 */

@Document
@JsonSerialize(using = CompanySerializer.class)
public class Company {
    @Id
    private long ID;
    @DBRef
    private Section headSec;
    private String name;
    private String introduction;
    @DBRef
    private Collection<DeployOfProcess> deployOfProcesses;

    public Company() {
        ID=IdManager.IdForCompanty++;
        deployOfProcesses=new ArrayList<>();
    }

    public Section getHeadSec() {
        return headSec;
    }

    public boolean hasSection() {
        return headSec != null;
    }

    public void setHeadSec(Section headSec) {
        this.headSec = headSec;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public long getID() {
        return ID;
    }

    public String getName() {
        return name;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public void setName(String name) {
        this.name = name;
    }
}
