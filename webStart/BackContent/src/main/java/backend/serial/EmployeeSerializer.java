package backend.serial;

import backend.mdoel.Employee;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

/**
 * Created by lenovo on 2017/5/26.
 */
public class EmployeeSerializer extends StdSerializer<Employee> {
    public EmployeeSerializer() {this(null);}

    public EmployeeSerializer(Class<Employee> t) {
        super(t);
    }

    @Override
    public void serialize(Employee employee, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if(employee == null) {
            System.out.println("employee is null.");
            return;
        }
        jsonGenerator.writeStartObject();

        jsonGenerator.writeNumberField("ID",employee.getID());
        jsonGenerator.writeNumberField("sectionID",employee.getSectionID());
        jsonGenerator.writeNumberField("avatar",employee.getAvatar());
        jsonGenerator.writeNumberField("companyID",employee.getCompanyID());
        jsonGenerator.writeStringField("name",employee.getName());
        jsonGenerator.writeStringField("position",employee.getPosition());
        if(employee.isGender()) {
            jsonGenerator.writeStringField("gender","female");
        } else {
            jsonGenerator.writeStringField("gender","male");
        }
        if(employee.isActive()) {
            jsonGenerator.writeStringField("status","active");
        } else {
            jsonGenerator.writeStringField("status","banned");
        }
        jsonGenerator.writeBooleanField("leader",employee.isLeader());

        jsonGenerator.writeArrayFieldStart("phone");
        for(String s:employee.getPhone()) {
            jsonGenerator.writeString(s);
        }
        jsonGenerator.writeEndArray();
        jsonGenerator.writeArrayFieldStart("email");
        for(String s:employee.getEmail()) {
            jsonGenerator.writeString(s);
        }
        jsonGenerator.writeEndArray();

        jsonGenerator.writeEndObject();
    }
}
