package cz.cvut.fel.berloga.controller.mappers;

import cz.cvut.fel.berloga.controller.dto.RecordDTO;
import cz.cvut.fel.berloga.entity.CalendarRecordEntity;
import org.mapstruct.Mapper;

@Mapper
public abstract class CalendarMapper {

    public RecordDTO toDto(CalendarRecordEntity calendarRecordEntity) {
        RecordDTO dto = new RecordDTO();
        dto.setName(calendarRecordEntity.getName());
        dto.setDateTimeFinish(calendarRecordEntity.getDateTimeFinish());
        dto.setDateTimeStart(calendarRecordEntity.getDateTimeStart());
        dto.setRepeat(calendarRecordEntity.getRepeat());
        dto.setId(calendarRecordEntity.getId());
        dto.setRecordType(calendarRecordEntity.getRecordType());
        return dto;
    }
}
