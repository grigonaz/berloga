package cz.cvut.fel.berloga.process;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.cvut.fel.berloga.controller.dto.*;
import cz.cvut.fel.berloga.entity.enums.RecordTypeEnum;
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

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ProcessTests {

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
    public void t001_loginAddChatWriteMessageDeleteMessage() throws Exception {
        // Login
        LoginDTO login = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login))).andExpect(status().isOk());

        // Create chat
        ChatDTO chat = new ChatDTO(null, "testchat1");
        this.mock.perform(post("/chat/create").contentType("application/json")
                .content(this.objMapper.writeValueAsString(chat))).andExpect(status().isOk());

        // Get chat
        ChatDTO[] chatList = this.objMapper.readValue(this.mock.perform(get("/chat/all").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), ChatDTO[].class);
        ChatDTO chat2 = chatList[chatList.length - 1];

        // Get user
        UserDTO[] userList = this.objMapper.readValue(this.mock.perform(get("/user/list").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), UserDTO[].class);
        UserDTO admin = userList[0];

        // Send messages
        MessageDTO message1 = new MessageDTO(null, "message1", null, null,  admin, OffsetDateTime.now());
        this.mock.perform(post("/chat/" + chat2.getChatId() + "/send-message").contentType("application/json")
                .content(this.objMapper.writeValueAsString(message1))).andExpect(status().isOk());

        MessageDTO message2 = new MessageDTO(null, "message2", null,null,  admin, OffsetDateTime.now());
        this.mock.perform(post("/chat/" + chat2.getChatId() + "/send-message").contentType("application/json")
                .content(this.objMapper.writeValueAsString(message2))).andExpect(status().isOk());

        // Get last message
        ChatReturnDTO chatReturn1 = this.objMapper.readValue(this.mock.perform(get("/chat/" + chat2.getChatId()).contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), ChatReturnDTO.class);
        MessageDTO message3 = chatReturn1.getMessages().get(chatReturn1.getMessages().size() - 1);

        // Remove message
        this.mock.perform(delete("/chat/remove/" + message3.getId()).contentType("application/json"))
                .andExpect(status().isOk());

        // Check message
        ChatReturnDTO chatReturn2 = this.objMapper.readValue(this.mock.perform(get("/chat/" + chat2.getChatId()).contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), ChatReturnDTO.class);
        assertEquals(1, chatReturn2.getMessages().size());
    }

    @Test
    public void t002_loginAddChatAddMemberDeleteMember() throws Exception {
        // Login
        LoginDTO login = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login))).andExpect(status().isOk());

        // Create chat
        ChatDTO chat = new ChatDTO(null, "testchat2");
        this.mock.perform(post("/chat/create").contentType("application/json")
                .content(this.objMapper.writeValueAsString(chat))).andExpect(status().isOk());

        // Get chat
        ChatDTO[] chatList1 = this.objMapper.readValue(this.mock.perform(get("/chat/all").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), ChatDTO[].class);
        ChatDTO chat2 = chatList1[chatList1.length - 1];

        // Get user data of "anona"
        UserDTO[] userList = this.objMapper.readValue(this.mock.perform(get("/user/list").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), UserDTO[].class);
        UserDTO user = userList[userList.length - 1];

        // Add "anona" to chat
        this.mock.perform(post("/chat/" + chat2.getChatId() + "/add-member/" + user.getId()).contentType("application/json"))
                .andExpect(status().isOk());

        // Remove "anona" from chat
        this.mock.perform(delete("/chat/" + chat2.getChatId() + "/remove-member/" + user.getId()).contentType("application/json"))
                .andExpect(status().isOk());

        // Check deletion
        ChatReturnDTO chatReturn = this.objMapper.readValue(this.mock.perform(get("/chat/" + chat2.getChatId()).contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), ChatReturnDTO.class);
        assertEquals(1, chatReturn.getUsers().size());
    }

    @Test
    public void t003_loginAddChatAddMemberReloginToMemberSendMessage() throws Exception {
        // Login as admin
        LoginDTO login1 = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login1))).andExpect(status().isOk());

        // Create chat
        ChatDTO chat = new ChatDTO(null, "testchat3");
        this.mock.perform(post("/chat/create").contentType("application/json")
                .content(this.objMapper.writeValueAsString(chat))).andExpect(status().isOk());

        // Create user
        InviteUserDTO invite = new InviteUserDTO("test", "test", "username21",
                "Test123", "username21@gmail.com");
        this.mock.perform(post("/user/invite").contentType("application/json")
                .content(this.objMapper.writeValueAsString(invite))).andExpect(status().isOk());

        // Get chat
        ChatDTO[] chatList = this.objMapper.readValue(this.mock.perform(get("/chat/all").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), ChatDTO[].class);
        ChatDTO chat2 = chatList[chatList.length - 1];

        // Get user
        UserDTO[] userList = this.objMapper.readValue(this.mock.perform(get("/user/list").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), UserDTO[].class);
        UserDTO user = userList[userList.length - 1];

        // Add "anona" to chat
        this.mock.perform(post("/chat/" + chat2.getChatId() + "/add-member/" + user.getId()).contentType("application/json"))
                .andExpect(status().isOk());

        // Logout as admin
        this.mock.perform(get("/user/logout").contentType("application/json")).andExpect(status().isOk());

        // Login as new user
        LoginDTO login2 = new LoginDTO("username21", "Test123");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login2))).andExpect(status().isOk());

        // Send messages
        MessageDTO message1 = new MessageDTO(null, "message1", null,null,  user, OffsetDateTime.now());
        this.mock.perform(post("/chat/" + chat2.getChatId() + "/send-message").contentType("application/json")
                .content(this.objMapper.writeValueAsString(message1))).andExpect(status().isOk());

        MessageDTO message2 = new MessageDTO(null, "message2", null, null, user, OffsetDateTime.now());
        this.mock.perform(post("/chat/" + chat2.getChatId() + "/send-message").contentType("application/json")
                .content(this.objMapper.writeValueAsString(message2))).andExpect(status().isOk());

        // Get last message
        ChatReturnDTO chatReturn = this.objMapper.readValue(this.mock.perform(get("/chat/" + chat2.getChatId()).contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), ChatReturnDTO.class);
        MessageDTO message3 = chatReturn.getMessages().get(chatReturn.getMessages().size() - 1);

        // Check message
        assertEquals("message2", message3.getContent());
    }

    @Test
    public void t004_loginCreateChangeRemoveRecord() throws Exception {
        // Login as admin
        LoginDTO login1 = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login1))).andExpect(status().isOk());

        // Create new user
        InviteUserDTO invite = new InviteUserDTO("test", "test", "username22",
                "Test123", "username22@gmail.com");
        this.mock.perform(post("/user/invite").contentType("application/json")
                .content(this.objMapper.writeValueAsString(invite))).andExpect(status().isOk());

        // Logout as admin
        this.mock.perform(get("/user/logout").contentType("application/json")).andExpect(status().isOk());

        // Login as new user
        LoginDTO login2 = new LoginDTO("username22", "Test123");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login2))).andExpect(status().isOk());

        // Create record
        RecordDTO record = new RecordDTO(null, "record15", OffsetDateTime.now(), OffsetDateTime.now().plusYears(1), RecordTypeEnum.LECTURE, null, null);
        this.mock.perform(post("/calendar/create-record").contentType("application/json")
                .content(this.objMapper.writeValueAsString(record))).andExpect(status().isOk());

        // Get last record
        RecordDTO[] recordList1 = this.objMapper.readValue(this.mock.perform(get("/calendar").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), RecordDTO[].class);
        RecordDTO record2 = recordList1[recordList1.length - 1];
        Long id = record2.getId();

        // Edit record
        RecordDTO record3 = new RecordDTO(record2.getId(), "record16", OffsetDateTime.now(), OffsetDateTime.now().plusYears(2), RecordTypeEnum.SEMINAR, null, null);
        this.mock.perform(post("/calendar/update-record").contentType("application/json")
                .content(this.objMapper.writeValueAsString(record3))).andExpect(status().isOk());

        // Get last record
        RecordDTO record4 = recordList1[recordList1.length - 1];

        // Remove record
        this.mock.perform(delete("/calendar/remove-record/" + record4.getId()).contentType("application/json")
                .content(this.objMapper.writeValueAsString(record4))).andExpect(status().isOk());

        // Check deletion
        RecordDTO[] recordList2 = this.objMapper.readValue(this.mock.perform(get("/calendar").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), RecordDTO[].class);
        assertEquals(0, recordList2.length);
    }

    // Elasticsearch needed
    @Test
    public void t005_loginAddQuestionMarkAsResolvedReloginAddComment() throws Exception {
        // Login
        LoginDTO login = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login))).andExpect(status().isOk());

        // Create question
        QuestionForumDTO question = QuestionForumDTO.builder().question("question").build();
        this.mock.perform(post("/forum/create-question").contentType("application/json")
                .content(this.objMapper.writeValueAsString(question))).andExpect(status().isOk());

        // Get last record
        QuestionForumDTO[] questionList = this.objMapper.readValue(this.mock.perform(get("/forum/all").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), QuestionForumDTO[].class);
        QuestionForumDTO question1 = questionList[questionList.length - 1];
        assertNotNull(question1);

        // Marking as resolved
        this.mock.perform(post("/forum/mark-done/" + question1.getId()).contentType("application/json"))
                .andExpect(status().isOk());

        // Get last record
        QuestionForumDTO[] questionList2 = this.objMapper.readValue(this.mock.perform(get("/forum/all").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), QuestionForumDTO[].class);
        QuestionForumDTO question2 = questionList2[questionList2.length - 1];
        assertTrue(question2.isDone());

        // Logout
        this.mock.perform(get("/user/logout").contentType("application/json")).andExpect(status().isOk());

        // Login as new user
        login = new LoginDTO("second", "second");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login))).andExpect(status().isOk());

        // Get last record
        questionList2 = this.objMapper.readValue(this.mock.perform(get("/forum/all").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), QuestionForumDTO[].class);
        question2 = questionList2[questionList2.length - 1];

        assertTrue(question2.isDone());
        assertEquals(0, question2.getComments().size());

        // Create comment
        CommentDTO comment = CommentDTO.builder().content("aaa").questionId(question2.getId()).build();
        this.mock.perform(post("/forum/comment-question").contentType("application/json")
                .content(this.objMapper.writeValueAsString(comment))).andExpect(status().isOk());

        questionList2 = this.objMapper.readValue(this.mock.perform(get("/forum/all").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), QuestionForumDTO[].class);
        question2 = questionList2[questionList2.length - 1];
        assertEquals(1, question2.getComments().size());
    }

    // Elasticsearch needed
    @Test
    public void t006_loginAddQuestionRenameQuestionSearchByName() throws Exception {
        // Login
        LoginDTO login = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login))).andExpect(status().isOk());

        // Create question
        QuestionForumDTO question = QuestionForumDTO.builder().question("question").build();
        this.mock.perform(post("/forum/create-question").contentType("application/json")
                .content(this.objMapper.writeValueAsString(question))).andExpect(status().isOk());

        // Get last record
        QuestionForumDTO[] questionList = this.objMapper.readValue(this.mock.perform(get("/forum/all").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), QuestionForumDTO[].class);
        QuestionForumDTO question1 = questionList[questionList.length - 1];

        // Change the question
        QuestionForumDTO questionEdited = QuestionForumDTO.builder().question("changed").id(question1.getId()).build();
        this.mock.perform(post("/forum/edit-question").contentType("application/json")
                .content(this.objMapper.writeValueAsString(questionEdited))).andExpect(status().isOk());

        // Search for the question
        String searchString = "changed";
        questionList = this.objMapper.readValue(this.mock.perform(post("/forum/all-search").contentType("application/json")
                .content(this.objMapper.writeValueAsString(searchString)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), QuestionForumDTO[].class);

        // Check
        assertEquals(1, questionList.length);
    }

    @Test
    public void t007_createModeratorThenCreateSubjectAndSubscribeUserToIt() throws Exception {
        // Login as admin
        LoginDTO login1 = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login1))).andExpect(status().isOk());

        // Create new users
        InviteUserDTO invite1 = new InviteUserDTO("test", "test", "username23",
                "Test123", "username23@gmail.com");
        this.mock.perform(post("/user/invite").contentType("application/json")
                .content(this.objMapper.writeValueAsString(invite1))).andExpect(status().isOk());

        InviteUserDTO invite2 = new InviteUserDTO("test", "test", "username24",
                "Test123", "username24@gmail.com");
        this.mock.perform(post("/user/invite").contentType("application/json")
                .content(this.objMapper.writeValueAsString(invite2))).andExpect(status().isOk());

        // Get new user
        UserDTO[] userList = this.objMapper.readValue(this.mock.perform(get("/user/list").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), UserDTO[].class);
        UserDTO user2 = userList[userList.length - 1];

        // Set new role for new user
        this.mock.perform(post("/user/add-role/" + user2.getId() + "/MODERATOR").contentType("application/json"))
                .andExpect(status().isOk());

        // Logout as admin
        this.mock.perform(get("/user/logout").contentType("application/json")).andExpect(status().isOk());

        // Login as new user
        LoginDTO login2 = new LoginDTO("username24", "Test123");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login2))).andExpect(status().isOk());

        // Create subject
        SubjectDTO subject1 = SubjectDTO.builder()
                .code("MIR65J")
                .name("Wonderful")
                .description("Better short description")
                .build();

        this.mock.perform(post("/subject/create").contentType("application/json")
                .content(this.objMapper.writeValueAsString(subject1))).andExpect(status().isOk());

        // Get subject
        SubjectDTO[] subjects1 = this.objMapper.readValue(this.mock.perform(get("/subject/list-all/").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), SubjectDTO[].class);
        SubjectDTO subject2 = subjects1[subjects1.length - 1];

        // Get user
        UserDTO user1 = userList[userList.length - 2];

        // Subscribe new user to subject
        this.mock.perform(post("/subject/subscribe/" + subject2.getId() + "/" + user1.getId() + "/TEACHER").contentType("application/json"))
                .andExpect(status().isOk());

        // Get user subjects
        SubjectDTO[] subjects2 = this.objMapper.readValue(this.mock.perform(get("/subject/list/" + user1.getId()).contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), SubjectDTO[].class);

        // Check subscription
        assertEquals(subjects1[subjects1.length - 1], subjects2[0]);
    }

    @Test
    public void t008_loginAddChatAddMemberRenameChatAndFindByName() throws Exception {
        // Login as admin
        LoginDTO login = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login))).andExpect(status().isOk());

        // Create new users
        InviteUserDTO invite = new InviteUserDTO("test", "test", "username25",
                "Test123", "username25@gmail.com");
        this.mock.perform(post("/user/invite").contentType("application/json")
                .content(this.objMapper.writeValueAsString(invite))).andExpect(status().isOk());

        // Get new user
        UserDTO[] userList = this.objMapper.readValue(this.mock.perform(get("/user/list").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), UserDTO[].class);
        UserDTO user = userList[userList.length - 1];

        // Create chat
        ChatDTO chat = new ChatDTO(null, "testchat4");
        this.mock.perform(post("/chat/create").contentType("application/json")
                .content(this.objMapper.writeValueAsString(chat))).andExpect(status().isOk());

        // Get chat
        ChatDTO[] chatList = this.objMapper.readValue(this.mock.perform(get("/chat/all").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), ChatDTO[].class);
        ChatDTO chat2 = chatList[chatList.length - 1];

        // Add new user to chat
        this.mock.perform(post("/chat/" + chat2.getChatId() + "/add-member/" + user.getId()).contentType("application/json"))
                .andExpect(status().isOk());

        // Rename chat
        chat2.setChatName("notestchat4");
        this.mock.perform(post("/chat/edit-chat").contentType("application/json")
                .content(this.objMapper.writeValueAsString(chat2))).andExpect(status().isOk());

        // Check new name
        assertEquals(chatList[chatList.length - 1].getChatName(), "notestchat4");
    }

}
