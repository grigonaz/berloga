package cz.cvut.fel.berloga.repository;

import static org.junit.jupiter.api.Assertions.*;
import cz.cvut.fel.berloga.BerlogaApplication;
import cz.cvut.fel.berloga.entity.UserEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ComponentScan(basePackageClasses = BerlogaApplication.class)
public class UserRepositoryTest {

    @Autowired
    private UserRepository ur;

    @Test
    public void findByUsernameReturnsPersonWithThisUsername() {
        UserEntity user = generateUser();
        ur.save(user);
        UserEntity result = ur.getByUsernameEquals(user.getUsername());

        assertNotNull(result);
        assertEquals(user.getId(), result.getId());

        ur.delete(user);
    }

    @Test
    public void findByEmailReturnsPersonWithThisEmail() {
        UserEntity user = generateUser();
        ur.save(user);
        UserEntity result = ur.getByEmailEquals(user.getEmail());

        assertNotNull(result);
        assertEquals(user.getId(), result.getId());

        ur.delete(user);
    }

    private static UserEntity generateUser() {
        UserEntity user = new UserEntity();
        user.setFirstName("Test");
        user.setLastName("Test");
        user.setUsername("Test");
        user.setPasswordHash("test123!");
        user.setEmail("testtest@gmail.com");
        user.setBlocked(false);

        return user;
    }
}

