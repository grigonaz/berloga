package cz.cvut.fel.berloga.repository;

import cz.cvut.fel.berloga.entity.FileEntity;
import cz.cvut.fel.berloga.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, Long> {
    FileEntity findFileEntityByPseudoNameEquals(String name);
    List<FileEntity> findAllByOwner(UserEntity owner);
}
