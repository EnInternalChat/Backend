package backend.repository;

import backend.mdoel.InstanceOfProcess;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Created by lenovo on 2017/5/31.
 */
public interface InstanceOfProcessRepository extends MongoRepository<InstanceOfProcess, Long> {
    List<InstanceOfProcess> findByCompanyIDAndAndProcessID(long companyID, String processID);
}
