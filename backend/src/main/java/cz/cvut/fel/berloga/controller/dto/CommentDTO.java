package cz.cvut.fel.berloga.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentDTO  implements Serializable {
    private Long id;
    private String content;
    private UserDTO sender;
    private OffsetDateTime dateTime;
    private Long questionId;
}
