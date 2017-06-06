package backend.serial;

import backend.mdoel.Employee;
import backend.mdoel.Section;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

/**
 * Created by lenovo on 2017/5/23.
 */
public class SectionSerializer extends StdSerializer<Section> {
    public SectionSerializer() { this(null); }
    public SectionSerializer(Class<Section> t) { super(t); }

    @Override
    public void serialize(Section section, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if(section == null) {
            System.out.println("section is null.");
            return;
        }
        jsonGenerator.writeStartObject();

        jsonGenerator.writeNumberField("ID",section.getID());
        jsonGenerator.writeNumberField("leaderID",section.getLeader().getID());

        jsonGenerator.writeArrayFieldStart("membersID");
        for(Employee employee:section.getMembers()) {
            long id=employee.getID();
            jsonGenerator.writeNumber(id);
        }
        jsonGenerator.writeEndArray();

        jsonGenerator.writeStringField("name",section.getName());
        jsonGenerator.writeStringField("note",section.getNote());

        jsonGenerator.writeArrayFieldStart("childrenSections");
        for(Section child:section.getChildrenSections()) {
            jsonGenerator.writeTree(JsonNodeFactory.instance.pojoNode(child));
        }
        jsonGenerator.writeEndArray();

        jsonGenerator.writeEndObject();
    }
}
