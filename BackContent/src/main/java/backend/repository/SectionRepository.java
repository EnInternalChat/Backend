package backend.repository;

import backend.mdoel.Section;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Created by lenovo on 2017/5/15.
 */
public interface SectionRepository extends MongoRepository<Section, Long> {
    List<Section> findByCompanyID(long companyID);
}
