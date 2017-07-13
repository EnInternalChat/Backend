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
    private long iChat;
    private long iCompany;
    private long iEmployee;
    private long iDeployOfProcess;
    private long iInstanceOfProcess;
    private long iNotification;
    private long iSection;
    private long iTaskStage;

    public IdManager() {
        ID=0;
    }

    public IdManager(long IDeployOfProcess, int IChat, int ICompany, int IEmployee, int IInstanceOfProcess, int INotification, int ISection, int ITaskStage) {
        this();
        this.iChat = IChat;
        this.iCompany = ICompany;
        this.iEmployee = IEmployee;
        this.iDeployOfProcess=IDeployOfProcess;
        this.iInstanceOfProcess = IInstanceOfProcess;
        this.iNotification = INotification;
        this.iSection = ISection;
        this.iTaskStage = ITaskStage;
    }

    public long getIChat() {
        return iChat;
    }

    public void setIChat(long iChat) {
        this.iChat = iChat;
    }

    public long getICompany() {
        return iCompany;
    }

    public void setICompany(long iCompany) {
        this.iCompany = iCompany;
    }

    public long getIEmployee() {
        return iEmployee;
    }

    public void setIEmployee(long iEmployee) {
        this.iEmployee = iEmployee;
    }

    public long getIDeployOfProcess() {
        return iDeployOfProcess;
    }

    public void setIDeployOfProcess(long iDeployOfProcess) {
        this.iDeployOfProcess = iDeployOfProcess;
    }

    public long getIInstanceOfProcess() {
        return iInstanceOfProcess;
    }

    public void setIInstanceOfProcess(long iInstanceOfProcess) {
        this.iInstanceOfProcess = iInstanceOfProcess;
    }

    public long getINotification() {
        return iNotification;
    }

    public void setINotification(long iNotification) {
        this.iNotification = iNotification;
    }

    public long getISection() {
        return iSection;
    }

    public void setISection(long iSection) {
        this.iSection = iSection;
    }

    public long getITaskStage() {
        return iTaskStage;
    }

    public void setITaskStage(long iTaskStage) {
        this.iTaskStage = iTaskStage;
    }
}
