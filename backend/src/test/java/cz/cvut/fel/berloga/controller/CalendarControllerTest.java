package cz.cvut.fel.berloga.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.junit.jupiter.api.Assertions.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.cvut.fel.berloga.controller.dto.*;
import cz.cvut.fel.berloga.entity.enums.RecordTypeEnum;
import cz.cvut.fel.berloga.service.exceptions.DoesNotExistException;
import cz.cvut.fel.berloga.service.exceptions.PermissionException;
import cz.cvut.fel.berloga.service.exceptions.WrongInputException;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
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

import java.time.OffsetDateTime;
import java.util.Arrays;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CalendarControllerTest {

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
    public void t001_createNewRecordAsAdministrator() throws Exception {
        // Login as admin
        LoginDTO login = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login))).andExpect(status().isOk());

        // Create new record
        RecordDTO record1 = new RecordDTO(null, "record1", OffsetDateTime.now(), OffsetDateTime.now().plusYears(1), RecordTypeEnum.LECTURE, null, null);
        this.mock.perform(post("/calendar/create-record").contentType("application/json")
                .content(this.objMapper.writeValueAsString(record1))).andExpect(status().isOk());

        // Get new record
        RecordDTO[] recordList = this.objMapper.readValue(this.mock.perform(get("/calendar").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), RecordDTO[].class);
        RecordDTO record2 = recordList[recordList.length - 1];

        // Check
        assertEquals("record1", record2.getName());
    }

    @Test
    public void t002_createNewRecord() throws Exception {
        // Login as admin
        LoginDTO login1 = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login1))).andExpect(status().isOk());

        // Create new user
        InviteUserDTO invite = new InviteUserDTO("test", "test", "username18",
                "Test123", "username18@gmail.com");
        this.mock.perform(post("/user/invite").contentType("application/json")
                .content(this.objMapper.writeValueAsString(invite))).andExpect(status().isOk());

        // Logout as admin
        this.mock.perform(get("/user/logout").contentType("application/json")).andExpect(status().isOk());

        // Login as new user
        LoginDTO login2 = new LoginDTO("username18", "Test123");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login2))).andExpect(status().isOk());

        // Create new record
        RecordDTO record1 = new RecordDTO(null, "record1", OffsetDateTime.now(), OffsetDateTime.now().plusYears(1), RecordTypeEnum.LECTURE, null, null);
        this.mock.perform(post("/calendar/create-record").contentType("application/json")
                .content(this.objMapper.writeValueAsString(record1))).andExpect(status().isOk());

        // Get new record
        RecordDTO[] recordList = this.objMapper.readValue(this.mock.perform(get("/calendar").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), RecordDTO[].class);
        RecordDTO record2 = recordList[recordList.length - 1];

        // Check
        assertEquals("record1", record2.getName());
    }

    @Test
    public void t003_updateRecord() throws Exception {
        // login as admin
        LoginDTO login = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login))).andExpect(status().isOk());

        // Create new record
        RecordDTO record1 = new RecordDTO(null, "record2", OffsetDateTime.now(), OffsetDateTime.now().plusYears(1), RecordTypeEnum.LECTURE, null, null);
        this.mock.perform(post("/calendar/create-record").contentType("application/json")
                .content(this.objMapper.writeValueAsString(record1))).andExpect(status().isOk());

        // Get new record
        RecordDTO[] recordList1 = this.objMapper.readValue(this.mock.perform(get("/calendar").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), RecordDTO[].class);
        RecordDTO record = recordList1[recordList1.length - 1];

        // Update record
        RecordDTO record2 = new RecordDTO(record.getId(), "record3", OffsetDateTime.now(), OffsetDateTime.now().plusYears(2), RecordTypeEnum.SEMINAR, null, null);
        this.mock.perform(post("/calendar/update-record").contentType("application/json")
                .content(this.objMapper.writeValueAsString(record2))).andExpect(status().isOk());

        // Get new record
        RecordDTO[] recordList2 = this.objMapper.readValue(this.mock.perform(get("/calendar").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), RecordDTO[].class);

        // Check
        assertEquals("record3", recordList2[recordList2.length - 1].getName());
    }

    @Test
    public void t004_updateRecordThatDoesNotExist() throws Exception {
        // Login as admin
        LoginDTO login = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login))).andExpect(status().isOk());

        // Create new record
        RecordDTO record1 = new RecordDTO(null, "record4", OffsetDateTime.now(), OffsetDateTime.now().plusYears(1), RecordTypeEnum.LECTURE, null, null);
        this.mock.perform(post("/calendar/create-record").contentType("application/json")
                .content(this.objMapper.writeValueAsString(record1))).andExpect(status().isOk());

        // Get new record
        RecordDTO[] list = this.objMapper.readValue(this.mock.perform(get("/calendar").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), RecordDTO[].class);
        RecordDTO record = list[list.length - 1];

        // Try to update record that doesn't exist
        RecordDTO record2 = new RecordDTO(record.getId() - 1000, "record5", OffsetDateTime.now(), OffsetDateTime.now().plusYears(2), RecordTypeEnum.SEMINAR, null, null);
        this.mock.perform(post("/calendar/update-record").contentType("application/json").content(this.objMapper.writeValueAsString(record2)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof DoesNotExistException));
    }

    @Test
    public void t005_updateRecordWhenYouAreNotOwner() throws Exception {
        // Login as admin
        LoginDTO login1 = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login1))).andExpect(status().isOk());

        // Create new user
        InviteUserDTO invite = new InviteUserDTO("test", "test", "username19",
                "Test123", "username19@gmail.com");
        this.mock.perform(post("/user/invite").contentType("application/json")
                .content(this.objMapper.writeValueAsString(invite))).andExpect(status().isOk());

        // Create new record
        RecordDTO record1 = new RecordDTO(null, "record6", OffsetDateTime.now(), OffsetDateTime.now().plusYears(1), RecordTypeEnum.LECTURE, null, null);
        this.mock.perform(post("/calendar/create-record").contentType("application/json")
                .content(this.objMapper.writeValueAsString(record1))).andExpect(status().isOk());

        // Get new record
        RecordDTO[] list = this.objMapper.readValue(this.mock.perform(get("/calendar").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), RecordDTO[].class);
        RecordDTO record = list[list.length - 1];

        // Logout as admin
        this.mock.perform(get("/user/logout").contentType("application/json")).andExpect(status().isOk());

        // Login as new user
        LoginDTO login2 = new LoginDTO("username19", "Test123");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login2))).andExpect(status().isOk());

        // Try to update record with no permission
        RecordDTO record2 = new RecordDTO(record.getId(), "record7", OffsetDateTime.now(), OffsetDateTime.now().plusYears(2), RecordTypeEnum.SEMINAR, null, null);
        this.mock.perform(post("/calendar/update-record").contentType("application/json").content(this.objMapper.writeValueAsString(record2)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof PermissionException));
    }

    @Test
    public void t006_updateRecordWithWrongDates() throws Exception {
        // Login as admin
        LoginDTO login = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login))).andExpect(status().isOk());

        // Create new record
        RecordDTO record1 = new RecordDTO(null, "record8", OffsetDateTime.now(), OffsetDateTime.now().plusYears(1), RecordTypeEnum.LECTURE, null, null);
        this.mock.perform(post("/calendar/create-record").contentType("application/json")
                .content(this.objMapper.writeValueAsString(record1))).andExpect(status().isOk());

        // Get new record
        RecordDTO[] list = this.objMapper.readValue(this.mock.perform(get("/calendar").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), RecordDTO[].class);
        RecordDTO record = list[list.length - 1];

        // Try to update record with wrong dates
        RecordDTO record2 = new RecordDTO(record.getId(), "record9", OffsetDateTime.now().plusYears(2), OffsetDateTime.now(), RecordTypeEnum.SEMINAR, null, null);
        this.mock.perform(post("/calendar/update-record").contentType("application/json").content(this.objMapper.writeValueAsString(record2)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof WrongInputException));
    }

    @Test
    public void t007_updateRecordWithWrongSubject() throws Exception {
        // Login as admin
        LoginDTO login = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login))).andExpect(status().isOk());

        // Create new subject
        SubjectDTO subjectDTO = SubjectDTO.builder()
                .code("MIR54J")
                .name("Miracle")
                .description("Some short description")
                .build();
        this.mock.perform(post("/subject/create").contentType("application/json")
                .content(this.objMapper.writeValueAsString(subjectDTO))).andExpect(status().isOk());

        // Get new subject
        SubjectDTO[] subjects = this.objMapper.readValue(this.mock.perform(get("/subject/list-all/").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), SubjectDTO[].class);
        SubjectDTO subject = subjects[subjects.length - 1];

        // Create new record
        RecordDTO record1 = new RecordDTO(null, "record10", OffsetDateTime.now(), OffsetDateTime.now().plusYears(1), RecordTypeEnum.LECTURE, subject.getId(), null);
        this.mock.perform(post("/calendar/create-record").contentType("application/json")
                .content(this.objMapper.writeValueAsString(record1))).andExpect(status().isOk());

        // Get new record
        RecordDTO[] list = this.objMapper.readValue(this.mock.perform(get("/calendar").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), RecordDTO[].class);
        RecordDTO record = list[list.length - 1];

        // Try to update record with wrong subject
        RecordDTO record2 = new RecordDTO(record.getId(), "record11", OffsetDateTime.now(), OffsetDateTime.now().plusYears(2), RecordTypeEnum.SEMINAR, subject.getId() - 1000, null);
        this.mock.perform(post("/calendar/update-record").contentType("application/json").content(this.objMapper.writeValueAsString(record2)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof DoesNotExistException));
    }

    @Test
    public void t008_removeRecord() throws Exception {
        // Login as admin
        LoginDTO login = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login))).andExpect(status().isOk());

        // Create new record
        RecordDTO record1 = new RecordDTO(null, "record12", OffsetDateTime.now(), OffsetDateTime.now().plusYears(1), RecordTypeEnum.LECTURE, null, null);
        this.mock.perform(post("/calendar/create-record").contentType("application/json")
                .content(this.objMapper.writeValueAsString(record1))).andExpect(status().isOk());

        // Get new record
        RecordDTO[] recordList1 = this.objMapper.readValue(this.mock.perform(get("/calendar").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), RecordDTO[].class);
        RecordDTO record = recordList1[recordList1.length - 1];

        // Delete record
        this.mock.perform(delete("/calendar/remove-record/" + record.getId()).contentType("application/json")
                .content(this.objMapper.writeValueAsString(record))).andExpect(status().isOk());

        // Get name of deleted record
        RecordDTO[] recordList2 = this.objMapper.readValue(this.mock.perform(get("/calendar").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), RecordDTO[].class);
        RecordDTO record2 = Arrays.stream(recordList2)
                .filter(r -> r.getName().equals("record12"))
                .findFirst().orElse(null);

        // Check
        assertNull(record2);
    }

    @Test
    public void t009_removeRecordThatDoesNotExist() throws Exception {
        // Login as admin
        LoginDTO login = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login))).andExpect(status().isOk());

        // Create new record
        RecordDTO record1 = new RecordDTO(null, "record13", OffsetDateTime.now(), OffsetDateTime.now().plusYears(1), RecordTypeEnum.LECTURE, null, null);
        this.mock.perform(post("/calendar/create-record").contentType("application/json")
                .content(this.objMapper.writeValueAsString(record1))).andExpect(status().isOk());

        // Get new record
        RecordDTO[] list = this.objMapper.readValue(this.mock.perform(get("/calendar").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), RecordDTO[].class);
        RecordDTO record = list[list.length - 1];

        // Try to delete record that doesn't exist
        this.mock.perform(delete("/calendar/remove-record/" + (record.getId() -1000)).contentType("application/json").content(this.objMapper.writeValueAsString(record)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof DoesNotExistException));
    }

    @Test
    public void t010_deleteRecordWhenYouAreNotOwner() throws Exception {
        // Login as admin
        LoginDTO login1 = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login1))).andExpect(status().isOk());

        // Create new user
        InviteUserDTO invite = new InviteUserDTO("test", "test", "username20",
                "Test123", "username20@gmail.com");
        this.mock.perform(post("/user/invite").contentType("application/json")
                .content(this.objMapper.writeValueAsString(invite))).andExpect(status().isOk());

        // Create new record
        RecordDTO record1 = new RecordDTO(null, "record14", OffsetDateTime.now(), OffsetDateTime.now().plusYears(1), RecordTypeEnum.LECTURE, null, null);
        this.mock.perform(post("/calendar/create-record").contentType("application/json")
                .content(this.objMapper.writeValueAsString(record1))).andExpect(status().isOk());

        // Get new record
        RecordDTO[] list = this.objMapper.readValue(this.mock.perform(get("/calendar").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), RecordDTO[].class);
        RecordDTO record = list[list.length - 1];

        // Logout as admin
        this.mock.perform(get("/user/logout").contentType("application/json")).andExpect(status().isOk());

        // Login as new user
        LoginDTO login2 = new LoginDTO("username20", "Test123");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login2))).andExpect(status().isOk());

        // Try to delete record that user doesn't own
        this.mock.perform(delete("/calendar/remove-record/" + record.getId()).contentType("application/json").content(this.objMapper.writeValueAsString(record)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof PermissionException));
    }

}
