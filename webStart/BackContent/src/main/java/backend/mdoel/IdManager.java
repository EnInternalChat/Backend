package backend.mdoel;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by lenovo on 2017/6/7.
 */

@Document
public class IdManager {
    @Id
    private long ID;
    private long IChat;
    private long ICompany;
    private long IEmployee;
    private long IDeployOfProcess;
    private long IInstanceOfProcess;
    private long INotification;
    private long ISection;
    private long ITaskStage;

    public IdManager() {
        ID=0;
    }

    public IdManager(long IDeployOfProcess, int IChat, int ICompany, int IEmployee, int IInstanceOfProcess, int INotification, int ISection, int ITaskStage) {
        this();
        this.IChat = IChat;
        this.ICompany = ICompany;
        this.IEmployee = IEmployee;
        this.IDeployOfProcess=IDeployOfProcess;
        this.IInstanceOfProcess = IInstanceOfProcess;
        this.INotification = INotification;
        this.ISection = ISection;
        this.ITaskStage = ITaskStage;
    }

    public long getIDeployOfProcess() {
        return IDeployOfProcess++;
    }

    public long getIChat() {
        return IChat++;
    }

    public long getICompany() {
        return ICompany++;
    }

    public long getIEmployee() {
        return IEmployee++;
    }

    public long getIInstanceOfProcess() {
        return IInstanceOfProcess++;
    }

    public long getINotification() {
        return INotification++;
    }

    public long getISection() {
        return ISection++;
    }

    public long getITaskStage() {
        return ITaskStage++;
    }
}
