package cz.cvut.fel.berloga.service;

import cz.cvut.fel.berloga.repository.*;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Profile("test")
@Configuration
public class MockitoConfig {

    @Bean
    @Primary
    public UserRepository userRepository() {
        return Mockito.mock(UserRepository.class);
    }

    @Bean
    @Primary
    public ChatRepository chatRepository() {
        return Mockito.mock(ChatRepository.class);
    }

    @Bean
    @Primary
    public SessionRepository sessionRepository() {
        return Mockito.mock(SessionRepository.class);
    }

    @Bean
    @Primary
    public ChatUserRepository chatUserRepository() {
        return Mockito.mock(ChatUserRepository.class);
    }

    @Bean
    @Primary
    public MessageRepository messageRepository() {
        return Mockito.mock(MessageRepository.class);
    }
}
