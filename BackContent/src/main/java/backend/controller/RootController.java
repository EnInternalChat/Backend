package backend.controller;

import backend.service.DatabaseService;
import backend.util.ResponseJsonObj;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.activiti.engine.impl.util.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by lenovo on 2017/5/2.
 */

@RestController
@CrossOrigin
@RequestMapping(value = "")
public class RootController {
    @Autowired
    DatabaseService databaseService;

    @ApiOperation(value = "登录", notes = "用户登录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "用户名", required = true, dataType = "String", paramType = "body"),
            @ApiImplicitParam(name = "password", value = "用户密码", required = true, dataType = "String", paramType = "body"),
    })
    @ResponseBody
    @RequestMapping(value = "/login", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void logindo(@RequestParam("name") String name,
                        @RequestParam("password") String password,
                        HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        JSONObject jsonObject=databaseService.findLoginEmployee(name,password,httpServletRequest);
        ResponseJsonObj.write(httpServletResponse,jsonObject);
    }

    @ApiOperation(value = "注销账号", notes = "用户退出登录")
    @ApiImplicitParam(name = "x-auth-token",value = "有效token", required = true, dataType = "String", paramType = "header")
    @ResponseBody
    @RequestMapping(value = "/logout", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void logout(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        JSONObject jsonObject=databaseService.logout(httpServletRequest);
        ResponseJsonObj.write(httpServletResponse,jsonObject);
    }
}
