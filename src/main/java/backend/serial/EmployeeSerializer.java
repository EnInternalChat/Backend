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

    }
}
