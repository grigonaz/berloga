package cz.cvut.fel.berloga.controller;

import cz.cvut.fel.berloga.controller.dto.RecordDTO;
import cz.cvut.fel.berloga.controller.dto.StatusDTO;
import cz.cvut.fel.berloga.controller.mappers.CalendarMapper;
import cz.cvut.fel.berloga.entity.CalendarRecordEntity;
import cz.cvut.fel.berloga.service.CalendarService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/calendar")
public class CalendarController {

    private final CalendarService calendarService;
    private final CalendarMapper calendarMapper;

    @Autowired
    public CalendarController(CalendarService calendarService, CalendarMapper calendarMapper) {
        this.calendarService = calendarService;
        this.calendarMapper = calendarMapper;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getCalendar() {
        List<CalendarRecordEntity> records = calendarService.getCalendar();
        return ResponseEntity.ok().body(records.stream()
                .map(calendarMapper::toDto)
                .collect(Collectors.toList()));
    }

    @PostMapping(path = "/create-record", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createRecord(@RequestBody RecordDTO recordDTO) {
        calendarService.createRecord(recordDTO);
        return ResponseEntity.ok(StatusDTO.builder().status("Record created").build());
    }

    @PostMapping(path = "/update-record", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateRecord(@RequestBody RecordDTO recordDTO) {
        calendarService.updateRecord(recordDTO);
        return ResponseEntity.ok(StatusDTO.builder().status("Record updated").build());
    }

    @DeleteMapping(path = "/remove-record/{recordId}")
    public ResponseEntity<?> removeRecord(@PathVariable Long recordId) {
        calendarService.removeRecord(recordId);
        return ResponseEntity.ok(StatusDTO.builder().status("Record deleted").build());
    }


}
