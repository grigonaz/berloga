package cz.cvut.fel.berloga.controller;

import cz.cvut.fel.berloga.controller.dto.FileDTO;
import cz.cvut.fel.berloga.controller.dto.StatusDTO;
import cz.cvut.fel.berloga.controller.mappers.FileMapper;
import cz.cvut.fel.berloga.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@RestController
@RequestMapping("/files")
public class FileController {

    private final FileService fileService;
    private final FileMapper fileMapper;

    public FileController(FileService fileService, FileMapper fileMapper) {
        this.fileService = fileService;
        this.fileMapper = fileMapper;
    }

    @GetMapping(path = "/my-files", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<FileDTO>> listMyFiles() {
        return ResponseEntity.ok(this.fileService.listMyFiles().stream().map(this.fileMapper::toDTO).collect(Collectors.toList()));
    }

    @GetMapping(path = "/get-file/{id}")
    public ResponseEntity<Resource> getFile(@PathVariable String id) {
        FileService.ResourceHolder file = this.fileService.getFile(id);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\""
                + file.fileEntity.getName() + "\"").body(file.resource);
    }

    @PostMapping(path = "/add-file", consumes = MediaType.ALL_VALUE)
    public ResponseEntity<StatusDTO> addNewFile(@RequestBody MultipartFile file) {
        String code = this.fileService.saveFile(file);
        return ResponseEntity.ok(StatusDTO.builder().status("uploaded").error(code).build());
    }

    @PostMapping(path = "/delete-file/{id}")
    public ResponseEntity<StatusDTO> deleteFile(@PathVariable String id) {
        return ResponseEntity.ok(StatusDTO.builder().status("deleted").build());
    }
}
