package cz.cvut.fel.berloga.service;

import cz.cvut.fel.berloga.entity.FileEntity;
import cz.cvut.fel.berloga.repository.FileRepository;
import cz.cvut.fel.berloga.service.exceptions.BerlogaException;
import cz.cvut.fel.berloga.service.exceptions.DoesNotExistException;
import cz.cvut.fel.berloga.service.exceptions.WrongInputException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class FileService {

    private final UserService userService;
    private final SessionService sessionService;
    private final FileRepository fileRepository;
    private final CryptoService cryptoService;

    @Value("${files-storage.url}")
    private String filesFolderPath;

    @Autowired
    public FileService(UserService userService, SessionService sessionService, FileRepository fileRepository, CryptoService cryptoService) {
        this.userService = userService;
        this.sessionService = sessionService;
        this.fileRepository = fileRepository;
        this.cryptoService = cryptoService;
    }

    @Transactional
    public String saveFile(MultipartFile file) {
        this.sessionService.checkLogin();
        try {
            System.out.println("Size1: "+file.getBytes().length);
            System.out.println("Name: "+file.getName());
            System.out.println("Type: "+file.getContentType());
            System.out.println("Filename: "+file.getOriginalFilename());
            System.out.println("Size2: "+file.getSize());
            //
            File f = new File(filesFolderPath);

            String pseudoName = this.cryptoService.generateRandomHexName(64);

            FileEntity fileEntity = new FileEntity();
            fileEntity.setName(file.getOriginalFilename());
            fileEntity.setSize(file.getSize());
            fileEntity.setPseudoName(pseudoName);
            fileEntity.setOwner(this.sessionService.getSession().getUser());

            // create file and transfer data to him
            File out = new File(f.getAbsolutePath()+"/"+pseudoName);
            out.createNewFile();
            file.transferTo(out);

            this.fileRepository.save(fileEntity);
            return fileEntity.getPseudoName();

        } catch (IOException ioException) {
            System.out.println(ioException.getMessage());
            System.out.println("unable to read byte length");
            throw new BerlogaException(ioException.getMessage());
        }
    }

    public List<FileEntity> listMyFiles() {
        this.sessionService.checkLogin();
        return this.fileRepository.findAllByOwner(this.sessionService.getSession().getUser());
    }

    public ResourceHolder getFile(String pseudoName) {
        this.sessionService.checkLogin();
        if(pseudoName==null||pseudoName.length()!=64) {
            throw new WrongInputException("Only valid file names");
        }
        FileEntity fileEntity = this.fileRepository.findFileEntityByPseudoNameEquals(pseudoName);
        if(fileEntity == null) {
            throw new DoesNotExistException("File not exist");
        }
        File f = new File(filesFolderPath+"/"+pseudoName);
        if(f.exists()) {
            ResourceHolder resourceHolder = new ResourceHolder();
            resourceHolder.resource = new FileSystemResource(f.getAbsolutePath());
            resourceHolder.fileEntity = fileEntity;
            return resourceHolder;
        } else {
            System.out.println("Hodně divné");
            return null;
        }
    }

    public FileEntity getFileByPseudoId(String pseudoName) {
        return this.fileRepository.findFileEntityByPseudoNameEquals(pseudoName);
    }

    public static class ResourceHolder {
        public Resource resource;
        public FileEntity fileEntity;
    }

}
