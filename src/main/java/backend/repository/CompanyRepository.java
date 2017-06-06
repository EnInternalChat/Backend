package backend.repository;

import backend.mdoel.Company;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Created by lenovo on 2017/5/14.
 */
public interface CompanyRepository extends MongoRepository<Company, Long> {
}
