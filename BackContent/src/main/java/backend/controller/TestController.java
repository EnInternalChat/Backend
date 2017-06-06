package backend.controller;

import backend.mdoel.Company;
import backend.mdoel.Section;
import backend.service.DatabaseService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lenovo on 2017/4/30.
 */

@Controller
public class TestController {
    RepositoryService repositoryService;
    ProcessDefinition processDefinition;

    @Autowired
    private DatabaseService databaseService;

    public TestController() {
        ProcessEngineConfiguration cfg = new StandaloneProcessEngineConfiguration()
                .setJdbcUrl("jdbc:h2:mem:activiti;DB_CLOSE_DELAY=1000")
                .setJdbcUsername("sa")
                .setJdbcPassword("")
                .setJdbcDriver("org.h2.Driver")
                .setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);
        ProcessEngine processEngine=cfg.buildProcessEngine();
        repositoryService=processEngine.getRepositoryService();
    }

    private void diagramFile(File xmlFile, OutputStream outputStream) {
        Deployment deployment;
        deployment = repositoryService.createDeployment().addClasspathResource("tmp"+File.separator+xmlFile.getName()).deploy();
        processDefinition=repositoryService.createProcessDefinitionQuery()
                .deploymentId(deployment.getId()).singleResult();
        String diagramName=processDefinition.getDiagramResourceName();
        InputStream resourceAsStream=repositoryService.getResourceAsStream(processDefinition.getDeploymentId(), diagramName);
        try {
            byte[] b=new byte[resourceAsStream.available()];
            resourceAsStream.read(b, 0, b.length);
            outputStream.write(b);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @ResponseBody
    @RequestMapping(value = "/getJson", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Map<String,Object> testSpring() {
        Map<String,Object> resMap = new HashMap<>();
        resMap.put("user","name");
        return resMap;
    }

    @ResponseBody
    @RequestMapping(value = "/testMongo", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void mongoBug() throws UnknownHostException {
        databaseService.testNewStruc();
    }

    @ResponseBody
    @RequestMapping(value = "/testSerialize1", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Section getSecs(HttpSession session) {
        System.out.println(session.getAttribute("name"));
        return databaseService.findSecByID((long) 0);
    }

    @ResponseBody
    @RequestMapping(value = "/testSerialize2", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Company getCompany(HttpSession session) {
        session.setAttribute("name", "Amayadream");
        return databaseService.findComById((long) 0);
    }

    @ResponseBody
    @RequestMapping(value = "/modelToDiagram", method = RequestMethod.POST)
    public void diagram(@RequestParam("file")CommonsMultipartFile file, HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("image/png");
        String path= this.getClass().getResource("/").getPath()+File.separator+"tmp"+File.separator;
        File dir=new File(path);
        if(!dir.isDirectory()) dir.mkdir();
        System.out.println(path);
        File xmlFile=new File(path, file.getName()+".bpmn20.xml");
        if(xmlFile.exists()) {
            xmlFile.delete();
            xmlFile.createNewFile();
        }
        file.transferTo(xmlFile);
        diagramFile(xmlFile, response.getOutputStream());
    }

    @ResponseBody
    @RequestMapping(value = "/login", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void helloUser(HttpServletResponse response) {

    }


}
