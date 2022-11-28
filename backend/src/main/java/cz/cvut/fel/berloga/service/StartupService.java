package cz.cvut.fel.berloga.service;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.logging.LogEvent;
import com.hazelcast.logging.LogListener;
import com.hazelcast.logging.LoggingService;
import cz.cvut.fel.berloga.entity.PageEntity;
import cz.cvut.fel.berloga.entity.SubjectEntity;
import cz.cvut.fel.berloga.entity.UserEntity;
import cz.cvut.fel.berloga.entity.UserRoleEntity;
import cz.cvut.fel.berloga.entity.enums.UserRoleEnum;
import cz.cvut.fel.berloga.repository.SubjectRepository;
import cz.cvut.fel.berloga.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Objects;
import java.util.logging.Level;


@Service
@Slf4j
public class StartupService {

    private final UserService userService;
    private final CryptoService cryptoService;
    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;

    @Value("${files-storage.url}")
    private String filesFolderPath;

    @Autowired
    public StartupService(UserService userService, CryptoService cryptoService, UserRepository userRepository, SubjectRepository su) {
        this.userService = userService;
        this.cryptoService = cryptoService;
        this.userRepository = userRepository;
        this.subjectRepository = su;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void loaders() throws Exception {
        this.loadAdmin();
        this.loadExampleSubjects();
        this.clearFiles();
        System.out.println("Loading done......");
    }

    // load admin and test accounts
    public void loadAdmin() {
        System.out.println("Startup system");
        if(!this.userRepository.findById(1L).isPresent()) {
            // create administrator
            System.out.println("Creating default administrator");
            UserEntity admin = new UserEntity();
            admin.setUsername("administrator");
            admin.setFirstName("Admin");
            admin.setLastName("ad.");
            admin.setPasswordHash(this.cryptoService.generatePasswordHash("password"));
            admin.setEmail("default@test.cz");
            admin.setBlocked(false);
            admin.setRoles(Collections.singletonList(UserRoleEntity.builder().userRole(UserRoleEnum.MODERATOR).user(admin).build()));
            this.userRepository.save(admin);
            System.out.println("Creating default administrator");
            UserEntity user1 = new UserEntity();
            user1.setUsername("second");
            user1.setFirstName("second");
            user1.setLastName("sec.");
            user1.setPasswordHash(this.cryptoService.generatePasswordHash("second"));
            user1.setEmail("second@test.cz");
            user1.setBlocked(false);
            user1.setRoles(Collections.singletonList(UserRoleEntity.builder().userRole(UserRoleEnum.STUDENT).user(user1).build()));
            this.userRepository.save(user1);
            System.out.println("Creating default administrator");
            UserEntity user2 = new UserEntity();
            user2.setUsername("anona");
            user2.setFirstName("anona");
            user2.setLastName("zav.");
            user2.setPasswordHash(this.cryptoService.generatePasswordHash("fsociety"));
            user2.setEmail("nasedi@promin.cz");
            user2.setBlocked(false);
            user2.setRoles(Collections.singletonList(UserRoleEntity.builder().userRole(UserRoleEnum.MODERATOR).user(user2).build()));
            this.userRepository.save(user2);
        }
    }

    public void loadExampleSubjects() {
        SubjectEntity subjectEntity = new SubjectEntity();
        subjectEntity.setCode("B5566RD");
        subjectEntity.setDescription("Some normal description about the subject");
        subjectEntity.setName("Artwork a1 - beginners");
        this.subjectRepository.save(subjectEntity);
        SubjectEntity subjectEntity1 = new SubjectEntity();
        subjectEntity1.setCode("B5SSD");
        subjectEntity1.setDescription("Some normal description about the subject");
        subjectEntity1.setName("Artwork a2 - intermediate");
        this.subjectRepository.save(subjectEntity1);
        SubjectEntity subjectEntity2 = new SubjectEntity();
        subjectEntity2.setCode("C1236RD");
        subjectEntity2.setDescription("Some normal description about the subject");
        subjectEntity2.setName("Artwork a3 - experts");
        PageEntity p = new PageEntity();
        p.setSubject(subjectEntity);
        p.setData("# sem přijde text");
        subjectEntity.setPages(Collections.singletonList(p));
        this.subjectRepository.save(subjectEntity2);
    }

    private void clearFiles() {
        File f = new File(filesFolderPath);
        System.out.println(f.getAbsolutePath());
        if(!f.exists()||!f.isDirectory()) {
            System.out.println("Vytvářím složku");
            if(!f.mkdir()) {
                System.out.println("něco se pokazilo");
            }
        }
        for (File file : Objects.requireNonNull(f.listFiles())) {
            file.delete();
        }
    }



}
