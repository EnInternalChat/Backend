package backend.controller;

import backend.mdoel.Employee;
import backend.service.DatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

/**
 * Created by lenovo on 2017/5/2.
 */

@Controller
@RequestMapping(value = "/employees")
public class EmployeeController {
    @Autowired
    DatabaseService databaseService;

    @ResponseBody
    @RequestMapping(value = "/{companyID}", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public Collection<Employee> employeesData(@PathVariable("companyID") Integer companyID, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "limit", required = false) Integer limit) {
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
