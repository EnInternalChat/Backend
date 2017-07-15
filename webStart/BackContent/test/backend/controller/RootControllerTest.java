package backend.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {"classpath:spring-mvc.xml", "classpath:xmlConfig/activiti.xml", "classpath:xmlConfig/session.xml"})
@Transactional
public class RootControllerTest {
    @Autowired
    private WebApplicationContext wac;
    private MockMvc mockMvc;
    private String basic="";

    @Before
    public void setUp() throws Exception {
        this.mockMvc = webAppContextSetup(this.wac).build();
    }
    @Test
    public void login() throws Exception {
        mockMvc.perform(post(basic+"/login")
                .param("name","tomcat")
                .param("password","1b359d8753858b55befa0441067aaed3")
                .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(content().string("{\"companyID\":0,\"ID\":17,\"sectionID\":2,\"avatar\":1,\"status\":true,\"info\":\"登陆成功\"}"))
                .andDo(print());
    }
    @Test
    public void logout() throws Exception {
        mockMvc.perform(post(basic+"/login")
                .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().is(400))
                .andDo(print()).andReturn().getResponse().getHeader("x-auth-token");
    }
}