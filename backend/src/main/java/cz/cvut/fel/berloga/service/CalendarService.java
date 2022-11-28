package cz.cvut.fel.berloga.service;

import cz.cvut.fel.berloga.controller.dto.RecordDTO;
import cz.cvut.fel.berloga.entity.CalendarRecordEntity;
import cz.cvut.fel.berloga.entity.SubjectEntity;
import cz.cvut.fel.berloga.entity.UserEntity;
import cz.cvut.fel.berloga.entity.enums.RecordTypeEnum;
import cz.cvut.fel.berloga.repository.CalendarRecordRepository;
import cz.cvut.fel.berloga.repository.SubjectRepository;
import cz.cvut.fel.berloga.service.exceptions.DoesNotExistException;
import cz.cvut.fel.berloga.service.exceptions.PermissionException;
import cz.cvut.fel.berloga.service.exceptions.WrongInputException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class CalendarService {

    private final SessionService sessionService;
    private final CalendarRecordRepository recordRepository;
    private final SubjectRepository subjectRepository;

    @Autowired
    public CalendarService(SessionService sessionService, CalendarRecordRepository recordRepository, SubjectRepository subjectRepository) {
        this.sessionService = sessionService;
        this.recordRepository = recordRepository;
        this.subjectRepository = subjectRepository;
    }

    public List<CalendarRecordEntity> getCalendar(){
        UserEntity logged = sessionService.getSession().getUser();
        return recordRepository.findCalendarRecordEntityByUserId(logged.getId());
    }


    public void createRecord(RecordDTO recordDTO) {
        if(recordDTO.getRecordType() ==null){
            recordDTO.setRecordType(RecordTypeEnum.OTHER);
        }
        checkNotNullCreateRecordDTO(recordDTO);
        CalendarRecordEntity record = new CalendarRecordEntity();
        UserEntity logged = sessionService.getSession().getUser();
        fillAndSaveRecord(recordDTO, record, logged);
    }

    public void updateRecord(RecordDTO recordDTO) {
        if(recordDTO.getRecordType() ==null){
            recordDTO.setRecordType(RecordTypeEnum.OTHER);
        }
        checkNotNullCreateRecordDTO(recordDTO);
        CalendarRecordEntity record = recordRepository.findById(recordDTO.getId()).orElse(null);
        if (record == null) {
            throw new DoesNotExistException("Record " + recordDTO.getId() + " does not exist" );
        }
        UserEntity logged = sessionService.getSession().getUser();
        if (!record.getUser().getId().equals(logged.getId())){
            throw new PermissionException("Only owner of record is able to edit");
        }
        fillAndSaveRecord(recordDTO, record, logged);
    }

    private void fillAndSaveRecord(RecordDTO recordDTO, CalendarRecordEntity record, UserEntity logged) {
        if (recordDTO.getDateTimeFinish().isBefore(recordDTO.getDateTimeStart())) {
            throw new WrongInputException("Finish date must be after the start");
        }
        if (recordDTO.getSubjectId() != null) {
            SubjectEntity subject = subjectRepository.findById(recordDTO.getSubjectId()).orElse(null);
            if (subject == null) {
                throw new DoesNotExistException("Subject " + recordDTO.getSubjectId() + " does not exist");
            }
            record.setSubject(subject);
        }
        record.setName(recordDTO.getName());
        record.setDateTimeStart(recordDTO.getDateTimeStart());
        record.setDateTimeFinish(recordDTO.getDateTimeFinish());
        record.setRecordType(recordDTO.getRecordType());
        record.setRepeat(recordDTO.getRepeat());
        record.setUser(logged);
        recordRepository.save(record);
    }

    public void removeRecord(Long recordId) {
        Objects.requireNonNull(recordId);
        CalendarRecordEntity record = recordRepository.findById(recordId).orElse(null);
        if (record == null) {
            throw new DoesNotExistException("Record " + recordId + " does not exist" );
        }
        UserEntity logged = sessionService.getSession().getUser();
        if (!record.getUser().getId().equals(logged.getId())){
            throw new PermissionException("Only owner of record is able to delete it");
        }
        recordRepository.delete(record);
    }

    private void checkNotNullCreateRecordDTO(RecordDTO recordDTO) {
        Objects.requireNonNull(recordDTO);
        Objects.requireNonNull(recordDTO.getDateTimeFinish());
        Objects.requireNonNull(recordDTO.getDateTimeStart());
        Objects.requireNonNull(recordDTO.getRecordType());
        Objects.requireNonNull(recordDTO.getName());
    }
}
