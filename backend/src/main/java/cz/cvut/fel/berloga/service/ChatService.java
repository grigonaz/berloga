package cz.cvut.fel.berloga.service;

import cz.cvut.fel.berloga.controller.dto.ChatDTO;
import cz.cvut.fel.berloga.controller.dto.MessageDTO;
import cz.cvut.fel.berloga.entity.*;
import cz.cvut.fel.berloga.entity.enums.UserChatRoleEnum;
import cz.cvut.fel.berloga.repository.ChatRepository;
import cz.cvut.fel.berloga.repository.ChatUserRepository;
import cz.cvut.fel.berloga.repository.MessageRepository;
import cz.cvut.fel.berloga.repository.UserRepository;
import cz.cvut.fel.berloga.service.exceptions.DoesNotExistException;
import cz.cvut.fel.berloga.service.exceptions.PermissionException;
import cz.cvut.fel.berloga.service.exceptions.UserAlreadyInChatException;
import cz.cvut.fel.berloga.service.exceptions.WrongMessageException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@CacheConfig(cacheNames = {"chat-cache"})
public class ChatService {

    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final ChatUserRepository chatUserRepository;
    private final UserRepository userRepository;
    private final SessionService sessionService;
    private final EntityManager em;
    private final FileService fileService;
    private final CacheManager cacheManager;

    @Autowired
    public ChatService(ChatRepository chatRepository, MessageRepository messageRepository, ChatUserRepository chatUserRepository, UserRepository userRepository, SessionService sessionService, EntityManager em, FileService fileService, CacheManager cacheManager) {
        this.chatRepository = chatRepository;
        this.messageRepository = messageRepository;
        this.chatUserRepository = chatUserRepository;
        this.userRepository = userRepository;
        this.sessionService = sessionService;
        this.em = em;
        this.fileService = fileService;
        this.cacheManager = cacheManager;
    }

    @Transactional
    public List<ChatEntity> getChats() {
        UserEntity logged = sessionService.getSession().getUser();
        return chatRepository.findAllByUserId(logged.getId());
    }


    @Transactional
    @Cacheable(condition = "@sessionService.isLogged()", key = "#id+'_'+@sessionService.getSession().id")
    public ChatEntity getChat(Long id) {
        this.sessionService.checkLogin();
        System.out.println("test-cachingu");
        ChatEntity chat = chatRepository.findById(id).orElse(null);
        if (chat == null) {
            throw new DoesNotExistException("Chat with id " + id + " not found");
        }

        UserEntity logged = sessionService.getSession().getUser();
        if (!isUserInChat(chat, logged)) {
            throw new PermissionException("User is not in chat");
        }
        // keep this
        //keep
        return chat;
    }

    @Transactional
    public List<ChatEntity> findByNameChats(String name) {
        UserEntity logged = sessionService.getSession().getUser();
        return this.chatRepository.findAllByNameContains(name).stream()
                .filter(c -> c.getParticipators().stream()
                        .anyMatch(u -> u.getUser().getId().equals(logged.getId())))
                .collect(Collectors.toList());
    }

    @Transactional
    public void createChat(ChatDTO chatDto) {
        Objects.requireNonNull(chatDto);
        Objects.requireNonNull(chatDto.getChatName());
        ChatEntity chatEntity = new ChatEntity();
        chatEntity.setName(chatDto.getChatName());
        chatEntity = chatRepository.save(chatEntity);
        UserEntity logged = sessionService.getSession().getUser();
        ChatUserEntity chatUserEntity = new ChatUserEntity();
        chatUserEntity.setUser(logged);
        chatUserEntity.setRole(UserChatRoleEnum.OWNER);
        chatUserEntity.setChat(chatEntity);
        chatUserRepository.save(chatUserEntity);
    }

    @CacheEvict(condition = "@sessionService.isLogged()", key = "#chatId+'_'+@sessionService.getSession().id")
    @Transactional
    public void removeChat(Long chatId) {
        ChatEntity chat = chatRepository.findById(chatId).orElse(null);
        if (chat == null) {
            throw new DoesNotExistException("Chat with id " + chatId + " doesn't exist");
        }
        UserEntity logged = sessionService.getSession().getUser();
        if (!isUserInChat(chat, logged)) {
            throw new PermissionException("User must be participator of chat");
        }
        if (!isUserOwner(chat, logged)) {
            throw new PermissionException("User must be owner of chat");
        }
        chatRepository.delete(chat);
    }

    @CacheEvict(condition = "@sessionService.isLogged()", key = "#chatDto.chatId+'_'+@sessionService.getSession().id")
    @Transactional
    public void editChat(ChatDTO chatDto) {
        Objects.requireNonNull(chatDto);
        Objects.requireNonNull(chatDto.getChatName());
        Objects.requireNonNull(chatDto.getChatId());
        ChatEntity chat = chatRepository.findById(chatDto.getChatId()).orElse(null);
        Objects.requireNonNull(chat);
        UserEntity logged = sessionService.getSession().getUser();
        if (!isUserInChat(chat, logged)) {
            throw new PermissionException("User must be participator of chat");
        }
        if (!isUserOwner(chat, logged)) {
            throw new PermissionException("User must be owner of chat");
        }
        chat.setName(chatDto.getChatName());
        chatRepository.save(chat);
    }

    @CacheEvict(condition = "@sessionService.isLogged()", key = "#chatId+'_'+@sessionService.getSession().id")
    @Transactional
    public void addMember(Long chatId, Long userId) {
        ChatEntity chat = chatRepository.findById(chatId).orElse(null);
        Objects.requireNonNull(chat);
        UserEntity logged = sessionService.getSession().getUser();
        if (!isUserInChat(chat, logged)) {
            throw new PermissionException("User must be participator of chat");
        }
        if (!isUserOwner(chat, logged)) {
            throw new PermissionException("User must be owner of chat");
        }
        UserEntity user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new DoesNotExistException("User with id" + userId + "doesn't exist");
        }
        ChatUserEntity foundUser = chat.getParticipators().stream()
                .filter(cu -> cu.getUser().getId().equals(userId))
                .findFirst()
                .orElse(null);
        if (foundUser != null) {
            throw new UserAlreadyInChatException("User " + foundUser.getUser().getUsername() + " is already in chat");
        }
        ChatUserEntity chatUserEntity = new ChatUserEntity();
        chatUserEntity.setChat(chat);
        chatUserEntity.setUser(user);
        chatUserEntity.setRole(UserChatRoleEnum.INVITED);
        chatUserRepository.save(chatUserEntity);
    }

    @CacheEvict(condition = "@sessionService.isLogged()", key = "#chatId+'_'+@sessionService.getSession().id")
    @Transactional
    public void removeMember(Long chatId, Long memberId) {
        ChatEntity chat = chatRepository.findById(chatId).orElse(null);
        Objects.requireNonNull(chat);
        UserEntity logged = sessionService.getSession().getUser();
        if (!isUserInChat(chat, logged)) {
            throw new PermissionException("User must be participator of chat");
        }
        if (!isUserOwner(chat, logged) && !memberId.equals(logged.getId())) {
            throw new PermissionException("User must be owner of chat or the user that is being removed");
        }
        ChatUserEntity foundCue = chat.getParticipators().stream()
                .filter(cue -> cue.getUser().getId().equals(memberId))
                .findFirst()
                .orElse(null);
        if (foundCue == null) {
            throw new DoesNotExistException("User is not in chat");
        }
        chatUserRepository.deleteById(foundCue.getId());
    }

    @CacheEvict(condition = "@sessionService.isLogged()", key = "#chatId+'_'+@sessionService.getSession().id")
    @Transactional
    public void sendMessage(Long chatId, MessageDTO messageDTO) {
        Objects.requireNonNull(messageDTO);
        if (messageDTO.getFileId() == null && (messageDTO.getContent() == null || messageDTO.getContent().length() == 0)) {
            throw new WrongMessageException("Message can not be empty");
        }
        ChatEntity chat = chatRepository.findById(chatId).orElse(null);
        if (chat == null) {
            throw new DoesNotExistException("Chat with id " + chatId + " doesn't exist");
        }
        UserEntity logged = sessionService.getSession().getUser();
        MessageEntity message = new MessageEntity();
        if(messageDTO.getFileId()!= null) {
            FileEntity fileEntity = this.fileService.getFileByPseudoId(messageDTO.getFileId());
            message.setFile(fileEntity);
        }
        message.setSender(logged);
        message.setText(messageDTO.getContent());
        message.setChat(chat);
        message.setDate(OffsetDateTime.now());
        message.setDeleted(false);
        chat.getMessages().add(message);
        messageRepository.save(message);
    }

    @Transactional
    public void editMessage(MessageDTO messageDto) {
        Objects.requireNonNull(messageDto);
        Objects.requireNonNull(messageDto.getId());
        MessageEntity messageEntity = messageRepository.findById(messageDto.getId()).orElse(null);
        if (messageEntity == null) {
            throw new DoesNotExistException("Message with id " + messageDto.getId() + " doesn't exist");
        }
        UserEntity logged = sessionService.getSession().getUser();
        if (!messageEntity.getSender().getId().equals(logged.getId())) {
            throw new PermissionException("User must be sender to edit message");
        }
        messageEntity.setText(messageDto.getContent());
        if(messageDto.getFileId()!= null) {
            FileEntity fileEntity = this.fileService.getFileByPseudoId(messageDto.getFileId());
            messageEntity.setFile(fileEntity);
        }
        this.cacheManager.getCache("chat-cache").clear();
        messageRepository.save(messageEntity);
    }

    @Transactional
    public void removeMessage(Long messageID) {
        Objects.requireNonNull(messageID);
        MessageEntity messageEntity = messageRepository.findById(messageID).orElse(null);
        if (messageEntity == null) {
            throw new DoesNotExistException("Message with id " + messageID + " doesn't exist");
        }
        UserEntity logged = sessionService.getSession().getUser();
        if (!messageEntity.getSender().getId().equals(logged.getId())) {
            throw new PermissionException("User must be sender to delete message");
        }
        this.cacheManager.getCache("chat-cache").clear();
        messageRepository.delete(messageEntity);
    }

    private boolean isUserOwner(ChatEntity chat, UserEntity logged) {
        ChatUserEntity chatUser = chat.getParticipators().stream()
                .filter(cue -> cue.getUser().getId().equals(logged.getId()))
                .findFirst()
                .orElse(null);
        Objects.requireNonNull(chatUser);
        return chatUser.getRole() == UserChatRoleEnum.OWNER;
    }

    private boolean isUserInChat(ChatEntity chat, UserEntity logged) {
        return chat.getParticipators().stream()
                .anyMatch(p -> p.getUser().getId().equals(logged.getId()));
    }

}