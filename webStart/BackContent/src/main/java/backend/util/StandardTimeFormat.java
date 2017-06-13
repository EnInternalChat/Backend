package backend.util;

import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Created by lenovo on 2017/5/12.
 */
public class StandardTimeFormat {
    private static SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    public static String getCurrentTimeStr() {
        OffsetDateTime odt = OffsetDateTime.now ();
        ZoneOffset zoneOffset = odt.getOffset ();
        return formatter.format(System.currentTimeMillis())+zoneOffset;
    }
}
