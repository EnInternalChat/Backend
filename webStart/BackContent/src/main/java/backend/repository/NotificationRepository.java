package backend.repository;

import backend.mdoel.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Created by lenovo on 2017/5/14.
 */
public interface NotificationRepository extends MongoRepository<Notification, Long> {
    List<Notification> findBySenderIDAndCompanyID(long companyID, long senderID);
}
