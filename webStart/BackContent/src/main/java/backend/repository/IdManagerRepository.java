package backend.repository;

import backend.mdoel.IdManager;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Created by lenovo on 2017/6/8.
 */
public interface IdManagerRepository extends MongoRepository<IdManager, Long> {
}
