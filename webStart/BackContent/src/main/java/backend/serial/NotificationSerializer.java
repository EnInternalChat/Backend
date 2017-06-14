package backend.serial;

import backend.mdoel.Notification;
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

    }
}
