package backend.repository;

import backend.mdoel.TaskStage;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Created by lenovo on 2017/5/31.
 */
public interface TaskStageRepository extends MongoRepository<TaskStage, Long> {
}
