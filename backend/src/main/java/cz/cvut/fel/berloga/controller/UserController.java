package cz.cvut.fel.berloga.controller;

import cz.cvut.fel.berloga.controller.dto.InviteUserDTO;
import cz.cvut.fel.berloga.controller.dto.LoginDTO;
import cz.cvut.fel.berloga.controller.dto.StatusDTO;
import cz.cvut.fel.berloga.controller.dto.UserDTO;
import cz.cvut.fel.berloga.controller.mappers.UserMapper;
import cz.cvut.fel.berloga.entity.UserEntity;
import cz.cvut.fel.berloga.service.SessionService;
import cz.cvut.fel.berloga.service.UserService;
import cz.cvut.fel.berloga.service.exceptions.AuthorizationException;
import cz.cvut.fel.berloga.service.exceptions.UserException;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.cfg.NotYetImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;
    private final SessionService sessionService;

    @Autowired
    public UserController(UserService userService, UserMapper userMapper, SessionService sessionService) {
        this.userService = userService;
        this.userMapper = userMapper;
        this.sessionService = sessionService;
    }

    @PostMapping(path = "/invite")
    @ApiOperation(value = "Create user invitation", notes = "Create new user, assign him email and sets default password" +
            "sended to his email", produces = MediaType.APPLICATION_JSON_VALUE,  consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StatusDTO> inviteUser(@RequestBody InviteUserDTO inviteUserDTO) {
        this.userService.createUser(inviteUserDTO);
        return ResponseEntity.ok(StatusDTO.builder().status("user created").build());
    }

    @ApiOperation(value = "Login user", notes = "Login with given credentials", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @PostMapping(path = "/login")
    public ResponseEntity<StatusDTO> loginUser(@RequestBody LoginDTO loginDTO) {
        this.userService.loginUser(loginDTO);
        return ResponseEntity.ok(StatusDTO.builder().status("logged").build());
    }

    @ApiOperation(value = "Checking logged user", notes = "Checking logged user", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @GetMapping(path = "/am-i-logged")
    public ResponseEntity<StatusDTO> amILogged() {
        return ResponseEntity.ok().body(StatusDTO.builder().status(this.sessionService.isLogged()?"logged":"not logged").build());
    }

    @GetMapping(path = "/logout")
    public ResponseEntity<StatusDTO> logout() {
        this.userService.logoutUser();
        return ResponseEntity.ok().body(StatusDTO.builder().status("logged out").build());
    }

    @ApiOperation(value = "Get ", notes = "Checking logged user", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @GetMapping("/{user_id}")
    public ResponseEntity<UserDTO> getUser(@PathVariable Long user_id) throws AuthorizationException {
        return ResponseEntity.ok(this.userMapper.toDTO(this.userService.getUser(user_id)));
    }

    // BE CAREFUL, single api point to changing user from many roles
    @PostMapping(path = "/edit-profile")
    public ResponseEntity<StatusDTO> editProfile(@RequestBody UserDTO editUser) {
        // user with role student can change only his account
        // teacher can change only his account
        // MOD (administrator) can change users and teachers accounts (logged into default log)
        this.userService.editAccount(editUser);
        return ResponseEntity.ok().body(StatusDTO.builder().status("Account changed").build());
    }

    @PostMapping(path = "/add-role/{user_id}/{role}")
    public ResponseEntity<StatusDTO> addRole(@PathVariable Long user_id, @PathVariable String role) {
        this.userService.addRole(user_id, role);
        return ResponseEntity.ok().body(StatusDTO.builder().status("added role").build());
    }

    @PostMapping(path = "/delete-role/{user_id}/{role}")
    public ResponseEntity<StatusDTO> deleteRole(@PathVariable Long user_id, @PathVariable String role) {
        this.userService.deleteRole(user_id, role);
        return ResponseEntity.ok().body(StatusDTO.builder().status("deleted role").build());
    }

    @PostMapping(path = "/block/{user_id}")
    public ResponseEntity<StatusDTO> blockUser(@PathVariable Long user_id) {
        // user_id is blocked for logged user
        throw new NotYetImplementedException("Not yet implemented ");
    }

    @GetMapping(path = "/list")
    public ResponseEntity<List<UserDTO>> listUser() {
        return ResponseEntity.ok(this.userService.listUsers().stream().map(this.userMapper::toDTO).collect(Collectors.toList()));
    }

    @GetMapping(path = "/getLogged")
    public ResponseEntity<UserDTO> getLogged() {
        this.sessionService.checkLogin();
        return ResponseEntity.ok(userMapper.toDTO(this.sessionService.getSession().getUser()));
    }
}
