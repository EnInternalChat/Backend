package backend.repository;

import backend.mdoel.IdManger;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Created by lenovo on 2017/6/8.
 */
public interface IdMangerRepository extends MongoRepository<IdManger, Integer> {
}
