package cz.cvut.fel.berloga.controller;

import cz.cvut.fel.berloga.controller.dto.StatusDTO;
import cz.cvut.fel.berloga.controller.dto.SubjectDTO;
import cz.cvut.fel.berloga.controller.dto.SubjectExtendedDTO;
import cz.cvut.fel.berloga.controller.dto.UserDTO;
import cz.cvut.fel.berloga.controller.mappers.SubjectMapper;
import cz.cvut.fel.berloga.entity.SubjectEntity;
import cz.cvut.fel.berloga.entity.UserSubjectEntity;
import cz.cvut.fel.berloga.service.SubjectService;
import cz.cvut.fel.berloga.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.cfg.NotYetImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/subject")
public class SubjectController {

    private final SubjectService subjectService;
    private final SubjectMapper subjectMapper;

    @Autowired
    public SubjectController(SubjectService subjectService, SubjectMapper subjectMapper) {
        this.subjectService = subjectService;
        this.subjectMapper = subjectMapper;
    }


    @GetMapping(path = "/{subject_id}")
    public SubjectExtendedDTO getSubject(@PathVariable Long subject_id) {
        return this.subjectMapper.toExtendedDTO(this.subjectService.getSubjectEntity(subject_id));
    }

    @PostMapping(path = "/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StatusDTO> createSubject(@RequestBody SubjectExtendedDTO subjectDTO) {
        this.subjectService.createSubject(subjectDTO);
        return ResponseEntity.ok().body(StatusDTO.builder().status("created").build());
    }

    @PostMapping(path = "/edit/{subject_id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StatusDTO> editSubject(@PathVariable Long subject_id, @RequestBody SubjectExtendedDTO subjectDTO) {
        subjectDTO.setId(subject_id);
        this.subjectService.editSubject(subjectDTO);
        return ResponseEntity.ok().body(StatusDTO.builder().status("edited").build());
    }

    @PostMapping(path = "/subscribe/{subject_id}/{user_id}/{type}")
    public ResponseEntity<StatusDTO> subscribeUser(@PathVariable Long subject_id ,@PathVariable Long user_id, @PathVariable String type) {
        this.subjectService.subscribeUser(subject_id, user_id, type);
        return ResponseEntity.ok().body(StatusDTO.builder().status("subscribed").build());
    }

    @PostMapping(path = "/unsubscribe/{subject_id}/{user_id}")
    public ResponseEntity<StatusDTO> unsubscribeUser(@PathVariable Long subject_id ,@PathVariable Long user_id) {
        this.subjectService.unsubscribeUser(subject_id, user_id);
        return ResponseEntity.ok().body(StatusDTO.builder().status("unsubscribed").build());
    }

    @GetMapping(path="/list-all/")
    public ResponseEntity<List<SubjectDTO>> listAllSubjects() {
        return ResponseEntity.ok(this.subjectService.listAllSubjects().stream().map(subjectMapper::toDTO).collect(Collectors.toList()));
    }

    @GetMapping(path="/list/{user_id}")
    public ResponseEntity<List<SubjectExtendedDTO>> listUsersSubjects(@PathVariable Long user_id) {
        return ResponseEntity.ok(this.subjectService.listSubjectOfUser(user_id).stream().map(subjectEntity ->
                this.subjectMapper.toExtendedDTO(subjectEntity, user_id)).collect(Collectors.toList()));
    }

    //?
    @PostMapping(path = "/add-teacher/{subject_id}/{user_id}")
    public ResponseEntity<StatusDTO> addTeacher(@PathVariable Long subject_id ,@PathVariable Long user_id) {
        throw new NotYetImplementedException("Not yet implemented");
    }

}
