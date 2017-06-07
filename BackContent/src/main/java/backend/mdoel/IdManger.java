package backend.mdoel;

import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by lenovo on 2017/6/7.
 */

@Document
public class IdManger {
    private int IChat;
    private int ICompany;
    private int IEmployee;
    private int IInstanceOfProcess;
    private int INotification;
    private int ISection;
    private int ITaskStage;

    public IdManger() {
    }

    public IdManger(int IChat, int ICompany, int IEmployee, int IInstanceOfProcess, int INotification, int ISection, int ITaskStage) {
        this();
        this.IChat = IChat;
        this.ICompany = ICompany;
        this.IEmployee = IEmployee;
        this.IInstanceOfProcess = IInstanceOfProcess;
        this.INotification = INotification;
        this.ISection = ISection;
        this.ITaskStage = ITaskStage;
    }

    public int getIChat() {
        return IChat;
    }

    public void setIChat(int IChat) {
        this.IChat = IChat;
    }

    public int getICompany() {
        return ICompany;
    }

    public void setICompany(int ICompany) {
        this.ICompany = ICompany;
    }

    public int getIEmployee() {
        return IEmployee;
    }

    public void setIEmployee(int IEmployee) {
        this.IEmployee = IEmployee;
    }

    public int getIInstanceOfProcess() {
        return IInstanceOfProcess;
    }

    public void setIInstanceOfProcess(int IInstanceOfProcess) {
        this.IInstanceOfProcess = IInstanceOfProcess;
    }

    public int getINotification() {
        return INotification;
    }

    public void setINotification(int INotification) {
        this.INotification = INotification;
    }

    public int getISection() {
        return ISection;
    }

    public void setISection(int ISection) {
        this.ISection = ISection;
    }

    public int getITaskStage() {
        return ITaskStage;
    }

    public void setITaskStage(int ITaskStage) {
        this.ITaskStage = ITaskStage;
    }
}
