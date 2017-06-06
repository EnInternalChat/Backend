package backend.mdoel;

import backend.util.IdManager;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by lenovo on 2017/5/14.
 */

@Document
public class DeployOfProcess {
    @Id
    private long ID;
    private long companyID;
    private String name;
    private String path;
    private long uploadTime;
    private long updateTime;
    private long count;

    public DeployOfProcess() {
        companyID=IdManager.IdForDeployOfProcess++;
        name="leave";
        uploadTime=System.currentTimeMillis();
        updateTime=System.currentTimeMillis();
    }

    public DeployOfProcess(long companyID, String name, String path, long uploadTime, long updateTime, long count) {
        this.companyID = companyID;
        this.name = name;
        this.path = path;
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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
