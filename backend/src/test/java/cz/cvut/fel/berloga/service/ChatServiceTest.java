package cz.cvut.fel.berloga.service;

import cz.cvut.fel.berloga.controller.dto.ChatDTO;
import cz.cvut.fel.berloga.controller.dto.MessageDTO;
import cz.cvut.fel.berloga.entity.ChatEntity;
import cz.cvut.fel.berloga.entity.ChatUserEntity;
import cz.cvut.fel.berloga.entity.SessionEntity;
import cz.cvut.fel.berloga.entity.UserEntity;
import cz.cvut.fel.berloga.entity.enums.UserChatRoleEnum;
import cz.cvut.fel.berloga.repository.ChatRepository;
import cz.cvut.fel.berloga.repository.MessageRepository;
import cz.cvut.fel.berloga.repository.SessionRepository;
import cz.cvut.fel.berloga.repository.UserRepository;
import cz.cvut.fel.berloga.service.exceptions.DoesNotExistException;
import cz.cvut.fel.berloga.service.exceptions.PermissionException;
import cz.cvut.fel.berloga.service.exceptions.UserAlreadyInChatException;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ChatServiceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ChatService chatService;

    private UserEntity user1;
    private UserEntity user2;
    private SessionEntity session;

    @Before
    public void setUp() {
        user1 = UserEntity.builder()
                .id(5L)
                .blocked(false)
                .email("test@mail.com")
                .lastName("aaa")
                .firstName("bbb")
                .passwordHash("aasdasd")
                .build();
        user2 = UserEntity.builder()
                .id(6L)
                .blocked(false)
                .email("test2@mail.com")
                .lastName("ccc")
                .firstName("ddd")
                .passwordHash("aasdasd")
                .build();

        session = SessionEntity.builder()
                .id(10L)
                .user(user1)
                .session("aaaa")
                .lastAccess(OffsetDateTime.now())
                .build();

        Mockito.when(sessionRepository.findAllBySession(Mockito.any()))
                .thenReturn(Collections.singletonList(session));
    }

    @Test
    public void t001_creatingChatWithNullNameThrowsNullPointerException() {
        ChatDTO chatDTO = new ChatDTO();
        assertThrows(NullPointerException.class, () -> chatService.createChat(chatDTO));
    }

    @Test
    public void t002_sendingMessageSendsMessageSuccessfullyWhenMessageIsCorrect() {
        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setContent("testContent");
        ChatEntity testChat = new ChatEntity();
        testChat.setId(5L);
        testChat.setMessages(new ArrayList<>());
        Mockito.when(chatRepository.findById(5L)).thenReturn(java.util.Optional.of(testChat));
        chatService.sendMessage(5L, messageDTO);
        assertEquals(1, testChat.getMessages().size());
    }

    @Test
    public void t003_addingPersonToChatThatAlreadyExistThereThrowsUserAlreadyInChatException() {
        ChatEntity testChat = new ChatEntity();
        testChat.setId(5L);
        testChat.setMessages(new ArrayList<>());
        List<ChatUserEntity> participators = new ArrayList<>();
        ChatUserEntity cue1 = new ChatUserEntity();
        cue1.setChat(testChat);
        cue1.setUser(user1);
        cue1.setRole(UserChatRoleEnum.OWNER);
        participators.add(cue1);
        ChatUserEntity cue2 = new ChatUserEntity();
        cue2.setChat(testChat);
        cue2.setUser(user2);
        cue2.setRole(UserChatRoleEnum.INVITED);
        participators.add(cue2);
        testChat.setParticipators(participators);

        Mockito.when(chatRepository.findById(5L)).thenReturn(java.util.Optional.of(testChat));
        Mockito.when(userRepository.findById(6L)).thenReturn(java.util.Optional.ofNullable(user2));

        assertThrows(UserAlreadyInChatException.class, () -> chatService.addMember(5L, 6L));
    }

    @Test
    public void t004_removeUserFromChatThrowsPermissionExceptionWhenUserIsNotOwnerOfChat() {
        ChatEntity testChat = new ChatEntity();
        testChat.setId(5L);
        testChat.setMessages(new ArrayList<>());
        List<ChatUserEntity> participators = new ArrayList<>();
        ChatUserEntity cue1 = new ChatUserEntity();
        cue1.setChat(testChat);
        cue1.setUser(user1);
        cue1.setRole(UserChatRoleEnum.INVITED);
        participators.add(cue1);
        ChatUserEntity cue2 = new ChatUserEntity();
        cue2.setChat(testChat);
        cue2.setUser(user2);
        cue2.setRole(UserChatRoleEnum.INVITED);
        participators.add(cue2);
        testChat.setParticipators(participators);

        Mockito.when(chatRepository.findById(5L)).thenReturn(java.util.Optional.of(testChat));
        Mockito.when(userRepository.findById(6L)).thenReturn(java.util.Optional.ofNullable(user2));

        assertThrows(PermissionException.class, () -> chatService.removeMember(5L, 6L));
    }

    @Test
    public void t005_removeChatThrowsDoesNotExistExceptionWhenChatWithIdDoesNotExist(){
        ChatEntity testChat = new ChatEntity();
        ChatDTO chatDTO = new ChatDTO();
        chatDTO.setChatName("testChatName");
        testChat.setId(5L);
        testChat.setMessages(new ArrayList<>());
        List<ChatUserEntity> participators = new ArrayList<>();
        ChatUserEntity cue1 = new ChatUserEntity();
        cue1.setChat(testChat);
        cue1.setUser(user1);
        cue1.setRole(UserChatRoleEnum.OWNER);
        participators.add(cue1);
        testChat.setParticipators(participators);
        chatService.createChat(chatDTO);
        Mockito.when(chatRepository.findById(5L)).thenReturn(Optional.empty());
        assertThrows(DoesNotExistException.class, ()-> chatService.removeChat(5L)) ;
    }
}
