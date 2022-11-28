package cz.cvut.fel.berloga.controller.mappers;

import cz.cvut.fel.berloga.controller.dto.UserDTO;
import cz.cvut.fel.berloga.entity.SubjectEntity;
import cz.cvut.fel.berloga.entity.UserEntity;
import cz.cvut.fel.berloga.entity.UserRoleEntity;
import cz.cvut.fel.berloga.entity.UserSubjectEntity;
import org.mapstruct.Mapper;

import java.util.stream.Collectors;

@Mapper
public abstract class UserMapper {

    public UserDTO toDTO(UserEntity userEntity) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(userEntity.getId());
        userDTO.setUsername(userEntity.getUsername());
        userDTO.setFirstName(userEntity.getFirstName());
        userDTO.setLastName(userEntity.getLastName());
        userDTO.setEmail(userEntity.getEmail());
        userDTO.setBlocked(userEntity.getBlocked());
        userDTO.setRoles(userEntity.getRoles().stream().map(UserRoleEntity::getUserRole).collect(Collectors.toList()));
        userDTO.setSubjects(userEntity.getSubjects().stream().map(UserSubjectEntity::getSubject).map(SubjectEntity::getId).collect(Collectors.toList()));
        return userDTO;
    }

}
