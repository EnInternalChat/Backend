package backend.controller;

import backend.mdoel.Employee;
import backend.service.DatabaseService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

/**
 * Created by lenovo on 2017/5/2.
 */

@Controller
@CrossOrigin
@RequestMapping(value = "/employees")
public class EmployeeController {
    @Autowired
    DatabaseService databaseService;

    @ApiOperation(value = "获取公司职员信息",notes = "通过公司id获取职员信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "companyID",value = "公司id", required = true, dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "page", value = "页数(从0开始)", dataType = "Integer", paramType = "query"),
            @ApiImplicitParam(name = "limit", value = "单页信息条数", dataType = "Integer", paramType = "query")
    })
    @ResponseBody
    @RequestMapping(value = "/{companyID}", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public Collection<Employee> employeesData(@PathVariable("companyID") Long companyID, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "limit", required = false) Integer limit) {
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
}
