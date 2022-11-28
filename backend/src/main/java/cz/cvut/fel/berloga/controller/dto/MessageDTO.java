package cz.cvut.fel.berloga.controller.dto;

import cz.cvut.fel.berloga.entity.UserEntity;
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
public class MessageDTO implements Serializable {
    private Long id;
    private String content;
    private String fileId;
    private String fileName;
    private UserDTO sender;
    private OffsetDateTime date;
    }

