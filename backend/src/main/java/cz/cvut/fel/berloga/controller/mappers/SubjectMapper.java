package cz.cvut.fel.berloga.controller.mappers;

import cz.cvut.fel.berloga.controller.dto.RecordDTO;
import cz.cvut.fel.berloga.controller.dto.SubjectDTO;
import cz.cvut.fel.berloga.controller.dto.SubjectExtendedDTO;
import cz.cvut.fel.berloga.controller.dto.UserDTO;
import cz.cvut.fel.berloga.entity.SubjectEntity;
import cz.cvut.fel.berloga.entity.UserEntity;
import cz.cvut.fel.berloga.entity.UserSubjectEntity;
import cz.cvut.fel.berloga.entity.enums.UserSubjectEnum;
import cz.cvut.fel.berloga.service.UserService;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mapper
public class SubjectMapper {

    @Autowired
    UserMapper userMapper;

    public SubjectDTO toDTO(SubjectEntity subjectEntity) {
        SubjectDTO subjectDTO = new SubjectDTO();
        subjectDTO.setCode(subjectEntity.getCode());
        subjectDTO.setDescription(subjectEntity.getDescription());
        subjectDTO.setId(subjectEntity.getId());
        subjectDTO.setName(subjectEntity.getName());
        subjectDTO.setPicture(subjectEntity.getPicture());
        return subjectDTO;
    }

    public SubjectExtendedDTO toExtendedDTO(SubjectEntity subjectEntity, Long userId) {
        SubjectExtendedDTO subjectDTO = new SubjectExtendedDTO();
        subjectDTO.setCode(subjectEntity.getCode());
        subjectDTO.setDescription(subjectEntity.getDescription());
        subjectDTO.setId(subjectEntity.getId());
        subjectDTO.setName(subjectEntity.getName());
        subjectDTO.setPicture(subjectEntity.getPicture());
        UserSubjectEntity use = subjectEntity.getUsers().stream().filter(userSubjectEntity -> userSubjectEntity.getUser().getId().equals(userId)).findFirst().orElse(null);
        subjectDTO.setType(use == null ? null : use.getUserType());
        subjectDTO.setPageData(subjectEntity.getPages().size()>0?subjectEntity.getPages().get(0).getData():null);
        return subjectDTO;


    }

    public SubjectExtendedDTO toExtendedDTO(SubjectEntity subjectEntity) {
        SubjectExtendedDTO subjectDTO = new SubjectExtendedDTO();
        subjectDTO.setCode(subjectEntity.getCode());
        subjectDTO.setDescription(subjectEntity.getDescription());
        subjectDTO.setId(subjectEntity.getId());
        subjectDTO.setName(subjectEntity.getName());
        subjectDTO.setPicture(subjectEntity.getPicture());
        List<UserDTO> teachers = subjectEntity.getUsers().stream()
                .filter(u -> u.getUserType().equals(UserSubjectEnum.TEACHER))
                .map(UserSubjectEntity::getUser)
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
        subjectDTO.setTeachers(teachers);
        subjectDTO.setPageData(subjectEntity.getPages().size()>0?subjectEntity.getPages().get(0).getData():null);
        return subjectDTO;
    }
}
