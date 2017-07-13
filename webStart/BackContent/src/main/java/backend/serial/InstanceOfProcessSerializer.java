package backend.serial;

import backend.mdoel.InstanceOfProcess;
import backend.mdoel.TaskStage;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.util.Collection;

/**
 * Created by lenovo on 2017/7/13.
 */
public class InstanceOfProcessSerializer extends StdSerializer<InstanceOfProcess> {
    public InstanceOfProcessSerializer() {this(null);}
    public InstanceOfProcessSerializer(Class<InstanceOfProcess> t) {super(t);}

    @Override
    public void serialize(InstanceOfProcess instanceOfProcess, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if(instanceOfProcess == null) {
            System.out.println("instanceOfProcess is null.");
            return;
        }
        jsonGenerator.writeStartObject();

        jsonGenerator.writeNumberField("ID",instanceOfProcess.getID());
        jsonGenerator.writeStringField("processID",instanceOfProcess.getProcessID());
        jsonGenerator.writeStringField("processName",instanceOfProcess.getProcessName());
        jsonGenerator.writeBooleanField("over",instanceOfProcess.isOver());
        jsonGenerator.writeObjectField("startPerson",instanceOfProcess.getStartPerson());
        jsonGenerator.writeArrayFieldStart("stages");
        Collection<TaskStage> stages=instanceOfProcess.getStages();
        for(TaskStage stage:stages) {
            jsonGenerator.writeObject(stage);
        }
        jsonGenerator.writeEndArray();

        jsonGenerator.writeEndObject();
    }
}
