package cz.cvut.fel.berloga.service;

import cz.cvut.fel.berloga.controller.dto.InviteUserDTO;
import cz.cvut.fel.berloga.controller.dto.LoginDTO;
import cz.cvut.fel.berloga.controller.dto.UserDTO;
import cz.cvut.fel.berloga.entity.UserEntity;
import cz.cvut.fel.berloga.entity.UserRoleEntity;
import cz.cvut.fel.berloga.entity.enums.UserRoleEnum;
import cz.cvut.fel.berloga.repository.UserRepository;
import cz.cvut.fel.berloga.repository.UserRoleRepository;
import cz.cvut.fel.berloga.service.exceptions.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class UserService {

    @PersistenceContext
    private EntityManager entityManager;

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final SessionService sessionService;
    private final CryptoService cryptoService;

    @Autowired
    public UserService(UserRepository userRepository, SessionService sessionService, CryptoService cryptoService, UserRoleRepository userRoleRepository) {
        this.userRepository = userRepository;
        this.sessionService = sessionService;
        this.cryptoService = cryptoService;
        this.userRoleRepository = userRoleRepository;
    }

    public UserEntity getUserByUsername(String username) {
        return this.userRepository.getByUsernameEquals(username);
    }

    public UserEntity getUserById(Long id) {
        return this.userRepository.getOne(id);
    }

    public void loginUser(LoginDTO loginDTO) throws UserException {
        if (loginDTO != null && loginDTO.getUsername() != null && RegexService.checkUsername(loginDTO.getUsername())) {
            UserEntity maybe = this.userRepository.getByUsernameEquals(loginDTO.getUsername());
            if (maybe == null || !maybe.getPasswordHash().equals(this.cryptoService.generatePasswordHash(loginDTO.getPassword()))) {
                throw new UserException("Account doesn't exist");
            } else {
                // user exist and password match
                this.sessionService.associateSession(maybe.getId());
            }
        } else {
            throw new UserException("Given username is null or have invalid chars");
        }
    }

    public void logoutUser() throws UserException {
        if (!this.sessionService.isLogged()) {
            throw new UserException("No users logged");
        }
        this.sessionService.dissociateSession(this.sessionService.getSession().getUser().getId());
    }

    public UserEntity createUser(InviteUserDTO dto) throws UserException {
        this.sessionService.checkLogin();
        if (this.sessionService.getSession().getUser().getRoles().stream().noneMatch(userRoleEntity -> userRoleEntity.getUserRole().equals(UserRoleEnum.MODERATOR))) {
            throw new PermissionException("you must be administrator to add new users");
        }
        // check dto and regexes
        if (dto == null || dto.getUsername() == null || dto.getEmail() == null ||
                !RegexService.checkUsername(dto.getUsername()) || !RegexService.checkEmail(dto.getEmail())) {
            throw new UserException("Username or email is empty or invalid");
        }
        // check if user exist with username
        if (this.userRepository.getByUsernameEquals(dto.getUsername()) != null) {
            throw new UserException("Username is occupied");
        }
        if (this.userRepository.getByEmailEquals(dto.getEmail()) != null) {
            throw new UserException("Email is occupied");
        }
        // create entity and return
        UserEntity newUser = new UserEntity();
        newUser.setUsername(dto.getUsername());
        newUser.setEmail(dto.getEmail());
        newUser.setBlocked(false);
        newUser.setPasswordHash(cryptoService.generatePasswordHash(dto.getPassword()));
        newUser.setFirstName(dto.getFirstName());
        newUser.setLastName(dto.getLastName());
        UserRoleEntity userRoleEntity = new UserRoleEntity();
        userRoleEntity.setUser(newUser);
        userRoleEntity.setUserRole(UserRoleEnum.STUDENT);
        newUser.setRoles(Collections.singletonList(userRoleEntity));
        return this.userRepository.save(newUser);
    }

    public UserEntity getUser(Long userId) throws AuthorizationException {
        this.sessionService.checkLogin();
        // my profile?
        boolean showAll = false;
        // logged user has role moderator, can read any accounts
        if (this.sessionService.getSession().getUser().getId().equals(userId)) {
            // self display
            showAll = true;
        } else showAll = this.sessionService.getSession().getUser().getRoles().stream().anyMatch(userRoleEntity -> userRoleEntity.getUserRole().equals(UserRoleEnum.MODERATOR));
        UserEntity getUser = this.userRepository.getOne(userId);
        getUser.setPasswordHash(null);
        if (showAll) {
            // there is printed all data
            // password will be hidden after mapping into dto
            return getUser;
        } else {
            // there isd public data about user, visible for others
            return this.showOnlyPublic(getUser);
        }
    }

    public List<UserEntity> listUsers() {
        return this.userRepository.findAll().stream().map(this::showOnlyPublic).collect(Collectors.toList());
    }

    /**
     * Take only public data and show them
     *
     * @param userEntity
     * @return
     */
    private UserEntity showOnlyPublic(UserEntity userEntity) {
        UserEntity returned = new UserEntity();
        returned.setId(userEntity.getId());
        returned.setUsername(userEntity.getUsername());
        returned.setBlocked(userEntity.getBlocked());
        returned.setRoles(userEntity.getRoles());
        returned.setSubjects(userEntity.getSubjects());
        returned.setFirstName(userEntity.getFirstName());
        returned.setLastName(userEntity.getLastName());
        returned.setChats(userEntity.getChats());
        // admin can see moore
        if (this.sessionService.getSession().getUser().getRoles().stream().anyMatch(userRoleEntity -> userRoleEntity.getUserRole().equals(UserRoleEnum.MODERATOR))) {
            returned.setEmail(userEntity.getEmail());
            returned.setCalendarRecords(userEntity.getCalendarRecords());
        }
        return returned;
    }

    public void addRole(Long userId, String role) {
        Objects.requireNonNull(userId);
        Objects.requireNonNull(role);
        if (this.sessionService.getSession().getUser().getRoles().stream().noneMatch(userRoleEntity -> userRoleEntity.getUserRole().equals(UserRoleEnum.MODERATOR))) {
            throw new PermissionException("Only administrator/moderator can change roles");
        }

        UserEntity user = this.userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new DoesNotExistException("User doesn't exist");
        }

        UserRoleEnum roleValue;
        try {
            roleValue = UserRoleEnum.valueOf(role);
        } catch (IllegalArgumentException iae) {
            throw new DoesNotExistException("Role doesn't exist");
        }

        if (user.getRoles().stream().anyMatch(userRoleEntity -> userRoleEntity.getUserRole().equals(roleValue))) {
            throw new AlreadyExistException("User already have role");
        }

        UserRoleEntity userRoleEntity = new UserRoleEntity();
        userRoleEntity.setUserRole(roleValue);
        userRoleEntity.setUser(user);
        user.getRoles().add(userRoleEntity);
        this.userRoleRepository.save(userRoleEntity);
        this.userRepository.save(user);
    }

    public void deleteRole(Long userId, String role) {
        Objects.requireNonNull(userId);
        Objects.requireNonNull(role);
        if (this.sessionService.getSession().getUser().getRoles().stream().noneMatch(userRoleEntity -> userRoleEntity.getUserRole().equals(UserRoleEnum.MODERATOR))) {
            throw new PermissionException("Only administrator/moderator can change roles");
        }

        UserEntity user = this.userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new DoesNotExistException("User doesn't exist");
        }

        UserRoleEnum roleValue;
        try {
            roleValue = UserRoleEnum.valueOf(role);
        } catch (IllegalArgumentException iae) {
            throw new DoesNotExistException("Role doesn't exist");
        }

        if (user.getRoles().stream().noneMatch(userRoleEntity -> userRoleEntity.getUserRole().equals(roleValue))) {
            throw new AlreadyExistException("User doesn't have role");
        }

        Long deleteRoleId = user.getRoles().stream().filter(userRoleEntity -> userRoleEntity.getUserRole().equals(roleValue)).map(UserRoleEntity::getId).collect(Collectors.toList()).get(0);

        this.entityManager.detach(user);
        this.userRoleRepository.deleteById(deleteRoleId);
    }

    public void editAccount(UserDTO userDTO) {
        this.sessionService.checkLogin();
        // password and username change
        if (userDTO == null) {
            throw new UserException("Invalid DTO");
        }
        UserEntity loggedUser = this.sessionService.getSession().getUser();
        boolean isAdmin = loggedUser.getRoles().stream().anyMatch(userRoleEntity -> userRoleEntity.getUserRole().equals(UserRoleEnum.MODERATOR));
        if (!loggedUser.getId().equals(userDTO.getId())) {
            // admin can change
            if (!isAdmin) {
                throw new UserException("You cannot change user other than you. You are not administrator");
            }
        }
        if (!(userDTO.getPassword() == null || userDTO.getPassword().equals("")) && !RegexService.checkPassword(userDTO.getPassword())) {
            throw new UserException("Password must be valid");
        }
        if (!(userDTO.getUsername() == null || userDTO.getUsername().equals("")) && !RegexService.checkUsername(userDTO.getUsername())) {
            throw new UserException("Username must be valid");
        }

        UserEntity changed = this.userRepository.findById(userDTO.getId()).orElse(null);
        if (changed == null) {
            // this cannot happen but for sure i will check it
            throw new RuntimeException("Null 'changed' entity");
        } else {
            changed.setPasswordHash(userDTO.getPassword() == null ? changed.getPasswordHash() : this.cryptoService.generatePasswordHash(userDTO.getPassword()));
            changed.setUsername(userDTO.getUsername() == null ? changed.getUsername() : userDTO.getUsername());
            if (isAdmin) {
                changed.setFirstName(userDTO.getFirstName() == null ? changed.getFirstName() : userDTO.getFirstName());
                changed.setLastName(userDTO.getLastName() == null ? changed.getLastName() : userDTO.getLastName());
                if (userDTO.getEmail() != null) {
                    if (RegexService.checkEmail(userDTO.getEmail())) {
                        changed.setEmail(userDTO.getEmail());
                    } else {
                        // invalid email
                        throw new UserException("Email must be valid");
                    }
                }
            }
            this.userRepository.save(changed);
        }
    }
}
