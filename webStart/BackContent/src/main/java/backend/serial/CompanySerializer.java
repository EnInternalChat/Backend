package backend.serial;

import backend.mdoel.Company;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

/**
 * Created by lenovo on 2017/5/23.
 */
public class CompanySerializer extends StdSerializer<Company> {
    public CompanySerializer() {this(null);}
    public CompanySerializer(Class<Company> t) { super(t); }

    @Override
    public void serialize(Company company, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if(company == null) {
            System.out.println("company is null");
            return;
        }
        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberField("ID",company.getID());
        if(company.hasSection()) {
            jsonGenerator.writeObjectField("organization",company.getHeadSec());
        } else {
            jsonGenerator.writeNullField("organization");
            System.out.println("empty company");
        }
        jsonGenerator.writeStringField("name",company.getName());
        jsonGenerator.writeStringField("introduction",company.getIntroduction());
        jsonGenerator.writeEndObject();
    }
}
