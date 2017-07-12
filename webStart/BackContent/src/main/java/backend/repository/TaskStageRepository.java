package backend.repository;

import backend.mdoel.TaskStage;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Created by lenovo on 2017/5/31.
 */
public interface TaskStageRepository extends MongoRepository<TaskStage, Long> {
    List<TaskStage> findByProcessIDAndActivityID(String processID, String activityID);
}
