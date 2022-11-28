package cz.cvut.fel.berloga.controller.dto;

import cz.cvut.fel.berloga.entity.SubjectEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QuestionForumDTO implements Serializable {
    private Long id;
    private String question;
    private Long subjectId;
    private OffsetDateTime dateTime;
    private UserDTO sender;
    private List<CommentDTO> comments;
    private boolean done;
}
