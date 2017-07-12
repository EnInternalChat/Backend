package backend.mdoel;

import backend.serial.DeployOfProcessSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Collection;
import java.util.Set;

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
    private String processKey;
    private String path;
    private String data;
    private long uploadTime;
    private long updateTime;
    private long count;
    private Collection<String> labels;

    public DeployOfProcess(long ID, long companyID, String name, String processKey,
                           String path, String data, long uploadTime,
                           long updateTime, long count, Set<String> labels) {
        this.ID=ID;
        this.companyID = companyID;
        this.name = name;
        this.processKey=processKey;
        this.path=path;
        this.uploadTime = uploadTime;
        this.updateTime = updateTime;
        this.count = count;
        this.data=data;
        this.labels=labels;
    }

    public String getData() {
        return data;
    }

    public String getProcessKey() {
        return processKey;
    }

    public Collection<String> getLabels() {
        return labels;
    }

    public String getPath() {
        return path;
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
