package cz.cvut.fel.berloga.controller.dto;

import cz.cvut.fel.berloga.entity.UserSubjectEntity;
import cz.cvut.fel.berloga.entity.enums.UserSubjectEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SubjectExtendedDTO implements Serializable {
    private Long id;
    private String name;
    private String code;
    private String description;
    private String picture;
    private UserSubjectEnum type;
    private List<UserDTO> teachers;
    private String pageData;
}
