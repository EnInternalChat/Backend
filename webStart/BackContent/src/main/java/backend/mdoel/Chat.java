package backend.mdoel;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by lenovo on 2017/5/14.
 */

@Document
public class Chat {
    @Id
    private long ID;
    private long companyID;
    private String mark;
    private Long trdPartyID;

    public Chat() {
    }

    public Chat(long ID, long companyID, String mark) {
        this.ID=ID;
        this.companyID=companyID;
        this.mark=mark;
    }

    public long getID() {
        return ID;
    }

    public String getMark() {
        return mark;
    }

    public Long getTrdPartyID() {
        return trdPartyID;
    }

    public void setTrdPartyID(Long trdPartyID) {
        this.trdPartyID = trdPartyID;
    }
}
