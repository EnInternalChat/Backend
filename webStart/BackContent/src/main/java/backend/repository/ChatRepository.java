package backend.repository;

import backend.mdoel.Chat;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Created by lenovo on 2017/5/14.
 */
public interface ChatRepository extends MongoRepository<Chat, Long> {
    List<Chat> findByMark(String mark);
}
