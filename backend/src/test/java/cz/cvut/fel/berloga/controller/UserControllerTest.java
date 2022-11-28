package cz.cvut.fel.berloga.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.junit.jupiter.api.Assertions.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.cvut.fel.berloga.controller.dto.InviteUserDTO;
import cz.cvut.fel.berloga.controller.dto.LoginDTO;
import cz.cvut.fel.berloga.controller.dto.UserDTO;
import cz.cvut.fel.berloga.service.exceptions.AlreadyExistException;
import cz.cvut.fel.berloga.service.exceptions.DoesNotExistException;
import cz.cvut.fel.berloga.service.exceptions.PermissionException;
import cz.cvut.fel.berloga.service.exceptions.UserException;
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

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UserControllerTest { // 2 of 27 does not have assert!

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

    @Test // No assert
    public void t001_loginUser() throws Exception {
        // Login as admin
        LoginDTO login = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login))).andExpect(status().isOk());
    }

    @Test
    public void t002_loginUserInvalidUsername() throws Exception {
        // Try to login with wrong username
        LoginDTO login = new LoginDTO("-", "password");
        this.mock.perform(post("/user/login").contentType("application/json").content(this.objMapper.writeValueAsString(login)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof UserException));
    }

    @Test
    public void t003_isUserLogged() throws Exception {
        // Login as admin
        LoginDTO login = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login))).andExpect(status().isOk());

        // Check
        this.mock.perform(get("/user/am-i-logged").contentType("application/json")).andExpect(status().isOk());
    }

    @Test // No assert
    public void t004_logoutUser() throws Exception {
        // Login as admin
        LoginDTO login = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login))).andExpect(status().isOk());

        // Logout as admin
        this.mock.perform(get("/user/logout").contentType("application/json")).andExpect(status().isOk());
    }

    @Test
    public void t005_createNewUser() throws Exception {
        // Login as admin
        LoginDTO login = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login))).andExpect(status().isOk());

        // Get size of user list
        int size1 = this.objMapper.readValue(this.mock.perform(get("/user/list").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), UserDTO[].class).length;

        // Create new user
        InviteUserDTO invite = new InviteUserDTO("test", "test", "username1",
                "Test123", "username1@gmail.com");
        this.mock.perform(post("/user/invite").contentType("application/json")
                .content(this.objMapper.writeValueAsString(invite))).andExpect(status().isOk());

        // Get new size of user list
        int size2 = this.objMapper.readValue(this.mock.perform(get("/user/list").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), UserDTO[].class).length;

        // Check
        assertEquals(size1 + 1, size2);
    }

    @Test
    public void t006_createNewUserWithNoPermission() throws Exception {
        // Login as admin
        LoginDTO login1 = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login1))).andExpect(status().isOk());

        // Create new user
        InviteUserDTO invite1 = new InviteUserDTO("test", "test", "username2",
                "Test123", "username2@gmail.com");
        this.mock.perform(post("/user/invite").contentType("application/json")
                .content(this.objMapper.writeValueAsString(invite1))).andExpect(status().isOk());

        // Logout as admin
        this.mock.perform(get("/user/logout").contentType("application/json")).andExpect(status().isOk());

        // Login as new user
        LoginDTO login2 = new LoginDTO("username2", "Test123");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login2))).andExpect(status().isOk());

        // Try to create one more user with no permission
        InviteUserDTO invite2 = new InviteUserDTO("test", "test", "test",
                "Test123", "testtest@gmail.com");
        this.mock.perform(post("/user/invite").contentType("application/json").content(this.objMapper.writeValueAsString(invite2)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof PermissionException));
    }

    @Test
    public void t007_createNewUserUsernameIsEmptyOrInvalid() throws Exception {
        // Login as admin
        LoginDTO login = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login))).andExpect(status().isOk());

        // Try to create user with wrong username
        InviteUserDTO invite = new InviteUserDTO("test", "test", "-",
                "Test123", "test@gmail.com");
        this.mock.perform(post("/user/invite").contentType("application/json").content(this.objMapper.writeValueAsString(invite)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof UserException));
    }

    @Test
    public void t008_createNewUserUsernameIsEmptyOrInvalid() throws Exception {
        // Login as admin
        LoginDTO login = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login))).andExpect(status().isOk());

        // Try to create user with wrong email
        InviteUserDTO invite = new InviteUserDTO("test", "test", "test",
                "Test123", "-");
        this.mock.perform(post("/user/invite").contentType("application/json").content(this.objMapper.writeValueAsString(invite)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof UserException));
    }

    @Test
    public void t009_createNewUserUsernameIsOccupied() throws Exception {
        // Login as admin
        LoginDTO login = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login))).andExpect(status().isOk());

        // Create new user
        InviteUserDTO invite1 = new InviteUserDTO("name", "surname", "username3",
                "Test123", "username3@gmail.com");
        this.mock.perform(post("/user/invite").contentType("application/json")
                .content(this.objMapper.writeValueAsString(invite1))).andExpect(status().isOk());

        // Try to create one more user with the same username
        InviteUserDTO invite2 = new InviteUserDTO("name", "surname", "username3",
                "Test123", "username4@gmail.com");

        this.mock.perform(post("/user/invite").contentType("application/json")
                .content(this.objMapper.writeValueAsString(invite2)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof UserException));
    }

    @Test
    public void t010_createNewUserEmailIsOccupied() throws Exception {
        // Login as admin
        LoginDTO login = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login))).andExpect(status().isOk());

        // Create new user
        InviteUserDTO invite1 = new InviteUserDTO("name", "surname", "username4",
                "Test123", "username4@gmail.com");
        this.mock.perform(post("/user/invite").contentType("application/json")
                .content(this.objMapper.writeValueAsString(invite1))).andExpect(status().isOk());

        // Try to create new user with the same email
        InviteUserDTO invite2 = new InviteUserDTO("name", "surname", "username5",
                "Test123", "username4@gmail.com");
        this.mock.perform(post("/user/invite").contentType("application/json").content(this.objMapper.writeValueAsString(invite2)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof UserException));
    }

    @Test
    public void t011_getUser() throws Exception {
        // Login as admin
        LoginDTO login = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login))).andExpect(status().isOk());

        // Create new user
        InviteUserDTO invite = new InviteUserDTO("test", "test", "username0",
                "Test123", "username0@gmail.com");
        this.mock.perform(post("/user/invite").contentType("application/json")
                .content(this.objMapper.writeValueAsString(invite))).andExpect(status().isOk());

        // Get user id
        UserDTO[] users = this.objMapper.readValue(this.mock.perform(get("/user/list").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), UserDTO[].class);
        Long id = users[users.length-1].getId();

        // Get user
        UserDTO user = this.objMapper.readValue(this.mock.perform(get("/user/" + id).contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), UserDTO.class);

        // Check
        assertEquals(id, user.getId());
    }

    @Test
    public void t012_editUser() throws Exception {
        // Login as admin
        LoginDTO login = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login))).andExpect(status().isOk());

        // Create user
        InviteUserDTO invite = new InviteUserDTO("test", "test", "username5",
                "Test123", "username5@gmail.com");
        this.mock.perform(post("/user/invite").contentType("application/json")
                .content(this.objMapper.writeValueAsString(invite))).andExpect(status().isOk());

        // Get new user
        UserDTO[] list = this.objMapper.readValue(this.mock.perform(get("/user/list").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), UserDTO[].class);
        UserDTO user1 = list[list.length - 1];

        // Set new username
        user1.setUsername("NewUsername123");

        // Edit username
        this.mock.perform(post("/user/edit-profile").contentType("application/json")
                .content(this.objMapper.writeValueAsString(user1))).andExpect(status().isOk());

        // Get user with new  username
        UserDTO user2 = this.objMapper.readValue(this.mock.perform(get("/user/" + user1.getId()).contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), UserDTO.class);

        // Check
        assertEquals(user2.getUsername(), "NewUsername123");
    }

    @Test
    public void t013_editUserFirstNameWhenYouAreAdmin() throws Exception {
        // Login as admin
        LoginDTO login1 = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login1))).andExpect(status().isOk());

        // Create new user
        InviteUserDTO invite1 = new InviteUserDTO("test", "test", "username6",
                "Test123", "username6@gmail.com");
        this.mock.perform(post("/user/invite").contentType("application/json")
                .content(this.objMapper.writeValueAsString(invite1))).andExpect(status().isOk());

        // Get new user
        UserDTO[] list = this.objMapper.readValue(this.mock.perform(get("/user/list").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), UserDTO[].class);
        UserDTO user1 = list[list.length - 1];

        // Set new first name
        user1.setFirstName("FirstName");

        // Edit user
        this.mock.perform(post("/user/edit-profile").contentType("application/json")
                .content(this.objMapper.writeValueAsString(user1))).andExpect(status().isOk());

        // Check
        assertEquals("FirstName", user1.getFirstName());
    }

    @Test
    public void t014_editUserNoValidUsername() throws Exception {
        // Login as admin
        LoginDTO login = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login))).andExpect(status().isOk());

        // Create new user
        InviteUserDTO invite = new InviteUserDTO("test", "test", "username7",
                "Test123", "username7@gmail.com");
        this.mock.perform(post("/user/invite").contentType("application/json")
                .content(this.objMapper.writeValueAsString(invite))).andExpect(status().isOk());

        // Get new user
        UserDTO[] list = this.objMapper.readValue(this.mock.perform(get("/user/list").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), UserDTO[].class);
        UserDTO user1 = list[list.length - 1];

        // Set invalid username
        user1.setUsername("???");

        // Try to edit profile
        this.mock.perform(post("/user/edit-profile").contentType("application/json").content(this.objMapper.writeValueAsString(user1)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof UserException));
    }

    @Test
    public void t015_editUserNoValidEmail() throws Exception {
        // Login as admin
        LoginDTO login = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login))).andExpect(status().isOk());

        // Create new user
        InviteUserDTO invite = new InviteUserDTO("test", "test", "username8",
                "Test123", "username8@gmail.com");
        this.mock.perform(post("/user/invite").contentType("application/json")
                .content(this.objMapper.writeValueAsString(invite))).andExpect(status().isOk());

        // Get new user
        UserDTO[] list = this.objMapper.readValue(this.mock.perform(get("/user/list").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), UserDTO[].class);
        UserDTO user1 = list[list.length - 1];

        // Set invalid email
        user1.setEmail("???");

        // Try to edit user
        this.mock.perform(post("/user/edit-profile").contentType("application/json").content(this.objMapper.writeValueAsString(user1)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof UserException));
    }

    @Test
    public void t016_editUserWithNoPermission() throws Exception {
        // Login as admin
        LoginDTO login1 = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login1))).andExpect(status().isOk());

        // Create new user
        InviteUserDTO invite1 = new InviteUserDTO("test", "test", "username9",
                "Test123", "username9@gmail.com");
        this.mock.perform(post("/user/invite").contentType("application/json")
                .content(this.objMapper.writeValueAsString(invite1))).andExpect(status().isOk());

        // Create one more new user
        InviteUserDTO invite2 = new InviteUserDTO("test", "test", "username10",
                "Test123", "username10@gmail.com");
        this.mock.perform(post("/user/invite").contentType("application/json")
                .content(this.objMapper.writeValueAsString(invite2))).andExpect(status().isOk());

        // Logout as admin
        this.mock.perform(get("/user/logout").contentType("application/json")).andExpect(status().isOk());

        // Login as new user
        LoginDTO login2 = new LoginDTO("username9", "Test123");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login2))).andExpect(status().isOk());

        // Get last new user
        UserDTO[] list = this.objMapper.readValue(this.mock.perform(get("/user/list").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), UserDTO[].class);
        UserDTO user1 = list[list.length - 1];

        // Set new username to last new user
        user1.setUsername("NewUsername123");

        // Try to edit last new user
        this.mock.perform(post("/user/edit-profile").contentType("application/json").content(this.objMapper.writeValueAsString(user1)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof UserException));
    }

    @Test
    public void t017_editUsernameByUserHimself() throws Exception {
        // Login as admin
        LoginDTO login1 = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login1))).andExpect(status().isOk());

        // Create new user
        InviteUserDTO invite1 = new InviteUserDTO("test", "test", "username11",
                "Test123", "username11@gmail.com");
        this.mock.perform(post("/user/invite").contentType("application/json")
                .content(this.objMapper.writeValueAsString(invite1))).andExpect(status().isOk());

        // Logout as admin
        this.mock.perform(get("/user/logout").contentType("application/json")).andExpect(status().isOk());

        // Login as new user
        LoginDTO login2 = new LoginDTO("username11", "Test123");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login2))).andExpect(status().isOk());

        // Get user himself
        UserDTO[] list = this.objMapper.readValue(this.mock.perform(get("/user/list").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), UserDTO[].class);
        UserDTO user1 = list[list.length - 1];

        // Set new username
        user1.setUsername("NewUsername123");

        // Edit user himself
        this.mock.perform(post("/user/edit-profile").contentType("application/json")
                .content(this.objMapper.writeValueAsString(user1))).andExpect(status().isOk());

        // Check
        assertEquals("NewUsername123", user1.getUsername());
    }

    @Test
    public void t018_addRole() throws Exception {
        // Login as admin
        LoginDTO login = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login))).andExpect(status().isOk());

        // Create new user
        InviteUserDTO invite1 = new InviteUserDTO("test", "test", "userAssertTest1",
                "Test123", "userAssertTest1@gmail.com");
        this.mock.perform(post("/user/invite").contentType("application/json")
                .content(this.objMapper.writeValueAsString(invite1))).andExpect(status().isOk());

        // Get new  user
        UserDTO[] list = this.objMapper.readValue(this.mock.perform(get("/user/list").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), UserDTO[].class);
        UserDTO user = list[list.length - 1];

        // Add new role to user
        this.mock.perform(post("/user/add-role/" + user.getId() + "/TEACHER").contentType("application/json"))
                .andExpect(status().isOk());

        // Check
        assertEquals(1, user.getRoles().size());
    }

    @Test
    public void t019_deleteRoleWithNoPermission() throws Exception {
        // Login as admin
        LoginDTO login1 = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login1))).andExpect(status().isOk());

        // Create new user
        InviteUserDTO invite1 = new InviteUserDTO("test", "test", "username12",
                "Test123", "username12@gmail.com");
        this.mock.perform(post("/user/invite").contentType("application/json")
                .content(this.objMapper.writeValueAsString(invite1))).andExpect(status().isOk());

        // Create one more new user
        InviteUserDTO invite2 = new InviteUserDTO("test", "test", "username13",
                "Test123", "username13@gmail.com");
        this.mock.perform(post("/user/invite").contentType("application/json")
                .content(this.objMapper.writeValueAsString(invite2))).andExpect(status().isOk());

        // Logout as admin
        this.mock.perform(get("/user/logout").contentType("application/json")).andExpect(status().isOk());

        // Login as new user
        LoginDTO login2 = new LoginDTO("username12", "Test123");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login2))).andExpect(status().isOk());

        // Get last new user
        UserDTO[] list = this.objMapper.readValue(this.mock.perform(get("/user/list").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), UserDTO[].class);
        UserDTO user = list[list.length - 1];

        // Try to delete last new user's role
        this.mock.perform(post("/user/delete-role/" + user.getId() + "/TEACHER").contentType("application/json"))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof PermissionException));
    }

    @Test
    public void t020_deleteRoleToUserThatDoesNotExist() throws Exception {
        // Login as admin
        LoginDTO login = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login))).andExpect(status().isOk());

        // Get last user
        UserDTO[] list = this.objMapper.readValue(this.mock.perform(get("/user/list").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), UserDTO[].class);
        UserDTO user = list[list.length - 1];

        // Try to delete role from user that doesn't exist
        this.mock.perform(post("/user/delete-role/" + (user.getId()*989)+ "/TEACHER").contentType("application/json"))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof DoesNotExistException));
    }

    @Test
    public void t021_deleteRoleThatDoesNotExist() throws Exception {
        // Login as admin
        LoginDTO login = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login))).andExpect(status().isOk());

        // Get last user
        UserDTO[] list = this.objMapper.readValue(this.mock.perform(get("/user/list").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), UserDTO[].class);
        UserDTO user = list[list.length - 1];

        // Try to delete role that doesn't exist
        this.mock.perform(post("/user/add-role/" + user.getId() + "/MAPHIN").contentType("application/json"))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof DoesNotExistException));
    }

    @Test
    public void t022_addSameRoleTwoTimes() throws Exception {
        // Login as admin
        LoginDTO login = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login))).andExpect(status().isOk());

        // Create new user
        InviteUserDTO invite = new InviteUserDTO("test", "test", "username14",
                "Test123", "username14@gmail.com");
        this.mock.perform(post("/user/invite").contentType("application/json")
                .content(this.objMapper.writeValueAsString(invite))).andExpect(status().isOk());

        // Get new user
        UserDTO[] list = this.objMapper.readValue(this.mock.perform(get("/user/list").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), UserDTO[].class);
        UserDTO user = list[list.length - 1];

        // Add new role to user
        this.mock.perform(post("/user/add-role/" + user.getId() + "/TEACHER").contentType("application/json"))
                .andExpect(status().isOk());

        // Try to add same role to user
        this.mock.perform(post("/user/add-role/" + user.getId() + "/TEACHER").contentType("application/json"))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof AlreadyExistException));
    }

    @Test //No assert
    public void t023_deleteRole() throws Exception {
        // Login as admin
        LoginDTO login = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login))).andExpect(status().isOk());

        // Create new user
        InviteUserDTO invite1 = new InviteUserDTO("test", "test", "userAssertTest2",
                "Test123", "userAssertTest2@gmail.com");
        this.mock.perform(post("/user/invite").contentType("application/json")
                .content(this.objMapper.writeValueAsString(invite1))).andExpect(status().isOk());

        // Get new user
        UserDTO[] list = this.objMapper.readValue(this.mock.perform(get("/user/list").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), UserDTO[].class);
        UserDTO user = list[list.length - 1];

        // Add new role to user
        this.mock.perform(post("/user/add-role/" + user.getId() + "/TEACHER").contentType("application/json"))
                .andExpect(status().isOk());

        // Refresh user
        list = this.objMapper.readValue(this.mock.perform(get("/user/list").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), UserDTO[].class);
        user = list[list.length - 1];

        // Check
        assertEquals(2, user.getRoles().size());

        // Delete role from user
        this.mock.perform(post("/user/delete-role/" + user.getId() + "/TEACHER").contentType("application/json"))
                .andExpect(status().isOk());

        // Get user one more time
        UserDTO[] list2 = this.objMapper.readValue(this.mock.perform(get("/user/list").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), UserDTO[].class);
        UserDTO user2 = list2[list2.length - 1];

        // Check
        assertEquals(1, user2.getRoles().size());
    }

    @Test
    public void t024_addRoleWithNoPermission() throws Exception {
        // Login as admin
        LoginDTO login1 = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login1))).andExpect(status().isOk());

        // Create new user
        InviteUserDTO invite1 = new InviteUserDTO("test", "test", "username15",
                "Test123", "username15@gmail.com");
        this.mock.perform(post("/user/invite").contentType("application/json")
                .content(this.objMapper.writeValueAsString(invite1))).andExpect(status().isOk());

        // Create one more new user
        InviteUserDTO invite2 = new InviteUserDTO("test", "test", "username16",
                "Test123", "username16@gmail.com");
        this.mock.perform(post("/user/invite").contentType("application/json")
                .content(this.objMapper.writeValueAsString(invite2))).andExpect(status().isOk());

        // Logout as admin
        this.mock.perform(get("/user/logout").contentType("application/json")).andExpect(status().isOk());

        // Login as new user
        LoginDTO login2 = new LoginDTO("username15", "Test123");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login2))).andExpect(status().isOk());

        // Get last new user
        UserDTO[] list = this.objMapper.readValue(this.mock.perform(get("/user/list").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), UserDTO[].class);
        UserDTO user = list[list.length - 1];

        // Try to change last new user's role
        this.mock.perform(post("/user/add-role/" + user.getId() + "/TEACHER").contentType("application/json"))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof PermissionException));
    }

    @Test
    public void t025_addRoleToUserThatDoesNotExist() throws Exception {
        // Login as admin
        LoginDTO login = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login))).andExpect(status().isOk());

        // Get last user
        UserDTO[] list = this.objMapper.readValue(this.mock.perform(get("/user/list").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), UserDTO[].class);
        UserDTO user = list[list.length - 1];

        // Try to add role to user that doesn't exist
        this.mock.perform(post("/user/add-role/" + (user.getId()*989)+ "/TEACHER").contentType("application/json"))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof DoesNotExistException));
    }

    @Test
    public void t026_addRoleThatDoesNotExist() throws Exception {
        // Login as admin
        LoginDTO login = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login))).andExpect(status().isOk());

        // Get last user
        UserDTO[] list = this.objMapper.readValue(this.mock.perform(get("/user/list").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), UserDTO[].class);
        UserDTO user = list[list.length - 1];

        // Try to add role that doesn't exist
        this.mock.perform(post("/user/add-role/" + user.getId() + "/MAPHIN").contentType("application/json"))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof DoesNotExistException));
    }

    @Test
    public void t027_deleteRoleThatDoesNotExist() throws Exception {
        // Login as admin
        LoginDTO login = new LoginDTO("administrator", "password");
        this.mock.perform(post("/user/login").contentType("application/json")
                .content(this.objMapper.writeValueAsString(login))).andExpect(status().isOk());

        // Create new user
        InviteUserDTO invite = new InviteUserDTO("test", "test", "username17",
                "Test123", "username17@gmail.com");
        this.mock.perform(post("/user/invite").contentType("application/json")
                .content(this.objMapper.writeValueAsString(invite))).andExpect(status().isOk());

        // Get new user
        UserDTO[] list = this.objMapper.readValue(this.mock.perform(get("/user/list").contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), UserDTO[].class);
        UserDTO user = list[list.length - 1];

        // Try delete role, but user don't have any
        this.mock.perform(post("/user/delete-role/" + user.getId() + "/TEACHER").contentType("application/json"))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof AlreadyExistException));
    }

}
