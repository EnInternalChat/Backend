package backend.controller;

import backend.mdoel.Company;
import backend.mdoel.Section;
import backend.service.DatabaseService;
import backend.util.ResponseJsonObj;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.activiti.engine.impl.util.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

/**
 * Created by lenovo on 2017/5/26.
 */

@RestController
@CrossOrigin
@Api(description = "公司数据")
@RequestMapping(value = "/company")
public class CompanyController {
    @Autowired
    DatabaseService databaseService;

    @ApiOperation(value = "获取公司整体信息", notes = "根据公司id获取公司信息", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "companyID", value = "公司id", required = true, dataType = "Long", paramType = "path")
    })
    @ResponseBody
    @RequestMapping(value = "/{companyID}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Company companyData(@PathVariable("companyID") Long companyID) {
        return databaseService.findComById(companyID);
    }

    @ApiOperation(value = "获取公司部门信息", notes = "信息以树状结构显示")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "companyID", value = "公司id", required = true, dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "sectionID", value = "部门id", required = true, dataType = "Long", paramType = "path")
    })
    @ResponseBody
    @RequestMapping(value = "/{companyID}/sections/{sectionID}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Section sectionData(@PathVariable("companyID") Long companyID,
                               @PathVariable("sectionID") Long sectionID) {
        return databaseService.findSecByID(companyID,sectionID);
    }

    @ApiOperation(value = "添加部门", notes = "为某部门添加子部门", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "companyID", value = "公司id", required = true, dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "sectionID", value = "父部门id", required = true, dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "description", value = "部门描述", dataType = "String", paramType = "body"),
            @ApiImplicitParam(name = "name", value = "部门名称", required = true, dataType = "String", paramType = "body")
    })
    @ResponseBody
    @RequestMapping(value = "/{companyID}/{sectionID}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void addSection(HttpServletResponse httpServletResponse,
                           @PathVariable("companyID") Long companyID, @PathVariable("sectionID") Long sectionID,
                           @RequestParam("name") String secName,
                           @RequestParam(value = "description", required = false) String description
    ) {
        JSONObject jsonObject=databaseService.addNewSection(companyID,sectionID,secName,description);
        ResponseJsonObj.write(httpServletResponse,jsonObject);
    }

    @ApiOperation(value = "修改部门信息", notes = "修改部门名称，部长，描述等信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "companyID", value = "公司id", required = true, dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "sectionID", value = "部门id", required = true, dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "leaderID", value = "部长id", dataType = "Long", paramType = "body"),
            @ApiImplicitParam(name = "description", value = "部门描述", dataType = "String", paramType = "body"),
            @ApiImplicitParam(name = "name", value = "部门新名称", dataType = "String", paramType = "body")
    })
    @ResponseBody
    @RequestMapping(value = "/{companyID}/sections/{sectionID}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void modifySection(HttpServletResponse httpServletResponse,
                              @PathVariable("companyID") Long companyID, @PathVariable("sectionID") Long sectionID,
                              @RequestParam(value = "leaderID", required = false) Long leaderID,
                              @RequestParam(value = "description", required = false) String description,
                              @RequestParam(value = "name", required = false) String name
                              ) {
        JSONObject jsonObject=databaseService.modifySectionData(companyID,sectionID,description,name,leaderID);
        ResponseJsonObj.write(httpServletResponse,jsonObject);
    }

    @ApiOperation(value = "删除部门", notes = "删除公司部门")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "companyID", value = "公司id", required = true, dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "sectionID", value = "部门id", required = true, dataType = "Long", paramType = "path"),
    })
    @ResponseBody
    @RequestMapping(value = "/{companyID}/sections/{sectionID}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void delSection(HttpServletResponse httpServletResponse,
                           @PathVariable("companyID") Long companyID,
                           @PathVariable("sectionID") Long sectionID) {
        JSONObject jsonObject=databaseService.delSection(companyID,sectionID);
        ResponseJsonObj.write(httpServletResponse,jsonObject);
    }
}
