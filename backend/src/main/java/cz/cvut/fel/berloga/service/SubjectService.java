package cz.cvut.fel.berloga.service;

import cz.cvut.fel.berloga.controller.dto.SubjectDTO;
import cz.cvut.fel.berloga.controller.dto.SubjectExtendedDTO;
import cz.cvut.fel.berloga.entity.PageEntity;
import cz.cvut.fel.berloga.entity.SubjectEntity;
import cz.cvut.fel.berloga.entity.UserEntity;
import cz.cvut.fel.berloga.entity.UserSubjectEntity;
import cz.cvut.fel.berloga.entity.enums.UserRoleEnum;
import cz.cvut.fel.berloga.entity.enums.UserSubjectEnum;
import cz.cvut.fel.berloga.repository.SubjectRepository;
import cz.cvut.fel.berloga.repository.UserRepository;
import cz.cvut.fel.berloga.repository.UserSubjectRepository;
import cz.cvut.fel.berloga.service.exceptions.AlreadyExistException;
import cz.cvut.fel.berloga.service.exceptions.DoesNotExistException;
import cz.cvut.fel.berloga.service.exceptions.PermissionException;
import cz.cvut.fel.berloga.service.exceptions.WrongInputException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SubjectService {

    @PersistenceContext
    private EntityManager entityManager;

    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;
    private final UserSubjectRepository userSubjectRepository;
    private final SessionService sessionService;

    @Autowired
    public SubjectService(UserRepository userRepository, SubjectRepository subjectRepository, UserSubjectRepository userSubjectRepository, SessionService sessionService) {
        this.userRepository = userRepository;
        this.subjectRepository = subjectRepository;
        this.userSubjectRepository = userSubjectRepository;
        this.sessionService = sessionService;
    }

    public SubjectEntity getSubjectEntity(Long id) {
        Objects.requireNonNull(id);
        SubjectEntity subjectEntity = this.subjectRepository.findById(id).orElse(null);
        if (subjectEntity != null) {
            // if it is teacher or admin, he can see
            if(this.sessionService.getSession().getUser().getRoles().stream()
                    .anyMatch(userRoleEntity ->
                            userRoleEntity.getUserRole().equals(UserRoleEnum.MODERATOR) ||
                                    userRoleEntity.getUserRole().equals(UserRoleEnum.TEACHER))) {
                return subjectEntity;
            }
            // true if logged user is in subject
            boolean isSubscribed = subjectEntity.getUsers().stream()
                    .anyMatch(userSubjectEntity -> userSubjectEntity.getUser().getId()
                            .equals(this.sessionService.getSession().getUser().getId()));
            if (!isSubscribed) {
                // person, who is not in subject, cant see anyone, whos in. Unless its admin or teacher
                subjectEntity.setUsers(new ArrayList<>());
            }
            return subjectEntity;
        } else {
            throw new DoesNotExistException("Subject entity with id(" + id + ") doesn't exist");
        }
    }


    @Transactional
    public SubjectEntity createSubject(SubjectExtendedDTO subjectDTO) {
        // check null
        Objects.requireNonNull(subjectDTO);

        // check permission
        UserEntity creator = this.sessionService.getSession().getUser();
        if (creator.getRoles().stream().noneMatch(userRoleEntity -> userRoleEntity.getUserRole().equals(UserRoleEnum.MODERATOR))) {
            throw new PermissionException("Only Administrator/Moderator can create subjects");
        }

        if(subjectDTO.getCode() == null || subjectDTO.getCode().equals("")) {
            throw new WrongInputException("Subject code must not be null");
        }


        if(this.subjectRepository.findAll().stream().anyMatch(subjectEntity -> subjectEntity.getCode().equals(subjectDTO.getCode()))) {
            throw new AlreadyExistException("Subject with code already exist");
        }

        if(subjectDTO.getName() == null || subjectDTO.getName().equals("")) {
            throw new WrongInputException("Subject name must not be null");
        }

        SubjectEntity subjectEntity = new SubjectEntity();
        subjectEntity.setName(subjectDTO.getName());
        subjectEntity.setCode(subjectDTO.getCode());
        subjectEntity.setPicture(subjectDTO.getPicture());
        subjectEntity.setDescription(subjectDTO.getDescription());
        PageEntity p = new PageEntity();
        p.setSubject(subjectEntity);
        p.setData("# sem přijde text");
        subjectEntity.setPages(Collections.singletonList(p));

        // subjectEntity.setUsers(Collections.singletonList(use));
        // question if will save bidirectionally
        // subjectRepository.save(subjectEntity);
        return subjectRepository.save(subjectEntity);
    }

    @Transactional
    public SubjectEntity editSubject(SubjectExtendedDTO subjectDTO) {
        Objects.requireNonNull(subjectDTO);
        Objects.requireNonNull(subjectDTO.getId());

        if(subjectDTO.getName() == null || subjectDTO.getName().equals("")) {
            throw new WrongInputException("Subject name must not be null");
        }

        if(subjectDTO.getCode() == null || subjectDTO.getCode().equals("")) {
            throw new WrongInputException("Subject code must not be null");
        }

        SubjectEntity subjectEntity = subjectRepository.findById(subjectDTO.getId()).orElse(null);
        if (subjectEntity == null) {
            throw new DoesNotExistException("Subject with id " + subjectDTO.getId() + " doesn't exist");
        }

        UserEntity creator = this.sessionService.getSession().getUser();
        boolean isAdmin = creator.getRoles().stream().anyMatch(userRoleEntity -> userRoleEntity.getUserRole().equals(UserRoleEnum.MODERATOR));
        boolean isInSubTeacher = subjectEntity.getUsers().stream().anyMatch(userSubjectEntity -> userSubjectEntity.getUser()
                .getId().equals(creator.getId()) && userSubjectEntity.getUserType().equals(UserSubjectEnum.TEACHER));

        if(!isAdmin && !isInSubTeacher) {
            throw new PermissionException("You cant change subject data, you arent owner or admin");
        }

        subjectEntity.setDescription(subjectDTO.getDescription());
        subjectEntity.setCode(subjectDTO.getCode());
        subjectEntity.setPicture(subjectDTO.getPicture());
        subjectEntity.setName(subjectDTO.getName());
        subjectEntity.getPages().get(0).setData(subjectDTO.getPageData());
        subjectRepository.save(subjectEntity);
        return subjectEntity;
    }

    @Transactional
    public void subscribeUser(Long subjectId, Long userId, String type) {
        SubjectEntity subjectEntity = subjectRepository.findById(subjectId).orElse(null);
        if (subjectEntity == null) {
            throw new DoesNotExistException("Subject with id " + subjectId + " doesn't exist");
        }

        UserEntity userEntity = userRepository.findById(userId).orElse(null);
        if (userEntity == null) {
            throw new DoesNotExistException("User with id " + userId + " doesn't exist");
        }

        UserSubjectEnum userSubjectEnum;

        try {
            userSubjectEnum = UserSubjectEnum.valueOf(type);
        } catch (IllegalArgumentException iae) {
            throw new DoesNotExistException("Type doesn't exist");
        }

        // IF MODERATOR/ADMIN, THEN HE CAN DO ANYTHING
        if(this.sessionService.getSession().getUser().getRoles().stream().noneMatch(userRoleEntity -> userRoleEntity.getUserRole().equals(UserRoleEnum.MODERATOR))) {
            //CHECK IF LOGGED USER IS TEACHER IN THIS SUBJECT
            if (subjectEntity.getUsers().stream().noneMatch(
                    userSubjectEntity -> userSubjectEntity.getUser().getId().equals(
                            this.sessionService.getSession().getUser().getId()
                    ) && userSubjectEntity.getUserType().equals(UserSubjectEnum.TEACHER)
            )) {
                throw new PermissionException("User must be in subject as teacher to add another person");
            }
        }

        UserEntity foundUser = subjectEntity.getUsers().stream()
                .map(UserSubjectEntity::getUser)
                .filter(user -> user.getId().equals(userId))
                .findFirst()
                .orElse(null);

        if (foundUser != null) {
            throw new AlreadyExistException("User " + foundUser.getUsername() + " is already subscribed for subject " + subjectEntity.getName());
        }

        UserSubjectEntity userSubjectEntity = new UserSubjectEntity();
        userSubjectEntity.setUser(userEntity);
        userSubjectEntity.setSubject(subjectEntity);
        userSubjectEntity.setUserType(userSubjectEnum);
        userSubjectRepository.save(userSubjectEntity);
    }

    @Transactional
    public void unsubscribeUser(Long subjectId, Long userId) {
        SubjectEntity subjectEntity = subjectRepository.findById(subjectId).orElse(null);
        if (subjectEntity == null) {
            throw new DoesNotExistException("Subject with id " + subjectId + " doesn't exist");
        }

        UserEntity userEntity = userRepository.findById(userId).orElse(null);
        if (userEntity == null) {
            throw new DoesNotExistException("User with id " + userId + " doesn't exist");
        }

        // IF MODERATOR/ADMIN, THEN HE CAN DO ANYTHING
        if(this.sessionService.getSession().getUser().getRoles().stream().noneMatch(userRoleEntity -> userRoleEntity.getUserRole().equals(UserRoleEnum.MODERATOR))) {
            //CHECK IF LOGGED USER IS TEACHER IN THIS SUBJECT
            if (subjectEntity.getUsers().stream().noneMatch(
                    userSubjectEntity -> userSubjectEntity.getUser().getId().equals(
                            this.sessionService.getSession().getUser().getId()
                    ) && userSubjectEntity.getUserType().equals(UserSubjectEnum.TEACHER)
            )) {
                throw new PermissionException("User must be in subject as teacher to add another person");
            }
        }

        UserSubjectEntity toDelete = userEntity.getSubjects().stream()
                .filter(userSubjectEntity -> userSubjectEntity.getUser().getId().equals(userId)&&userSubjectEntity.getSubject().getId().equals(subjectId))
                .findAny().orElse(null);

        if(toDelete == null) {
            throw new DoesNotExistException("User " + userEntity.getUsername() + " is not subscribed for subject " + subjectEntity.getName());
        } else {
            Long id = toDelete.getId();
            this.entityManager.detach(subjectEntity);
            this.entityManager.detach(userEntity);
            this.entityManager.detach(toDelete);
            this.userSubjectRepository.deleteById(id);
        }
    }

    public List<SubjectEntity> listSubjectOfUser(Long userId) {
        // only teacher or admin
        if(this.sessionService.getSession().getUser().getRoles().stream()
                .noneMatch(userRoleEntity ->
                        userRoleEntity.getUserRole().equals(UserRoleEnum.MODERATOR) ||
                        userRoleEntity.getUserRole().equals(UserRoleEnum.TEACHER))) {
            throw new PermissionException("You cant see another person subjects, unless you are admin");
        }
        return this.subjectRepository.findAllByUserStudying(userId);
    }

    public List<SubjectEntity> listAllSubjects() {
        return this.subjectRepository.findAll();
    }

}