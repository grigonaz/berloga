package cz.cvut.fel.berloga.controller.mappers;

import cz.cvut.fel.berloga.controller.dto.FileDTO;
import cz.cvut.fel.berloga.entity.FileEntity;
import org.mapstruct.Mapper;

@Mapper
public class FileMapper {

    public FileDTO toDTO(FileEntity fileEntity) {
        return FileDTO.builder().id(fileEntity.getId())
                .name(fileEntity.getName())
                .pseudoName(fileEntity.getPseudoName())
                .ownerID(fileEntity.getOwner().getId())
                .size(fileEntity.getSize())
                .build();
    }
}
