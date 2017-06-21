package backend.serial;

import backend.mdoel.DeployOfProcess;
import backend.service.ActivitiService;
import backend.util.StandardTimeFormat;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

/**
 * Created by lenovo on 2017/6/22.
 */
public class DeployOfProcessSerializer extends StdSerializer<DeployOfProcess> {
    @Autowired
    ActivitiService activitiService;

    public DeployOfProcessSerializer() {this(null);}
    public DeployOfProcessSerializer(Class<DeployOfProcess> t) {
        super(t);
    }

    @Override
    public void serialize(DeployOfProcess deployOfProcess, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if(deployOfProcess == null) {
            System.out.println("deploy empty");
            return;
        }
        jsonGenerator.writeStartObject();

        jsonGenerator.writeNumberField("ID",deployOfProcess.getID());
        jsonGenerator.writeNumberField("companyID",deployOfProcess.getCompanyID());
        jsonGenerator.writeStringField("processKey",deployOfProcess.getName());
        jsonGenerator.writeStringField("uploadTime", StandardTimeFormat.parse(deployOfProcess.getUploadTime()));
        jsonGenerator.writeStringField("updateTime",StandardTimeFormat.parse(deployOfProcess.getUpdateTime()));
        //TODO activiti xmldata

        jsonGenerator.writeEndObject();
    }

}
