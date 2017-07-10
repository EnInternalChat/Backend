package backend.repository;

import backend.mdoel.Employee;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Created by lenovo on 2017/5/7.
 */

public interface EmployeeRepository extends MongoRepository<Employee, Long> {
    List<Employee> findByCompanyIDAndSectionID (long companyID, long sectionID);
    List<Employee> findByCompanyID(long companyID);
    List<Employee> findByCompanyIDAndSectionID (long companyID, long sectionID, Pageable pageable);
    List<Employee> findByCompanyID(long companyID, Pageable pageable);
    List<Employee> findByName(String name);
}
