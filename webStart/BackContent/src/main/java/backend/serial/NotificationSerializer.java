package backend.serial;

import backend.mdoel.Notification;
import backend.util.StandardTimeFormat;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

/**
 * Created by lenovo on 2017/5/26.
 */
public class NotificationSerializer extends StdSerializer<Notification> {
    public NotificationSerializer() {this(null);}
    public NotificationSerializer(Class<Notification> t) {
        super(t);
    }

    @Override
    public void serialize(Notification notification, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if(notification == null) {
            System.out.println("notification empty");
            return;
        }
        jsonGenerator.writeStartObject();

        jsonGenerator.writeNumberField("ID",notification.getID());
        jsonGenerator.writeNumberField("companyID",notification.getCompanyID());
        jsonGenerator.writeStringField("senderName",notification.getSenderName());
        jsonGenerator.writeStringField("sentTime", StandardTimeFormat.parse(notification.getSentTime()));
        jsonGenerator.writeStringField("content",notification.getContent());
        jsonGenerator.writeStringField("title",notification.getTitle());

        jsonGenerator.writeEndObject();
    }
}
