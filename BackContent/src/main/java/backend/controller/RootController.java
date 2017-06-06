package backend.controller;

import backend.service.DatabaseService;
import backend.util.ResponseJsonObj;
import org.activiti.engine.impl.util.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by lenovo on 2017/5/2.
 */

@Controller
@CrossOrigin
public class RootController {
    @Autowired
    DatabaseService databaseService;

    @ResponseBody
    @RequestMapping(value = "/login", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void logindo(@RequestParam("name") String name,
                        @RequestParam("password") String password,
                        @RequestParam("companyID") Integer companyID,
                        HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        JSONObject jsonObject=databaseService.findLoginEmployee(companyID,name,password,httpServletRequest);
        ResponseJsonObj.write(httpServletResponse,jsonObject);
    }

    @ResponseBody
    @RequestMapping(value = "/logout", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void logout(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        JSONObject jsonObject=databaseService.logout(httpServletRequest);
        ResponseJsonObj.write(httpServletResponse,jsonObject);
    }
}
