package cz.cvut.fel.berloga.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.junit.jupiter.api.Assertions.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.cvut.fel.berloga.controller.dto.*;
import cz.cvut.fel.berloga.service.exceptions.DoesNotExistException;
import cz.cvut.fel.berloga.service.exceptions.LoginException;
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

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ForumControllerTest {

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
    public void t001_createQuestion() throws Exception {
        //login as admin
        LoginDTO login = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login))).andExpect(status().isOk());

        //get number of questions before
        int size1 = this.objMapper.readValue(this.mock.perform(get("/forum/all").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), QuestionForumDTO[].class).length;

        //get UserDTO of admin
        UserDTO[] list = this.objMapper.readValue(this.mock.perform(get("/user/list").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), UserDTO[].class);

        UserDTO user = list[0];

        //create question
        QuestionForumDTO question = new QuestionForumDTO(null, "test_question", null, OffsetDateTime.now(), user, null, false);

        this.mock.perform(post("/forum/create-question").contentType("application/json")
                .content(this.objMapper.writeValueAsString(question))).andExpect(status().isOk());

        //get number of questions after
        int size2 = this.objMapper.readValue(this.mock.perform(get("/forum/all").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), QuestionForumDTO[].class).length;

        assertEquals(size1 + 1, size2);
    }

    @Test
    public void t002_createQuestionNotLoggedIn() throws Exception {
        //login as admin
        LoginDTO login = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login))).andExpect(status().isOk());

        //get UserDTO of admin
        UserDTO[] list = this.objMapper.readValue(this.mock.perform(get("/user/list").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), UserDTO[].class);

        UserDTO user = list[0];

        //logout as admin
        this.mock.perform(get("/user/logout").contentType("application/json")).andExpect(status().isOk());

        //create question
        QuestionForumDTO question = new QuestionForumDTO(null, "test_question", null, OffsetDateTime.now(), user, null, false);
        this.mock.perform(post("/forum/create-question").contentType("application/json")
                .content(this.objMapper.writeValueAsString(question)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof LoginException));
    }

    @Test
    public void t003_createQuestionNoContent() throws Exception {
        //login as admin
        LoginDTO login = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login))).andExpect(status().isOk());

        //get UserDTO of admin
        UserDTO[] list = this.objMapper.readValue(this.mock.perform(get("/user/list").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), UserDTO[].class);

        UserDTO user = list[0];

        //create question
        QuestionForumDTO question = new QuestionForumDTO(null, "", null, OffsetDateTime.now(), user, null, false);
        this.mock.perform(post("/forum/create-question").contentType("application/json")
                .content(this.objMapper.writeValueAsString(question)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof WrongInputException));
    }

    @Test
    public void t004_removeQuestion() throws Exception {
        //login as admin
        LoginDTO login = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login))).andExpect(status().isOk());

        //get UserDTO of admin
        UserDTO[] list = this.objMapper.readValue(this.mock.perform(get("/user/list").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), UserDTO[].class);

        UserDTO user = list[0];

        //create question
        QuestionForumDTO question = new QuestionForumDTO(null, "test_question", null, OffsetDateTime.now(), user, null, false);

        this.mock.perform(post("/forum/create-question").contentType("application/json")
                .content(this.objMapper.writeValueAsString(question))).andExpect(status().isOk());

        //get number of questions before
        int size1 = this.objMapper.readValue(this.mock.perform(get("/forum/all").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), QuestionForumDTO[].class).length;

        //get previously created question from list
        QuestionForumDTO[] questionList2 = this.objMapper.readValue(this.mock.perform(get("/forum/all").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), QuestionForumDTO[].class);

        QuestionForumDTO question2 = questionList2[questionList2.length - 1];

        //delete question
        this.mock.perform(delete("/forum/questions/" + question2.getId()).contentType("application/json"))
                .andExpect(status().isOk());

        //get number of questions after
        int size2 = this.objMapper.readValue(this.mock.perform(get("/forum/all").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), QuestionForumDTO[].class).length;

        assertEquals(size1 - 1, size2);
    }

    @Test
    public void t005_removeQuestionNotExist() throws Exception {
        //login as admin
        LoginDTO login = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login))).andExpect(status().isOk());

        //get UserDTO of admin
        UserDTO[] list = this.objMapper.readValue(this.mock.perform(get("/user/list").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), UserDTO[].class);

        UserDTO user = list[0];

        //create question
        QuestionForumDTO question = new QuestionForumDTO(null, "test_question", null, OffsetDateTime.now(), user, null, false);

        this.mock.perform(post("/forum/create-question").contentType("application/json")
                .content(this.objMapper.writeValueAsString(question))).andExpect(status().isOk());

        //get previously created question from list
        QuestionForumDTO[] questionList2 = this.objMapper.readValue(this.mock.perform(get("/forum/all").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), QuestionForumDTO[].class);

        QuestionForumDTO question2 = questionList2[questionList2.length - 1];

        question2.setId(7779346L);

        //delete question
        this.mock.perform(delete("/forum/questions/" + question2.getId()).contentType("application/json"))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof DoesNotExistException));
    }

    @Test
    public void t006_removeQuestionNotLoggedIn() throws Exception {
        //login as admin
        LoginDTO login = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login))).andExpect(status().isOk());

        //get UserDTO of admin
        UserDTO[] list = this.objMapper.readValue(this.mock.perform(get("/user/list").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), UserDTO[].class);

        UserDTO user = list[0];

        //create question
        QuestionForumDTO question = new QuestionForumDTO(null, "test_question", null, OffsetDateTime.now(), user, null, false);

        this.mock.perform(post("/forum/create-question").contentType("application/json")
                .content(this.objMapper.writeValueAsString(question))).andExpect(status().isOk());

        //get previously created question from list
        QuestionForumDTO[] questionList2 = this.objMapper.readValue(this.mock.perform(get("/forum/all").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), QuestionForumDTO[].class);

        QuestionForumDTO question2 = questionList2[questionList2.length - 1];

        //logout as admin
        this.mock.perform(get("/user/logout").contentType("application/json")).andExpect(status().isOk());

        //delete question
        this.mock.perform(delete("/forum/questions/" + question2.getId()).contentType("application/json"))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof LoginException));
    }

    @Test
    public void t007_editQuestion() throws Exception {
        //login as admin
        LoginDTO login = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login))).andExpect(status().isOk());

        //get UserDTO of admin
        UserDTO[] list = this.objMapper.readValue(this.mock.perform(get("/user/list").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), UserDTO[].class);

        UserDTO user = list[0];

        //create question
        QuestionForumDTO question = new QuestionForumDTO(null, "test_question", null, OffsetDateTime.now(), user, null, false);

        this.mock.perform(post("/forum/create-question").contentType("application/json")
                .content(this.objMapper.writeValueAsString(question))).andExpect(status().isOk());

        //get previously created question from list before
        QuestionForumDTO[] questionList2 = this.objMapper.readValue(this.mock.perform(get("/forum/all").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), QuestionForumDTO[].class);

        QuestionForumDTO question2 = questionList2[questionList2.length - 1];

        //edit question
        question2.setQuestion("edited_test_question");

        this.mock.perform(post("/forum/edit-question").contentType("application/json")
                .content(this.objMapper.writeValueAsString(question2))).andExpect(status().isOk());

        //get previously created question from list after
        QuestionForumDTO[] questionList3 = this.objMapper.readValue(this.mock.perform(get("/forum/all").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), QuestionForumDTO[].class);

        QuestionForumDTO question3 = questionList3[questionList3.length - 1];

        assertEquals("edited_test_question", question3.getQuestion());
    }

    @Test
    public void t008_editQuestionNotExist() throws Exception {
        //login as admin
        LoginDTO login = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login))).andExpect(status().isOk());

        //get UserDTO of admin
        UserDTO[] list = this.objMapper.readValue(this.mock.perform(get("/user/list").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), UserDTO[].class);

        UserDTO user = list[0];

        //create question
        QuestionForumDTO question = new QuestionForumDTO(null, "test_question", null, OffsetDateTime.now(), user, null, false);

        this.mock.perform(post("/forum/create-question").contentType("application/json")
                .content(this.objMapper.writeValueAsString(question))).andExpect(status().isOk());

        //get previously created question from list
        QuestionForumDTO[] questionList2 = this.objMapper.readValue(this.mock.perform(get("/forum/all").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), QuestionForumDTO[].class);

        QuestionForumDTO question2 = questionList2[questionList2.length - 1];

        //edit question
        question2.setQuestion("edited_test_question");
        question2.setId(6534646L);

        this.mock.perform(post("/forum/edit-question").contentType("application/json")
                .content(this.objMapper.writeValueAsString(question2)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof DoesNotExistException));
    }

    @Test
    public void t009_editQuestionNoRights() throws Exception {
        //login as admin
        LoginDTO login = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login))).andExpect(status().isOk());

        //get UserDTO of admin
        UserDTO[] list = this.objMapper.readValue(this.mock.perform(get("/user/list").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), UserDTO[].class);

        UserDTO user = list[0];

        //create question
        QuestionForumDTO question = new QuestionForumDTO(null, "test_question", null, OffsetDateTime.now(), user, null, false);

        this.mock.perform(post("/forum/create-question").contentType("application/json")
                .content(this.objMapper.writeValueAsString(question))).andExpect(status().isOk());

        //get previously created question from list
        QuestionForumDTO[] questionList2 = this.objMapper.readValue(this.mock.perform(get("/forum/all").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), QuestionForumDTO[].class);

        QuestionForumDTO question2 = questionList2[questionList2.length - 1];

        //logout as admin
        this.mock.perform(get("/user/logout").contentType("application/json")).andExpect(status().isOk());

        //login as second
        LoginDTO login2 = new LoginDTO("second", "second");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login2))).andExpect(status().isOk());

        //edit question
        question2.setQuestion("edited_test_question");

        this.mock.perform(post("/forum/edit-question").contentType("application/json")
                .content(this.objMapper.writeValueAsString(question2)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof PermissionException));
    }

    @Test
    public void t010_markDone() throws Exception {
        //login as admin
        LoginDTO login = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login))).andExpect(status().isOk());

        //get UserDTO of admin
        UserDTO[] list = this.objMapper.readValue(this.mock.perform(get("/user/list").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), UserDTO[].class);

        UserDTO user = list[0];

        //create question
        QuestionForumDTO question = new QuestionForumDTO(null, "test_question", null, OffsetDateTime.now(), user, null, false);

        this.mock.perform(post("/forum/create-question").contentType("application/json")
                .content(this.objMapper.writeValueAsString(question))).andExpect(status().isOk());

        //get previously created question from list before
        QuestionForumDTO[] questionList2 = this.objMapper.readValue(this.mock.perform(get("/forum/all").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), QuestionForumDTO[].class);

        QuestionForumDTO question2 = questionList2[questionList2.length - 1];

        //mark question as done
        this.mock.perform(post("/forum/mark-done/" + question2.getId()).contentType("application/json"))
                .andExpect(status().isOk());

        //get previously created question from list after
        QuestionForumDTO[] questionList3 = this.objMapper.readValue(this.mock.perform(get("/forum/all").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), QuestionForumDTO[].class);

        QuestionForumDTO question3 = questionList3[questionList3.length - 1];

        assertTrue(question3.isDone());
    }

    @Test
    public void t011_markDoneNotExist() throws Exception {
        //login as admin
        LoginDTO login = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login))).andExpect(status().isOk());

        //get UserDTO of admin
        UserDTO[] list = this.objMapper.readValue(this.mock.perform(get("/user/list").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), UserDTO[].class);

        UserDTO user = list[0];

        //create question
        QuestionForumDTO question = new QuestionForumDTO(null, "test_question", null, OffsetDateTime.now(), user, null, false);

        this.mock.perform(post("/forum/create-question").contentType("application/json")
                .content(this.objMapper.writeValueAsString(question))).andExpect(status().isOk());

        //get previously created question from list
        QuestionForumDTO[] questionList2 = this.objMapper.readValue(this.mock.perform(get("/forum/all").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), QuestionForumDTO[].class);

        QuestionForumDTO question2 = questionList2[questionList2.length - 1];

        //mark question as done
        question2.setId(6534646L);
        this.mock.perform(post("/forum/mark-done/" + question2.getId()).contentType("application/json"))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof DoesNotExistException));
    }

    @Test
    public void t012_markDoneNoRights() throws Exception {
        //login as admin
        LoginDTO login = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login))).andExpect(status().isOk());

        //get UserDTO of admin
        UserDTO[] list = this.objMapper.readValue(this.mock.perform(get("/user/list").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), UserDTO[].class);

        UserDTO user = list[0];

        //create question
        QuestionForumDTO question = new QuestionForumDTO(null, "test_question", null, OffsetDateTime.now(), user, null, false);

        this.mock.perform(post("/forum/create-question").contentType("application/json")
                .content(this.objMapper.writeValueAsString(question))).andExpect(status().isOk());

        //get previously created question from list
        QuestionForumDTO[] questionList2 = this.objMapper.readValue(this.mock.perform(get("/forum/all").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), QuestionForumDTO[].class);

        QuestionForumDTO question2 = questionList2[questionList2.length - 1];

        //logout as admin
        this.mock.perform(get("/user/logout").contentType("application/json")).andExpect(status().isOk());

        //login as second
        LoginDTO login2 = new LoginDTO("second", "second");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login2))).andExpect(status().isOk());

        //mark question as done
        this.mock.perform(post("/forum/mark-done/" + question2.getId()).contentType("application/json"))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof PermissionException));
    }

    @Test
    public void t013_createComment() throws Exception {
        //login as admin
        LoginDTO login = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login))).andExpect(status().isOk());

        //get UserDTO of admin
        UserDTO[] list = this.objMapper.readValue(this.mock.perform(get("/user/list").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), UserDTO[].class);

        UserDTO user = list[0];

        //create question
        QuestionForumDTO question = new QuestionForumDTO(null, "test_question", null, OffsetDateTime.now(), user, null, false);

        this.mock.perform(post("/forum/create-question").contentType("application/json")
                .content(this.objMapper.writeValueAsString(question))).andExpect(status().isOk());

        //get previously created question from list before
        QuestionForumDTO[] questionList2 = this.objMapper.readValue(this.mock.perform(get("/forum/all").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), QuestionForumDTO[].class);

        QuestionForumDTO question2 = questionList2[questionList2.length - 1];

        //get number of comments before
        int size1 = question2.getComments().size();

        //create comment to the question
        CommentDTO comment = new CommentDTO(null, "test_comment", user, OffsetDateTime.now(), question2.getId());

        this.mock.perform(post("/forum/comment-question").contentType("application/json")
                .content(this.objMapper.writeValueAsString(comment))).andExpect(status().isOk());

        //get previously created question from list after
        QuestionForumDTO[] questionList3 = this.objMapper.readValue(this.mock.perform(get("/forum/all").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), QuestionForumDTO[].class);

        QuestionForumDTO question3 = questionList3[questionList3.length - 1];

        //get number of comments after
        int size2 = question3.getComments().size();

        assertEquals(size1 + 1, size2);
    }

    @Test
    public void t014_createCommentQuestionNotExist() throws Exception {
        //login as admin
        LoginDTO login = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login))).andExpect(status().isOk());

        //get UserDTO of admin
        UserDTO[] list = this.objMapper.readValue(this.mock.perform(get("/user/list").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), UserDTO[].class);

        UserDTO user = list[0];

        //create question
        QuestionForumDTO question = new QuestionForumDTO(null, "test_question", null, OffsetDateTime.now(), user, null, false);

        this.mock.perform(post("/forum/create-question").contentType("application/json")
                .content(this.objMapper.writeValueAsString(question))).andExpect(status().isOk());

        //create comment to the question
        CommentDTO comment = new CommentDTO(null, "test_comment", user, OffsetDateTime.now(), 76384639L);

        this.mock.perform(post("/forum/comment-question").contentType("application/json")
                .content(this.objMapper.writeValueAsString(comment)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof DoesNotExistException));
    }

    @Test
    public void t015_removeComment() throws Exception {
        //login as admin
        LoginDTO login = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login))).andExpect(status().isOk());

        //get UserDTO of admin
        UserDTO[] list = this.objMapper.readValue(this.mock.perform(get("/user/list").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), UserDTO[].class);

        UserDTO user = list[0];

        //create question
        QuestionForumDTO question = new QuestionForumDTO(null, "test_question", null, OffsetDateTime.now(), user, null, false);

        this.mock.perform(post("/forum/create-question").contentType("application/json")
                .content(this.objMapper.writeValueAsString(question))).andExpect(status().isOk());

        //get previously created question from list 1st time
        QuestionForumDTO[] questionList2 = this.objMapper.readValue(this.mock.perform(get("/forum/all").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), QuestionForumDTO[].class);

        QuestionForumDTO question2 = questionList2[questionList2.length - 1];

        //create comment to the question
        CommentDTO comment = new CommentDTO(null, "test_comment", user, OffsetDateTime.now(), question2.getId());

        this.mock.perform(post("/forum/comment-question").contentType("application/json")
                .content(this.objMapper.writeValueAsString(comment))).andExpect(status().isOk());

        //get previously created question from list 2nd time
        QuestionForumDTO[] questionList3 = this.objMapper.readValue(this.mock.perform(get("/forum/all").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), QuestionForumDTO[].class);

        QuestionForumDTO question3 = questionList3[questionList3.length - 1];

        //get number of comments before
        int size1 = question3.getComments().size();

        //get comment from question
        CommentDTO comment2 = question3.getComments().get(question3.getComments().size() - 1);

        //delete comment
        this.mock.perform(delete("/forum/remove-comment/" + comment2.getId()).contentType("application/json")
                .content(this.objMapper.writeValueAsString(comment2))).andExpect(status().isOk());

        //get previously created question from list 3rd time
        QuestionForumDTO[] questionList4 = this.objMapper.readValue(this.mock.perform(get("/forum/all").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), QuestionForumDTO[].class);

        QuestionForumDTO question4 = questionList4[questionList4.length - 1];

        //get number of comments after
        int size2 = question4.getComments().size();

        assertEquals(size1 - 1, size2);
    }

    @Test
    public void t016_removeCommentNotExist() throws Exception {
        //login as admin
        LoginDTO login = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login))).andExpect(status().isOk());

        //get UserDTO of admin
        UserDTO[] list = this.objMapper.readValue(this.mock.perform(get("/user/list").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), UserDTO[].class);

        UserDTO user = list[0];

        //create question
        QuestionForumDTO question = new QuestionForumDTO(null, "test_question", null, OffsetDateTime.now(), user, null, false);

        this.mock.perform(post("/forum/create-question").contentType("application/json")
                .content(this.objMapper.writeValueAsString(question))).andExpect(status().isOk());

        //get previously created question from list 1st time
        QuestionForumDTO[] questionList2 = this.objMapper.readValue(this.mock.perform(get("/forum/all").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), QuestionForumDTO[].class);

        QuestionForumDTO question2 = questionList2[questionList2.length - 1];

        //create comment to the question
        CommentDTO comment = new CommentDTO(null, "test_comment", user, OffsetDateTime.now(), question2.getId());

        this.mock.perform(post("/forum/comment-question").contentType("application/json")
                .content(this.objMapper.writeValueAsString(comment))).andExpect(status().isOk());

        //get previously created question from list 2nd time
        QuestionForumDTO[] questionList3 = this.objMapper.readValue(this.mock.perform(get("/forum/all").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), QuestionForumDTO[].class);

        QuestionForumDTO question3 = questionList3[questionList3.length - 1];

        //get comment from question
        CommentDTO comment2 = question3.getComments().get(question3.getComments().size() - 1);

        //delete comment
        this.mock.perform(delete("/forum/remove-comment/" + 46346L).contentType("application/json")
                .content(this.objMapper.writeValueAsString(comment2)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof DoesNotExistException));

    }
}