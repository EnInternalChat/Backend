package backend.serial;

import backend.mdoel.TaskStage;
import backend.util.StandardTimeFormat;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

/**
 * Created by lenovo on 2017/7/13.
 */
public class TaskStageSerializer extends StdSerializer<TaskStage> {
    public TaskStageSerializer() {this(null);}
    public TaskStageSerializer(Class<TaskStage> t) {super(t);}

    @Override
    public void serialize(TaskStage taskStage, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if(taskStage == null) {
            System.out.println("taskStage is null.");
            return;
        }
        jsonGenerator.writeStartObject();

        jsonGenerator.writeStringField("activityID",taskStage.getActivityID());
        jsonGenerator.writeStringField("title",taskStage.getTitle());
        jsonGenerator.writeStringField("content",taskStage.getContent());
        jsonGenerator.writeStringField("startTime", StandardTimeFormat.parse(taskStage.getStartTime()));
        Long finishTime=taskStage.getFinishTime();
        if(finishTime != null) {
            jsonGenerator.writeStringField("finishTime",StandardTimeFormat.parse(taskStage.getFinishTime()));
        }
        jsonGenerator.writeObjectField("person",taskStage.getPersonData());
        jsonGenerator.writeObjectField("exclusiveGateway",taskStage.getChoices());

        jsonGenerator.writeEndObject();
    }
}
