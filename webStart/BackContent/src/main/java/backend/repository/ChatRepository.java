package backend.repository;

import backend.mdoel.Chat;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Created by lenovo on 2017/5/14.
 */
public interface ChatRepository extends MongoRepository<Chat, Long> {
}
