package backend.mdoel;

import backend.serial.DeployOfProcessSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by lenovo on 2017/5/14.
 */

@Document
@JsonSerialize(using = DeployOfProcessSerializer.class)
public class DeployOfProcess {
    @Id
    private long ID;
    private long companyID;
    private String name;
    private long uploadTime;
    private long updateTime;
    private long count;

    public DeployOfProcess() {
    }

    public DeployOfProcess(long ID,long companyID, String name, long uploadTime, long updateTime, long count) {
        this();
        this.ID=ID;
        this.companyID = companyID;
        this.name = name;
        this.uploadTime = uploadTime;
        this.updateTime = updateTime;
        this.count = count;
    }

    public long getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(long uploadTime) {
        this.uploadTime = uploadTime;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public long getID() {
        return ID;
    }

    public void setID(long ID) {
        this.ID = ID;
    }

    public long getCompanyID() {
        return companyID;
    }

    public void setCompanyID(long companyID) {
        this.companyID = companyID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
