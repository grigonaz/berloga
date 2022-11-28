package cz.cvut.fel.berloga.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.cvut.fel.berloga.controller.dto.*;
import cz.cvut.fel.berloga.service.exceptions.DoesNotExistException;
import cz.cvut.fel.berloga.service.exceptions.PermissionException;
import cz.cvut.fel.berloga.service.exceptions.UserAlreadyInChatException;
import cz.cvut.fel.berloga.service.exceptions.WrongMessageException;
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

public class ChatControllerTest {

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
    public void t001_createChat() throws Exception {
        //login as admin
        LoginDTO login = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login))).andExpect(status().isOk());

        //get number of chats before
        int size1 = this.objMapper.readValue(this.mock.perform(get("/chat/all").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), ChatDTO[].class).length;

        //create chat
        ChatDTO chat = new ChatDTO(null, "test100");

        this.mock.perform(post("/chat/create").contentType("application/json")
                .content(this.objMapper.writeValueAsString(chat))).andExpect(status().isOk());

        //get number of chats after
        int size2 = this.objMapper.readValue(this.mock.perform(get("/chat/all").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), ChatDTO[].class).length;

        assertEquals(size1 + 1, size2);
    }

    @Test
    public void t002_editChat() throws Exception {
        //login as admin
        LoginDTO login = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login))).andExpect(status().isOk());

        //create chat
        ChatDTO chat = new ChatDTO(null, "test100");

        this.mock.perform(post("/chat/create").contentType("application/json")
                .content(this.objMapper.writeValueAsString(chat))).andExpect(status().isOk());

        //get previously created chat from list
        ChatDTO[] chatList2 = this.objMapper.readValue(this.mock.perform(get("/chat/all").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), ChatDTO[].class);

        ChatDTO chat2 = chatList2[chatList2.length - 1];

        //edit chat
        chat2.setChatName("test101");

        this.mock.perform(post("/chat/edit-chat").contentType("application/json")
                .content(this.objMapper.writeValueAsString(chat2))).andExpect(status().isOk());

        //get previously created chat from list after edit
        ChatDTO[] chatList3 = this.objMapper.readValue(this.mock.perform(get("/chat/all").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), ChatDTO[].class);

        ChatDTO chat3 = chatList3[chatList3.length - 1];

        assertEquals("test101", chat3.getChatName());
    }

    @Test
    public void t003_editChatAsNotOwner() throws Exception {
        //login as admin
        LoginDTO login = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login))).andExpect(status().isOk());

        //create chat
        ChatDTO chat = new ChatDTO(null, "test100");

        this.mock.perform(post("/chat/create").contentType("application/json")
                .content(this.objMapper.writeValueAsString(chat))).andExpect(status().isOk());

        //get UserDTO of second
        UserDTO[] list = this.objMapper.readValue(this.mock.perform(get("/user/list").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), UserDTO[].class);

        UserDTO user = list[list.length - 2];

        //get previously created chat from list
        ChatDTO[] chatList2 = this.objMapper.readValue(this.mock.perform(get("/chat/all").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), ChatDTO[].class);

        ChatDTO chat2 = chatList2[chatList2.length - 1];

        //add second to chat
        this.mock.perform(post("/chat/" + chat2.getChatId() + "/add-member/" + user.getId()).contentType("application/json"))
                .andExpect(status().isOk());

        //get previously created chat from list
        ChatDTO[] chatList3 = this.objMapper.readValue(this.mock.perform(get("/chat/all").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), ChatDTO[].class);

        ChatDTO chat3 = chatList3[chatList3.length - 1];

        //logout as admin
        this.mock.perform(get("/user/logout").contentType("application/json")).andExpect(status().isOk());

        //login as second
        LoginDTO login2 = new LoginDTO("second", "second");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login2))).andExpect(status().isOk());

        //change chat name as second
        chat2.setChatName("test101");

        this.mock.perform(post("/chat/edit-chat").contentType("application/json")
                .content(this.objMapper.writeValueAsString(chat3)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof PermissionException));
    }

    @Test
    public void t004_removeChat() throws Exception {
        //login as admin
        LoginDTO login = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login))).andExpect(status().isOk());

        //create chat
        ChatDTO chat = new ChatDTO(null, "test100");

        this.mock.perform(post("/chat/create").contentType("application/json")
                .content(this.objMapper.writeValueAsString(chat))).andExpect(status().isOk());

        //get number of chats before
        int size1 = this.objMapper.readValue(this.mock.perform(get("/chat/all").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), ChatDTO[].class).length;

        //get previously created chat from list
        ChatDTO[] chatList2 = this.objMapper.readValue(this.mock.perform(get("/chat/all").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), ChatDTO[].class);

        ChatDTO chat2 = chatList2[chatList2.length - 1];

        //delete chat
        this.mock.perform(delete("/chat/delete/" + chat2.getChatId()).contentType("application/json"))
                .andExpect(status().isOk());

        //get number of chats after
        int size2 = this.objMapper.readValue(this.mock.perform(get("/chat/all").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), ChatDTO[].class).length;

        assertEquals(size1 - 1, size2);

    }

    @Test
    public void t005_removeChatWhichNotExist() throws Exception {
        //login as admin
        LoginDTO login = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login))).andExpect(status().isOk());

        //create chat
        ChatDTO chat = new ChatDTO(null, "test100");

        this.mock.perform(post("/chat/create").contentType("application/json")
                .content(this.objMapper.writeValueAsString(chat))).andExpect(status().isOk());

        //get previously created chat from list
        ChatDTO[] chatList2 = this.objMapper.readValue(this.mock.perform(get("/chat/all").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), ChatDTO[].class);

        ChatDTO chat2 = chatList2[chatList2.length - 1];

        //delete chat
        this.mock.perform(delete("/chat/delete/" + (chat2.getChatId() - 1000)).contentType("application/json"))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof DoesNotExistException));

    }

    @Test
    public void t006_removeChatAsNotOwner() throws Exception {
        //login as admin
        LoginDTO login = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login))).andExpect(status().isOk());

        //create chat
        ChatDTO chat = new ChatDTO(null, "test100");

        this.mock.perform(post("/chat/create").contentType("application/json")
                .content(this.objMapper.writeValueAsString(chat))).andExpect(status().isOk());

        //get UserDTO of second
        UserDTO[] list = this.objMapper.readValue(this.mock.perform(get("/user/list").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), UserDTO[].class);

        UserDTO user = list[list.length - 2];

        //get previously created chat from list
        ChatDTO[] chatList2 = this.objMapper.readValue(this.mock.perform(get("/chat/all").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), ChatDTO[].class);

        ChatDTO chat2 = chatList2[chatList2.length - 1];

        //add second to chat
        this.mock.perform(post("/chat/" + chat2.getChatId() + "/add-member/" + user.getId()).contentType("application/json"))
                .andExpect(status().isOk());

        //logout as admin
        this.mock.perform(get("/user/logout").contentType("application/json"))
                .andExpect(status().isOk());

        //login as second
        LoginDTO login2 = new LoginDTO("second", "second");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login2))).andExpect(status().isOk());

        //delete chat
        this.mock.perform(delete("/chat/delete/" + chat2.getChatId()).contentType("application/json"))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof PermissionException));

    }

    @Test
    public void t007_addMember() throws Exception {
        //login as admin
        LoginDTO login = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login))).andExpect(status().isOk());

        //create chat
        ChatDTO chat = new ChatDTO(null, "test100");

        this.mock.perform(post("/chat/create").contentType("application/json")
                .content(this.objMapper.writeValueAsString(chat))).andExpect(status().isOk());

        //get UserDTO of anona
        UserDTO[] list = this.objMapper.readValue(this.mock.perform(get("/user/list").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), UserDTO[].class);

        UserDTO user = list[list.length - 1];

        //get previously created chat from list
        ChatDTO[] chatList2 = this.objMapper.readValue(this.mock.perform(get("/chat/all").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), ChatDTO[].class);

        ChatDTO chat2 = chatList2[chatList2.length - 1];

        //get number of users not in chat before
        int size1 = this.objMapper.readValue(this.mock.perform(get("/chat/find-user-not-in-chat/" + chat2.getChatId()).contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), UserDTO[].class).length;

        //add anona to chat
        this.mock.perform(post("/chat/" + chat2.getChatId() + "/add-member/" + user.getId()).contentType("application/json"))
                .andExpect(status().isOk());

        //get number of users not in chat after
        int size2 = this.objMapper.readValue(this.mock.perform(get("/chat/find-user-not-in-chat/" + chat2.getChatId()).contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), UserDTO[].class).length;

        assertEquals(size1 - 1, size2);
    }

    @Test
    public void t008_addMemberAsNotOwner() throws Exception {
        //login as admin
        LoginDTO login = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login))).andExpect(status().isOk());

        //create chat
        ChatDTO chat = new ChatDTO(null, "test100");

        this.mock.perform(post("/chat/create").contentType("application/json")
                .content(this.objMapper.writeValueAsString(chat))).andExpect(status().isOk());

        //get previously created chat from list
        ChatDTO[] chatList2 = this.objMapper.readValue(this.mock.perform(get("/chat/all").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), ChatDTO[].class);

        ChatDTO chat2 = chatList2[chatList2.length - 1];

        //logout as admin
        this.mock.perform(get("/user/logout").contentType("application/json")).andExpect(status().isOk());

        //login as second
        LoginDTO login2 = new LoginDTO("second", "second");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login2))).andExpect(status().isOk());

        //get UserDTO of anona
        UserDTO[] list = this.objMapper.readValue(this.mock.perform(get("/user/list").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), UserDTO[].class);

        UserDTO user = list[list.length - 1];

        //add member anona as second
        this.mock.perform(post("/chat/" + chat2.getChatId() + "/add-member/" + user.getId()).contentType("application/json"))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof PermissionException));

    }

    @Test
    public void t009_addMemberWhichNotExist() throws Exception {
        //login as admin
        LoginDTO login = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login))).andExpect(status().isOk());

        //create chat
        ChatDTO chat = new ChatDTO(null, "test100");

        this.mock.perform(post("/chat/create").contentType("application/json")
                .content(this.objMapper.writeValueAsString(chat))).andExpect(status().isOk());

        //get previously created chat from list
        ChatDTO[] chatList2 = this.objMapper.readValue(this.mock.perform(get("/chat/all").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), ChatDTO[].class);

        ChatDTO chat2 = chatList2[chatList2.length - 1];

        //add not existing user to chat
        this.mock.perform(post("/chat/" + chat2.getChatId() + "/add-member/" + 75893273245L).contentType("application/json"))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof DoesNotExistException));
    }

    @Test
    public void t010_addMemberAlreadyInChat() throws Exception {
        //login as admin
        LoginDTO login = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login))).andExpect(status().isOk());

        //create chat
        ChatDTO chat = new ChatDTO(null, "test100");

        this.mock.perform(post("/chat/create").contentType("application/json")
                .content(this.objMapper.writeValueAsString(chat))).andExpect(status().isOk());

        //get UserDTO of anona
        UserDTO[] list = this.objMapper.readValue(this.mock.perform(get("/user/list").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), UserDTO[].class);

        UserDTO user = list[list.length - 1];

        //get previously created chat from list
        ChatDTO[] chatList2 = this.objMapper.readValue(this.mock.perform(get("/chat/all").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), ChatDTO[].class);

        ChatDTO chat2 = chatList2[chatList2.length - 1];

        //add member anona
        this.mock.perform(post("/chat/" + chat2.getChatId() + "/add-member/" + user.getId()).contentType("application/json"))
                .andExpect(status().isOk());

        //add member anona 2nd time
        this.mock.perform(post("/chat/" + chat2.getChatId() + "/add-member/" + user.getId()).contentType("application/json"))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof UserAlreadyInChatException));

    }

    @Test
    public void t011_removeMember() throws Exception {
        //login as admin
        LoginDTO login = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login))).andExpect(status().isOk());

        //create chat
        ChatDTO chat = new ChatDTO(null, "test100");

        this.mock.perform(post("/chat/create").contentType("application/json")
                .content(this.objMapper.writeValueAsString(chat))).andExpect(status().isOk());

        //get UserDTO of anona
        UserDTO[] list = this.objMapper.readValue(this.mock.perform(get("/user/list").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), UserDTO[].class);

        UserDTO user = list[list.length - 1];

        //get previously created chat from list
        ChatDTO[] chatList2 = this.objMapper.readValue(this.mock.perform(get("/chat/all").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), ChatDTO[].class);

        ChatDTO chat2 = chatList2[chatList2.length - 1];

        //add anona to chat
        this.mock.perform(post("/chat/" + chat2.getChatId() + "/add-member/" + user.getId()).contentType("application/json"))
                .andExpect(status().isOk());

        //get number of users not in chat before
        int size1 = this.objMapper.readValue(this.mock.perform(get("/chat/find-user-not-in-chat/" + chat2.getChatId()).contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), UserDTO[].class).length;

        //remove anona from chat
        this.mock.perform(delete("/chat/" + chat2.getChatId() + "/remove-member/" + user.getId()).contentType("application/json"))
                .andExpect(status().isOk());

        //get number of users not in chat after
        int size2 = this.objMapper.readValue(this.mock.perform(get("/chat/find-user-not-in-chat/" + chat2.getChatId()).contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), UserDTO[].class).length;

        assertEquals(size1 + 1, size2);

    }

    @Test
    public void t012_removeMemberAsNotOwnerAndNotMyself() throws Exception {
        //login as admin
        LoginDTO login = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login))).andExpect(status().isOk());

        //create chat
        ChatDTO chat = new ChatDTO(null, "test100");

        this.mock.perform(post("/chat/create").contentType("application/json")
                .content(this.objMapper.writeValueAsString(chat))).andExpect(status().isOk());

        //get UserDTO of anona
        UserDTO[] list = this.objMapper.readValue(this.mock.perform(get("/user/list").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), UserDTO[].class);

        UserDTO user = list[list.length - 1];

        //get UserDTO of second
        UserDTO user2 = list[list.length - 2];

        //get previously created chat from list
        ChatDTO[] chatList2 = this.objMapper.readValue(this.mock.perform(get("/chat/all").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), ChatDTO[].class);

        ChatDTO chat2 = chatList2[chatList2.length - 1];

        //add anona to chat
        this.mock.perform(post("/chat/" + chat2.getChatId() + "/add-member/" + user.getId()).contentType("application/json"))
                .andExpect(status().isOk());

        //add second to chat
        this.mock.perform(post("/chat/" + chat2.getChatId() + "/add-member/" + user2.getId()).contentType("application/json"))
                .andExpect(status().isOk());

        //logout as admin
        this.mock.perform(get("/user/logout").contentType("application/json")).andExpect(status().isOk());

        //login as second
        LoginDTO login2 = new LoginDTO("second", "second");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login2))).andExpect(status().isOk());

        //try to remove anona from chat as second
        this.mock.perform(delete("/chat/" + chat2.getChatId() + "/remove-member/" + user.getId()).contentType("application/json"))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof PermissionException));

    }

    @Test
    public void t013_removeMemberNotMember() throws Exception {
        //login as admin
        LoginDTO login = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login))).andExpect(status().isOk());

        //create chat
        ChatDTO chat = new ChatDTO(null, "test100");

        this.mock.perform(post("/chat/create").contentType("application/json")
                .content(this.objMapper.writeValueAsString(chat))).andExpect(status().isOk());

        //get UserDTO of anona
        UserDTO[] list = this.objMapper.readValue(this.mock.perform(get("/user/list").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), UserDTO[].class);

        UserDTO user = list[list.length - 1];

        //get previously created chat from list
        ChatDTO[] chatList2 = this.objMapper.readValue(this.mock.perform(get("/chat/all").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), ChatDTO[].class);

        ChatDTO chat2 = chatList2[chatList2.length - 1];

        //try to remove anona from chat as second
        this.mock.perform(delete("/chat/" + chat2.getChatId() + "/remove-member/" + user.getId()).contentType("application/json"))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof DoesNotExistException));

    }

    @Test
    public void t014_sendMessage() throws Exception {
        //login as admin
        LoginDTO login = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login))).andExpect(status().isOk());

        //create chat
        ChatDTO chat = new ChatDTO(null, "test100");

        this.mock.perform(post("/chat/create").contentType("application/json")
                .content(this.objMapper.writeValueAsString(chat))).andExpect(status().isOk());

        //get UserDTO of admin
        UserDTO[] list = this.objMapper.readValue(this.mock.perform(get("/user/list").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), UserDTO[].class);

        UserDTO user = list[0];

        //get previously created chat from list
        ChatDTO[] chatList2 = this.objMapper.readValue(this.mock.perform(get("/chat/all").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), ChatDTO[].class);

        ChatDTO chat2 = chatList2[chatList2.length - 1];

        //get number of messages before
        ChatReturnDTO chat3 = this.objMapper.readValue(this.mock.perform(get("/chat/" + chat2.getChatId()).contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), ChatReturnDTO.class);

        int size1 = chat3.getMessages().size();

        //send message
        MessageDTO msg = new MessageDTO(null, "test_msg", null, null, user, OffsetDateTime.now());

        this.mock.perform(post("/chat/" + chat2.getChatId() + "/send-message").contentType("application/json")
                .content(this.objMapper.writeValueAsString(msg)))
                .andExpect(status().isOk());

        //get number of messages after
        ChatReturnDTO chat4 = this.objMapper.readValue(this.mock.perform(get("/chat/" + chat2.getChatId()).contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), ChatReturnDTO.class);

        int size2 = chat4.getMessages().size();

        assertEquals(size1 + 1, size2);
    }

    @Test
    public void t015_sendMessageEmptyOne() throws Exception {
        //login as admin
        LoginDTO login = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login))).andExpect(status().isOk());

        //create chat
        ChatDTO chat = new ChatDTO(null, "test100");

        this.mock.perform(post("/chat/create").contentType("application/json")
                .content(this.objMapper.writeValueAsString(chat))).andExpect(status().isOk());

        //get UserDTO of admin
        UserDTO[] list = this.objMapper.readValue(this.mock.perform(get("/user/list").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), UserDTO[].class);

        UserDTO user = list[0];

        //get previously created chat from list
        ChatDTO[] chatList2 = this.objMapper.readValue(this.mock.perform(get("/chat/all").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), ChatDTO[].class);

        ChatDTO chat2 = chatList2[chatList2.length - 1];

        //send empty message
        MessageDTO msg = new MessageDTO(null, "", null, null, user, OffsetDateTime.now());

        this.mock.perform(post("/chat/" + chat2.getChatId() + "/send-message").contentType("application/json")
                .content(this.objMapper.writeValueAsString(msg)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof WrongMessageException));
    }

    @Test
    public void t016_sendMessageNotExistChat() throws Exception {
        //login as admin
        LoginDTO login = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login))).andExpect(status().isOk());

        //create chat
        ChatDTO chat = new ChatDTO(null, "test100");

        this.mock.perform(post("/chat/create").contentType("application/json")
                .content(this.objMapper.writeValueAsString(chat))).andExpect(status().isOk());

        //get UserDTO of admin
        UserDTO[] list = this.objMapper.readValue(this.mock.perform(get("/user/list").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), UserDTO[].class);

        UserDTO user = list[0];

        //get previously created chat from list
        ChatDTO[] chatList2 = this.objMapper.readValue(this.mock.perform(get("/chat/all").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), ChatDTO[].class);

        ChatDTO chat2 = chatList2[chatList2.length - 1];

        //send message to not existing chat
        MessageDTO msg = new MessageDTO(null, "test_msg", null, null, user, OffsetDateTime.now());

        this.mock.perform(post("/chat/" + (chat2.getChatId() - 1000) + "/send-message").contentType("application/json")
                .content(this.objMapper.writeValueAsString(msg)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof DoesNotExistException));
    }

    @Test
    public void t017_editMessage() throws Exception {
        //login as admin
        LoginDTO login = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login))).andExpect(status().isOk());

        //create chat
        ChatDTO chat = new ChatDTO(null, "test100");

        this.mock.perform(post("/chat/create").contentType("application/json")
                .content(this.objMapper.writeValueAsString(chat))).andExpect(status().isOk());

        //get UserDTO of admin
        UserDTO[] list = this.objMapper.readValue(this.mock.perform(get("/user/list").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), UserDTO[].class);

        UserDTO user = list[0];

        //get previously created chat from list
        ChatDTO[] chatList2 = this.objMapper.readValue(this.mock.perform(get("/chat/all").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), ChatDTO[].class);

        ChatDTO chat2 = chatList2[chatList2.length - 1];

        //send message
        MessageDTO msg = new MessageDTO(null, "test_msg", null, null, user, OffsetDateTime.now());

        this.mock.perform(post("/chat/" + chat2.getChatId() + "/send-message").contentType("application/json")
                .content(this.objMapper.writeValueAsString(msg)))
                .andExpect(status().isOk());

        //get previously created message
        ChatReturnDTO chat3 = this.objMapper.readValue(this.mock.perform(get("/chat/" + chat2.getChatId()).contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), ChatReturnDTO.class);

        MessageDTO msg2 = chat3.getMessages().get(chat3.getMessages().size() - 1);

        //edit message
        msg2.setContent("edited_test_msg");
        this.mock.perform(post("/chat/edit-message").contentType("application/json")
                .content(this.objMapper.writeValueAsString(msg2)))
                .andExpect(status().isOk());

        //get message after content editing
        ChatReturnDTO chat4 = this.objMapper.readValue(this.mock.perform(get("/chat/" + chat2.getChatId()).contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), ChatReturnDTO.class);

        MessageDTO msg3 = chat4.getMessages().get(chat3.getMessages().size() - 1);

        assertEquals("edited_test_msg", msg3.getContent());

    }

    @Test
    public void t018_editMessageDoesNotExist() throws Exception {
        //login as admin
        LoginDTO login = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login))).andExpect(status().isOk());

        //create chat
        ChatDTO chat = new ChatDTO(null, "test100");

        this.mock.perform(post("/chat/create").contentType("application/json")
                .content(this.objMapper.writeValueAsString(chat))).andExpect(status().isOk());

        //get UserDTO of admin
        UserDTO[] list = this.objMapper.readValue(this.mock.perform(get("/user/list").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), UserDTO[].class);

        UserDTO user = list[0];

        //get previously created chat from list
        ChatDTO[] chatList2 = this.objMapper.readValue(this.mock.perform(get("/chat/all").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), ChatDTO[].class);

        ChatDTO chat2 = chatList2[chatList2.length - 1];

        //send message
        MessageDTO msg = new MessageDTO(null, "test_msg", null, null, user, OffsetDateTime.now());

        this.mock.perform(post("/chat/" + chat2.getChatId() + "/send-message").contentType("application/json")
                .content(this.objMapper.writeValueAsString(msg)))
                .andExpect(status().isOk());

        //get previously created message
        ChatReturnDTO chat3 = this.objMapper.readValue(this.mock.perform(get("/chat/" + chat2.getChatId()).contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), ChatReturnDTO.class);

        MessageDTO msg2 = chat3.getMessages().get(chat3.getMessages().size() - 1);

        //edit message
        msg2.setContent("edited_test_msg");
        msg2.setId(7582358L);
        this.mock.perform(post("/chat/edit-message").contentType("application/json")
                .content(this.objMapper.writeValueAsString(msg2)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof DoesNotExistException));

    }

    @Test
    public void t019_editMessageNotOwner() throws Exception {
        //login as admin
        LoginDTO login = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login))).andExpect(status().isOk());

        //create chat
        ChatDTO chat = new ChatDTO(null, "test100");

        this.mock.perform(post("/chat/create").contentType("application/json")
                .content(this.objMapper.writeValueAsString(chat))).andExpect(status().isOk());

        //get UserDTO of admin
        UserDTO[] list = this.objMapper.readValue(this.mock.perform(get("/user/list").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), UserDTO[].class);

        UserDTO user = list[0];

        //get previously created chat from list
        ChatDTO[] chatList2 = this.objMapper.readValue(this.mock.perform(get("/chat/all").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), ChatDTO[].class);

        ChatDTO chat2 = chatList2[chatList2.length - 1];

        //send message
        MessageDTO msg = new MessageDTO(null, "test_msg", null, null, user, OffsetDateTime.now());

        this.mock.perform(post("/chat/" + chat2.getChatId() + "/send-message").contentType("application/json")
                .content(this.objMapper.writeValueAsString(msg)))
                .andExpect(status().isOk());

        //get previously created message
        ChatReturnDTO chat3 = this.objMapper.readValue(this.mock.perform(get("/chat/" + chat2.getChatId()).contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), ChatReturnDTO.class);

        MessageDTO msg2 = chat3.getMessages().get(chat3.getMessages().size() - 1);

        //logout as admin
        this.mock.perform(get("/user/logout").contentType("application/json")).andExpect(status().isOk());

        //login as second
        LoginDTO login2 = new LoginDTO("second", "second");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login2))).andExpect(status().isOk());

        //edit message as not owner
        msg2.setContent("edited_test_msg");
        this.mock.perform(post("/chat/edit-message").contentType("application/json")
                .content(this.objMapper.writeValueAsString(msg2)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof PermissionException));
    }

    @Test
    public void t020_removeMessage() throws Exception {
        //login as admin
        LoginDTO login = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login))).andExpect(status().isOk());

        //create chat
        ChatDTO chat = new ChatDTO(null, "test100");

        this.mock.perform(post("/chat/create").contentType("application/json")
                .content(this.objMapper.writeValueAsString(chat))).andExpect(status().isOk());

        //get UserDTO of admin
        UserDTO[] list = this.objMapper.readValue(this.mock.perform(get("/user/list").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), UserDTO[].class);

        UserDTO user = list[0];

        //get previously created chat from list
        ChatDTO[] chatList2 = this.objMapper.readValue(this.mock.perform(get("/chat/all").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), ChatDTO[].class);

        ChatDTO chat2 = chatList2[chatList2.length - 1];

        //send message
        MessageDTO msg = new MessageDTO(null, "test_msg", null, null, user, OffsetDateTime.now());

        this.mock.perform(post("/chat/" + chat2.getChatId() + "/send-message").contentType("application/json")
                .content(this.objMapper.writeValueAsString(msg)))
                .andExpect(status().isOk());

        //get previously created message
        ChatReturnDTO chat3 = this.objMapper.readValue(this.mock.perform(get("/chat/" + chat2.getChatId()).contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), ChatReturnDTO.class);

        MessageDTO msg2 = chat3.getMessages().get(chat3.getMessages().size() - 1);

        //get number of messages before
        int size1 = chat3.getMessages().size();

        //remove message
        this.mock.perform(delete("/chat/remove/" + msg2.getId()).contentType("application/json"))
                .andExpect(status().isOk());

        //get number of messages after
        ChatReturnDTO chat4 = this.objMapper.readValue(this.mock.perform(get("/chat/" + chat2.getChatId()).contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), ChatReturnDTO.class);

        int size2 = chat4.getMessages().size();

        assertEquals(size1 - 1, size2);
    }

    @Test
    public void t021_removeMessageDoesNotExist() throws Exception {
        //login as admin
        LoginDTO login = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login))).andExpect(status().isOk());

        //create chat
        ChatDTO chat = new ChatDTO(null, "test100");

        this.mock.perform(post("/chat/create").contentType("application/json")
                .content(this.objMapper.writeValueAsString(chat))).andExpect(status().isOk());

        //get UserDTO of admin
        UserDTO[] list = this.objMapper.readValue(this.mock.perform(get("/user/list").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), UserDTO[].class);

        UserDTO user = list[0];

        //get previously created chat from list
        ChatDTO[] chatList2 = this.objMapper.readValue(this.mock.perform(get("/chat/all").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), ChatDTO[].class);

        ChatDTO chat2 = chatList2[chatList2.length - 1];

        //send message
        MessageDTO msg = new MessageDTO(null, "test_msg", null, null, user, OffsetDateTime.now());

        this.mock.perform(post("/chat/" + chat2.getChatId() + "/send-message").contentType("application/json")
                .content(this.objMapper.writeValueAsString(msg)))
                .andExpect(status().isOk());

        //get previously created message
        ChatReturnDTO chat3 = this.objMapper.readValue(this.mock.perform(get("/chat/" + chat2.getChatId()).contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), ChatReturnDTO.class);

        MessageDTO msg2 = chat3.getMessages().get(chat3.getMessages().size() - 1);

        //remove message
        msg2.setId(7582358L);
        this.mock.perform(delete("/chat/remove/" + msg2.getId()).contentType("application/json"))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof DoesNotExistException));
    }

    @Test
    public void t022_removeMessageNotOwner() throws Exception {
        //login as admin
        LoginDTO login = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login))).andExpect(status().isOk());

        //create chat
        ChatDTO chat = new ChatDTO(null, "test100");

        this.mock.perform(post("/chat/create").contentType("application/json")
                .content(this.objMapper.writeValueAsString(chat))).andExpect(status().isOk());

        //get UserDTO of admin
        UserDTO[] list = this.objMapper.readValue(this.mock.perform(get("/user/list").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), UserDTO[].class);

        UserDTO user = list[0];

        //get previously created chat from list
        ChatDTO[] chatList2 = this.objMapper.readValue(this.mock.perform(get("/chat/all").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), ChatDTO[].class);

        ChatDTO chat2 = chatList2[chatList2.length - 1];

        //send message
        MessageDTO msg = new MessageDTO(null, "test_msg", null, null, user, OffsetDateTime.now());

        this.mock.perform(post("/chat/" + chat2.getChatId() + "/send-message").contentType("application/json")
                .content(this.objMapper.writeValueAsString(msg)))
                .andExpect(status().isOk());

        //get previously created message
        ChatReturnDTO chat3 = this.objMapper.readValue(this.mock.perform(get("/chat/" + chat2.getChatId()).contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), ChatReturnDTO.class);

        MessageDTO msg2 = chat3.getMessages().get(chat3.getMessages().size() - 1);

        //logout as admin
        this.mock.perform(get("/user/logout").contentType("application/json")).andExpect(status().isOk());

        //login as second
        LoginDTO login2 = new LoginDTO("second", "second");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login2))).andExpect(status().isOk());

        //remove message
        this.mock.perform(delete("/chat/remove/" + msg2.getId()).contentType("application/json"))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof PermissionException));
    }
}
