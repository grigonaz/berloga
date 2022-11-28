package cz.cvut.fel.berloga.controller.dto;

import cz.cvut.fel.berloga.entity.enums.RecordTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecordDTO implements Serializable {

    private Long id;
    private String name;
    private OffsetDateTime dateTimeStart;
    private OffsetDateTime dateTimeFinish;
    private RecordTypeEnum recordType;
    private Long subjectId;
    private String repeat;

}
