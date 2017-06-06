package backend.util;

import org.activiti.engine.impl.util.json.JSONObject;
import org.springframework.http.MediaType;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by lenovo on 2017/5/31.
 */
public class ResponseJsonObj {
    public static void write(HttpServletResponse httpServletResponse, JSONObject jsonObject) {
        httpServletResponse.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        try {
            httpServletResponse.getWriter().write(jsonObject.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
