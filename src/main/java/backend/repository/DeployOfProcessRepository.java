package backend.repository;

import backend.mdoel.DeployOfProcess;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Created by lenovo on 2017/5/14.
 */
public interface DeployOfProcessRepository extends MongoRepository<DeployOfProcess, Long> {
    List<DeployOfProcess> findByCompanyID(long companyID);
}
