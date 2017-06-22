package backend.controller;

import backend.mdoel.Employee;
import backend.service.DatabaseService;
import backend.util.ResponseJsonObj;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.activiti.engine.impl.util.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.Collection;

/**
 * Created by lenovo on 2017/5/2.
 */

@RestController
@CrossOrigin
@Api(description = "人员数据")
@RequestMapping(value = "/employees")
public class EmployeeController {
    @Autowired
    DatabaseService databaseService;

    @ApiOperation(value = "获取公司人员信息",notes = "通过公司id获取人员信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "companyID",value = "公司id", required = true, dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "page", value = "页数(从0开始)", dataType = "Integer", paramType = "query"),
            @ApiImplicitParam(name = "limit", value = "单页信息条数", dataType = "Integer", paramType = "query")
    })
    @ResponseBody
    @RequestMapping(value = "/{companyID}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Collection<Employee> companyEmployeesData(@PathVariable("companyID") Long companyID,
                                                     @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "limit", required = false) Integer limit) {
        Collection<Employee> employees=databaseService.employeesCompany(companyID,null);
        if(page == null && limit == null) {
            return employees;
        } else if(page == null || limit == null) {
            return null;
        } else if(page*limit<employees.size()) {
            PageRequest pageRequest=new PageRequest(page,limit);
            employees=databaseService.employeesCompany(companyID,pageRequest);
            return employees;
        }
        return null;
    }

    @ApiOperation(value = "获取部门人员信息", notes = "通过公司id与部门id获取人员信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "companyID",value = "公司id", required = true, dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "sectionID",value = "部门id", required = true, dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "page", value = "页数(从0开始)", dataType = "Integer", paramType = "query"),
            @ApiImplicitParam(name = "limit", value = "单页信息条数", dataType = "Integer", paramType = "query")
    })
    @ResponseBody
    @RequestMapping(value = "/{companyID}/{sectionID}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Collection<Employee> sectionEmployeeData(@PathVariable("companyID") Long companyID, @PathVariable("sectionID") Long sectionID,
                                                    @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "limit", required = false) Integer limit) {
        Collection<Employee> employees=databaseService.employeesSection(companyID,sectionID,null);
        if(page == null && limit == null) {
            return employees;
        } else if(page == null || limit == null) {
            return null;
        } else if(page*limit<employees.size()) {
            PageRequest pageRequest=new PageRequest(page,limit);
            employees=databaseService.employeesSection(companyID,sectionID,pageRequest);
            return employees;
        }
        return null;
    }

    @ApiOperation(value = "增加新人员", notes = "公司部门增添新员工")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "companyID",value = "公司id", required = true, dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "sectionID",value = "部门id", required = true, dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "name", value = "姓名", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "password", value = "秘密", defaultValue = "123456", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "position",value = "职位名称", required = true, dataType = "String", paramType = "query")
    })
    @ResponseBody
    @RequestMapping(value = "/{companyID}/{sectionID}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void addEmployee(@PathVariable("companyID") Long companyID, @PathVariable("sectionID") Long sectionID,
                            @RequestParam("name") String name, @RequestParam("position") String position,
                            @RequestParam(value = "password", required = false) String password) {

    }

    @ApiOperation(value = "删除人员", notes = "公司部门删除员工")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "companyID",value = "公司id", required = true, dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "sectionID",value = "部门id", required = true, dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "ID",value = "人员id", required = true, dataType = "Long", paramType = "path")
    })
    @ResponseBody
    @RequestMapping(value = "/{companyID}/{sectionID}/{ID}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void delEmployee(@PathVariable("companyID") Long companyID, @PathVariable("sectionID") Long sectionID, @PathVariable("ID") Long ID) {

    }

    @ApiOperation(value = "修改人员信息", notes = "员工修改个人信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "companyID",value = "公司id", required = true, dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "sectionID",value = "部门id", required = true, dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "ID",value = "人员id", required = true, dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "newPwd",value = "新密码", dataType = "String", paramType = "body"),
            @ApiImplicitParam(name = "email1",value = "邮箱1", dataType = "String", paramType = "body"),
            @ApiImplicitParam(name = "email2",value = "邮箱2", dataType = "String", paramType = "body"),
            @ApiImplicitParam(name = "phone1",value = "电话1", dataType = "String", paramType = "body"),
            @ApiImplicitParam(name = "phone2",value = "电话2", dataType = "String", paramType = "body")
    })
    @ResponseBody
    @RequestMapping(value = "/{companyID}/{sectionID}/{ID}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void modifyEmployee(@PathVariable("companyID") Long companyID, @PathVariable("sectionID") Long sectionID,
                                     @PathVariable("ID") Long ID,HttpServletResponse httpServletResponse,
                                     @RequestParam(value = "newPwd" ,required = false) String newPwd,
                                     @RequestParam(value = "email1", required = false) String email1,
                                     @RequestParam(value = "email2", required = false) String email2,
                                     @RequestParam(value = "phone1", required = false) String phone1,
                                     @RequestParam(value = "phone2", required = false) String phone2
                               ) {
        JSONObject jsonObject=databaseService.updateEmployeePersonal(companyID, sectionID, ID, newPwd, email1, email2, phone1, phone2);
        ResponseJsonObj.write(httpServletResponse,jsonObject);
        //TODO fix who can modify? admin? himself?
    }
}
