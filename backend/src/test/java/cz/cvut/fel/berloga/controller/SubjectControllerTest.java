package cz.cvut.fel.berloga.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.cvut.fel.berloga.controller.dto.InviteUserDTO;
import cz.cvut.fel.berloga.controller.dto.LoginDTO;
import cz.cvut.fel.berloga.controller.dto.SubjectDTO;
import cz.cvut.fel.berloga.controller.dto.UserDTO;
import cz.cvut.fel.berloga.service.exceptions.AlreadyExistException;
import cz.cvut.fel.berloga.service.exceptions.PermissionException;
import cz.cvut.fel.berloga.service.exceptions.WrongInputException;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SubjectControllerTest {

    private MockMvc mock;

    @Autowired
    private ObjectMapper objMapper;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Before
    public void setUp() throws Exception {
        final MockHttpServletRequestBuilder defaultRequestBuilder = get("/dummy-path");
        DefaultMockMvcBuilder defaultMockMvcBuilder = MockMvcBuilders.webAppContextSetup(this.webApplicationContext);
        defaultMockMvcBuilder.defaultRequest(defaultRequestBuilder);
        defaultMockMvcBuilder.alwaysDo(result -> setSessionBackOnRequestBuilder(defaultRequestBuilder, result.getRequest()));
        this.mock = defaultMockMvcBuilder.build();
    }

    private MockHttpServletRequest setSessionBackOnRequestBuilder(final MockHttpServletRequestBuilder requestBuilder, final MockHttpServletRequest request) {
        requestBuilder.session((MockHttpSession) request.getSession());
        return request;
    }

    @Test
    public void t001_createSubject() throws Exception {
        LoginDTO login = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login))).andExpect(status().isOk());

        SubjectDTO subjectDTO = new SubjectDTO();
        subjectDTO.setCode("MIR54J01");
        subjectDTO.setName("Miracle");
        subjectDTO.setDescription("Some short description");

        this.mock.perform(post("/subject/create").contentType("application/json")
                .content(this.objMapper.writeValueAsString(subjectDTO)))
                .andDo(mvcResult -> System.out.println(mvcResult.getResponse().getContentAsString()))
                .andExpect(status().isOk());

        SubjectDTO[] subjects = this.objMapper.readValue(this.mock.perform(get("/subject/list-all/").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), SubjectDTO[].class);

        Assertions.assertEquals(subjects[subjects.length - 1].getCode(), "MIR54J01");

        // prepare

        InviteUserDTO invite = new InviteUserDTO("test", "test", "subjectUser1",
                "Test123", "subjectUser1@gmail.com");

        this.mock.perform(post("/user/invite").contentType("application/json")
                .content(this.objMapper.writeValueAsString(invite))).andExpect(status().isOk());

    }

    @Test
    public void t002_createSubjectAsStudent() throws Exception {
        LoginDTO login = new LoginDTO("subjectUser1", "Test123");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login))).andExpect(status().isOk());

        SubjectDTO subjectDTO = new SubjectDTO();
        subjectDTO.setCode("MIR54J02");
        subjectDTO.setName("Miracle");
        subjectDTO.setDescription("Some short description");

        this.mock.perform(post("/subject/create").contentType("application/json")
                .content(this.objMapper.writeValueAsString(subjectDTO)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof PermissionException));
    }

    @Test
    public void t003_createSubjectAlreadyExist() throws Exception {
        LoginDTO login = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login))).andExpect(status().isOk());

        // Create subject
        SubjectDTO subject1 = SubjectDTO.builder()
                .code("MIR54J")
                .name("Miracle")
                .description("Some short description")
                .build();

        // Create subject
        SubjectDTO subject2 = SubjectDTO.builder()
                .code("MIR54J")
                .name("Miracle")
                .description("Some short description")
                .build();

        this.mock.perform(post("/subject/create").contentType("application/json")
                .content(this.objMapper.writeValueAsString(subject1)))
                .andDo(mvcResult -> System.out.println(mvcResult.getResponse().getContentAsString()))
                .andExpect(status().isOk());

        this.mock.perform(post("/subject/create").contentType("application/json")
                .content(this.objMapper.writeValueAsString(subject2)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof AlreadyExistException));
    }

    @Test
    public void t004_createSubjectCodeNull() throws Exception {
        LoginDTO login = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login))).andExpect(status().isOk());

        SubjectDTO subjectDTO = new SubjectDTO();
        subjectDTO.setCode(null);
        subjectDTO.setName("Miracle");
        subjectDTO.setDescription("Some short description");

        this.mock.perform(post("/subject/create").contentType("application/json")
                .content(this.objMapper.writeValueAsString(subjectDTO)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof WrongInputException));
    }

    @Test
    public void t005_createSubjectNameNull() throws Exception {
        LoginDTO login = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login))).andExpect(status().isOk());

        SubjectDTO subjectDTO = new SubjectDTO();
        subjectDTO.setCode("ZB54");
        subjectDTO.setName(null);
        subjectDTO.setDescription("Some short description");

        this.mock.perform(post("/subject/create").contentType("application/json")
                .content(this.objMapper.writeValueAsString(subjectDTO)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof WrongInputException));
    }

    @Test
    public void t006_subscribeUserAndUnsubscribeUser() throws Exception {
        LoginDTO login = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login))).andExpect(status().isOk());

        SubjectDTO[] subjects = this.objMapper.readValue(this.mock.perform(get("/subject/list-all/").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), SubjectDTO[].class);


        UserDTO[] existingUsers = this.objMapper.readValue(this.mock.perform(get("/user/list").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), UserDTO[].class);

        this.mock.perform(post("/subject/subscribe/" + subjects[subjects.length - 1].getId() +
                "/" + existingUsers[existingUsers.length - 1].getId() + "/TEACHER").contentType("application/json")).andExpect(status().isOk());

        SubjectDTO[] subjects2 = this.objMapper.readValue(this.mock.perform(get("/subject/list/"
                +existingUsers[existingUsers.length - 1].getId()).contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), SubjectDTO[].class);

        Assertions.assertEquals(1, subjects2.length);

        // unsubscribe

        this.mock.perform(post("/subject/unsubscribe/" + subjects[subjects.length - 1].getId() +
                "/" + existingUsers[existingUsers.length - 1].getId()).contentType("application/json")).andExpect(status().isOk());

        SubjectDTO[] subjects3 = this.objMapper.readValue(this.mock.perform(get("/subject/list/"
                +existingUsers[existingUsers.length - 1].getId()).contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), SubjectDTO[].class);

        Assertions.assertEquals(0, subjects3.length);
    }

    @Test
    public void t007_editSubject() throws Exception {
        LoginDTO login = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login))).andExpect(status().isOk());

        SubjectDTO[] subjects = this.objMapper.readValue(this.mock.perform(get("/subject/list-all/").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), SubjectDTO[].class);

        // trying to change data as admin

        SubjectDTO changed = subjects[subjects.length - 1];
        changed.setCode("df56g46df5");
        changed.setName("Altered name");
        changed.setDescription("AAAAAAAAAAAAAAAAAAAAAAAAAAA");

        this.mock.perform(post("/subject/edit/"+changed.getId()).contentType("application/json")
                .content(this.objMapper.writeValueAsString(changed))).andExpect(status().isOk());

        SubjectDTO[] changedSubjects = this.objMapper.readValue(this.mock.perform(get("/subject/list-all/").contentType("application/json"))
                .andDo(mvcResult -> System.out.println(mvcResult.getResponse().getContentAsString()))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), SubjectDTO[].class);

        SubjectDTO changedSaved = changedSubjects[changedSubjects.length - 1];

        Assertions.assertEquals("df56g46df5", changedSaved.getCode());
        Assertions.assertEquals("Altered name", changedSaved.getName());
        Assertions.assertEquals("AAAAAAAAAAAAAAAAAAAAAAAAAAA", changedSaved.getDescription());
    }

    @Test
    public void t008_editSubjectWithNoPermission() throws Exception {
        // no in subject and dont have administrator permission
        LoginDTO login = new LoginDTO("subjectUser1", "Test123");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login))).andExpect(status().isOk());

        SubjectDTO[] subjects = this.objMapper.readValue(this.mock.perform(get("/subject/list-all/").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), SubjectDTO[].class);

        // trying to change data as admin

        SubjectDTO changed = subjects[subjects.length - 1];
        changed.setCode("sdfdf564sd65");
        changed.setName("Super");
        changed.setDescription("BBBBBBBBBBBBBBBBBBBBB");

        this.mock.perform(post("/subject/edit/"+changed.getId()).contentType("application/json")
                .content(this.objMapper.writeValueAsString(changed)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof PermissionException));

        SubjectDTO[] changedSubjects = this.objMapper.readValue(this.mock.perform(get("/subject/list-all/").contentType("application/json"))
                .andDo(mvcResult -> System.out.println(mvcResult.getResponse().getContentAsString()))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), SubjectDTO[].class);

        SubjectDTO changedSaved = changedSubjects[changedSubjects.length - 1];

        Assertions.assertEquals("df56g46df5", changedSaved.getCode());
        Assertions.assertEquals("Altered name", changedSaved.getName());
        Assertions.assertEquals("AAAAAAAAAAAAAAAAAAAAAAAAAAA", changedSaved.getDescription());
    }

    @Test
    public void t009_editSubjectCodeNull() throws Exception {
        LoginDTO login = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login))).andExpect(status().isOk());

        SubjectDTO[] subjects = this.objMapper.readValue(this.mock.perform(get("/subject/list-all/").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), SubjectDTO[].class);

        SubjectDTO subjectDTO = subjects[subjects.length-1];
        subjectDTO.setCode(null);

        this.mock.perform(post("/subject/edit/"+subjectDTO.getId()).contentType("application/json")
                .content(this.objMapper.writeValueAsString(subjectDTO)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof WrongInputException));
    }

    @Test
    public void t010_editSubjectNameNull() throws Exception {
        LoginDTO login = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login))).andExpect(status().isOk());

        SubjectDTO[] subjects = this.objMapper.readValue(this.mock.perform(get("/subject/list-all/").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), SubjectDTO[].class);

        SubjectDTO subjectDTO = subjects[subjects.length-1];
        subjectDTO.setName(null);

        this.mock.perform(post("/subject/edit/"+subjectDTO.getId()).contentType("application/json")
                .content(this.objMapper.writeValueAsString(subjectDTO)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof WrongInputException));
    }

}
