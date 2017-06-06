package backend.repository;

import backend.mdoel.InstanceOfProcess;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Created by lenovo on 2017/5/31.
 */
public interface InstanceOfProcessRepository extends MongoRepository<InstanceOfProcess, Long> {
}
