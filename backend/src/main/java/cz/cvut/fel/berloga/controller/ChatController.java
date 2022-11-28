package cz.cvut.fel.berloga.controller;

import cz.cvut.fel.berloga.controller.dto.*;
import cz.cvut.fel.berloga.controller.mappers.ChatMapper;
import cz.cvut.fel.berloga.controller.mappers.ChatReturnMapper;
import cz.cvut.fel.berloga.controller.mappers.MessageMapper;
import cz.cvut.fel.berloga.controller.mappers.UserMapper;
import cz.cvut.fel.berloga.entity.ChatEntity;
import cz.cvut.fel.berloga.entity.UserEntity;
import cz.cvut.fel.berloga.service.ChatService;

import cz.cvut.fel.berloga.service.UserService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;
    private final UserService userService;
    private final ChatMapper chatMapper;
    private final ChatReturnMapper chatReturnMapper;
    private final UserMapper userMapper;

    @Autowired
    public ChatController(ChatService chatService, UserService userService, ChatMapper chatMapper, ChatReturnMapper chatReturnMapper, UserMapper userMapper) {
        this.chatService = chatService;
        this.userService = userService;
        this.chatMapper = chatMapper;
        this.chatReturnMapper = chatReturnMapper;
        this.userMapper = userMapper;
    }

    @GetMapping(path = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ChatDTO>> getAllChats() {
        List<ChatEntity> chatList = chatService.getChats();
        List<ChatDTO> chatDTOList = chatList.stream().map(chatMapper::toDTO).collect(Collectors.toList());
        return ResponseEntity.status(200).body(chatDTOList);
    }

    @GetMapping(path = "/{chatId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ChatReturnDTO> getChat(@PathVariable Long chatId) {
        ChatEntity chatEntity = chatService.getChat(chatId);
        return ResponseEntity.status(200).body(chatReturnMapper.toDTO(chatEntity));
    }

    @PostMapping(path = "/create", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StatusDTO> createChat(@RequestBody ChatDTO chatDTO) {
        chatService.createChat(chatDTO);
        return ResponseEntity.ok(StatusDTO.builder().status("Chat created").build());
    }

    @DeleteMapping(path = "/delete/{chatId}")
    public ResponseEntity<StatusDTO> removeChat(@PathVariable Long chatId) {
        chatService.removeChat(chatId);
        return ResponseEntity.ok(StatusDTO.builder().status("Chat" + chatId + " deleted").build());
    }

    @PostMapping(path = "/edit-chat", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StatusDTO> editChat(@RequestBody ChatDTO chatDto) {
        chatService.editChat(chatDto);
        return ResponseEntity.ok(StatusDTO.builder().status("Chat edited").build());
    }

    @PostMapping(path = "/{chatId}/add-member/{userId}")
    public ResponseEntity<StatusDTO> addMember(@PathVariable Long chatId, @PathVariable Long userId) {
        chatService.addMember(chatId, userId);
        return ResponseEntity.ok(StatusDTO.builder().status("Member " + userId + " added to chat").build());
    }

    @DeleteMapping(path = "/{chatId}/remove-member/{userId}")
    public ResponseEntity<StatusDTO> removeMember(@PathVariable Long chatId, @PathVariable Long userId) {
        chatService.removeMember(chatId, userId);
        return ResponseEntity.ok(StatusDTO.builder().status("Member " + userId + " removed from chat").build());
    }

    @PostMapping(path = "/{chatId}/send-message", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StatusDTO> sendMessage(@PathVariable Long chatId, @RequestBody MessageDTO messageDTO) {
        chatService.sendMessage(chatId, messageDTO);
        return ResponseEntity.ok(StatusDTO.builder().status("Message sent").build());
    }

    @PostMapping(path = "/edit-message", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StatusDTO> editMessage(@RequestBody MessageDTO messageDTO) {
        chatService.editMessage(messageDTO);
        return ResponseEntity.ok(StatusDTO.builder().status("Message edited").build());
    }

    @DeleteMapping(path = "/remove/{messageId}")
    public ResponseEntity<StatusDTO> removeMessage(@PathVariable Long messageId) {
        chatService.removeMessage(messageId);
        return ResponseEntity.ok(StatusDTO.builder().status("Message deleted").build());
    }

    @GetMapping(path = "/find/{chat_name}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ChatDTO>> searchChats(@PathVariable String chat_name) {
        List<ChatEntity> chats = chatService.findByNameChats(chat_name);
        return ResponseEntity.status(200).body(chats.stream()
                .map(chatMapper::toDTO)
                .collect(Collectors.toList()));
    }

    @GetMapping(path = "/find-user-not-in-chat/{chat_id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserDTO>> findUsersNotInChat(@PathVariable Long chat_id) {
        List<UserEntity> users = userService.listUsers();
        return ResponseEntity.status(200).body(users.stream()
                .filter(u -> u.getChats().stream()
                        .noneMatch(cue -> cue.getChat().getId().equals(chat_id)))
                .map(userMapper::toDTO)
                .collect(Collectors.toList()));
    }
}
