package backend.controller;

import backend.mdoel.Company;
import backend.mdoel.Section;
import backend.service.DatabaseService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * Created by lenovo on 2017/5/26.
 */

@RestController
@CrossOrigin
@RequestMapping(value = "/company")
public class CompanyController {
    @Autowired
    DatabaseService databaseService;

    @ApiOperation(value = "获取公司整体信息", notes = "根据公司id获取公司信息")
    @ApiImplicitParam(name = "companyID", value = "公司id", required = true, dataType = "Long", paramType = "path")
    @ResponseBody
    @RequestMapping(value = "/{companyID}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Company companyData(@PathVariable("companyID") Long companyID) {
        return databaseService.findComById(companyID);
    }

    @ApiOperation(value = "获取公司部门信息", notes = "信息以树状结构显示")
    @ApiImplicitParam(name = "sectionID", value = "部门id", required = true, dataType = "Long", paramType = "path")
    @ResponseBody
    @RequestMapping(value = "/sections/{sectionID}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Section sectionData(@PathVariable("sectionID") Long sectionID) {
        return databaseService.findSecByID(sectionID);
    }
}
