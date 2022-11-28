import { ActiveAccountServiceService } from './../../services/active-account.service';
import { environment } from '../../../../environments/environment';
import { Component, OnInit, ViewChild } from '@angular/core';
import {MessageService} from 'primeng/api';
import {
  ChatReturnDTO,
  DefaultService,
  ChatDTO,
  MessageDTO,
  UserDTO,
  ChatUserEntityDTO,
} from '@anona/berloga-api-client';
import { BerlogaDefaultComponent } from '../berloga-default/berloga-default.component';
import { HttpEventType, HttpRequest, HttpResponse, HttpClient } from '@angular/common/http';
import { FileUpload } from 'primeng/fileupload';

@Component({
  selector: 'app-berloga-chat',
  templateUrl: './berloga-chat.component.html',
  styleUrls: ['./berloga-chat.component.scss'],
  providers: [MessageService]
})
export class BerlogaChatComponent implements OnInit {

  @ViewChild('file') input: FileUpload;

  conversations: ChatDTO[];

  name: string;

  message: string;

  changedName:string;

  curChat: ChatReturnDTO;

  users: ChatUserEntityDTO[];

  findChatString: string;

  usersForInviting: UserDTO[];

  apiFilePath: string = null;

  displayCreateChat = false;
  displayAddMember = false;
  displayAllMembers = false;
  displayEditName = false;

  loggedUser: UserDTO = null;

  constructor(
    private service: DefaultService,
    private def: BerlogaDefaultComponent,
    private loggedS: ActiveAccountServiceService,
    private http: HttpClient
  ) {}

  async ngOnInit(): Promise<void> {
    this.apiFilePath = environment.API_PATH+"/files/add-file";
    this.service.chatControllerGetAllChatsGET().subscribe((data) => {
      this.conversations = data;
    });
    this.loggedUser = (await this.loggedS.getLoggedAccount());
  }

  showAddChatDialog() {
    this.displayCreateChat = true;
  }
  showAddMemberDialog() {
    this.displayAddMember = true;
  }
  showAllMembersDialog() {
    this.displayAllMembers = true;
  }
  showEditNameDialog() {
    this.displayEditName = true;
  }

  createChat() {
    let chatDTO = {
      chatName: this.name,
    } as ChatDTO;
    this.displayCreateChat = false;
    this.service.chatControllerCreateChatPOST(chatDTO).subscribe((data) => {
      this.def.showSuccess(data.status as string)
      this.service.chatControllerGetAllChatsGET().subscribe((data) => {
        this.conversations = data;
      });
    });
  }

  getChat(id: any) {
    this.service.chatControllerGetChatGET(id).subscribe((data) => {
      console.log(data);
      this.curChat = data as ChatReturnDTO;
      this.users = this.curChat.users ? this.curChat.users : [];
      this.initUsers(id);
    });
  }

  sendMessage() {
    let messageDTO = {
      content: this.message,
    } as MessageDTO;
    this.service
      .chatControllerSendMessagePOST(this.curChat.chatId as number, messageDTO)
      .subscribe((data) => {
        this.service
          .chatControllerGetChatGET(this.curChat.chatId as number)
          .subscribe((data) => {
            this.curChat = data as ChatReturnDTO;
          });
        this.message = '';
      });
  }

  addMember(id: number) {
    this.service
      .chatControllerAddMemberPOST(this.curChat.chatId as number, id)
      .subscribe((data) => {
        this.getChat(this.curChat.chatId);
      }, (error) => {
        this.def.showError(error.error);
      } );
  }

  removeMember(id: number) {
    this.service
      .chatControllerRemoveMemberDELETE(this.curChat.chatId as number, id)
      .subscribe((data) => {
        this.getChat(this.curChat.chatId);
      }, (error) => {
        this.def.showError(error.error);
      } );
  }

  initUsers(id: number) {
    this.service.chatControllerFindUsersNotInChatGET(id).subscribe((data) => {
      this.usersForInviting = data;
      console.log(this.usersForInviting);

    });
  }

  searchChat() {
    if (this.findChatString !== '') {
      this.service
        .chatControllerSearchChatsGET(this.findChatString)
        .subscribe((data) => {
          this.conversations = data;
        });
    } else {
      this.service.chatControllerGetAllChatsGET().subscribe((data) => {
        this.conversations = data;
      });
    }
  }

  changeChatName(){
    let chatDTO = {
      chatName: this.changedName,
      chatId: this.curChat.chatId
    } as ChatDTO
    this.service.chatControllerEditChatPOST(chatDTO).subscribe((data) =>{
      this.curChat.chatName = this.changedName;
      this.conversations.find(chat => chat.chatId == this.curChat.chatId)!.chatName = this.changedName;
      this.changedName = '';
    }, (error) => {
      this.def.showError(error.error);
    } );
  }

  who(message: MessageDTO) {
    return this.loggedUser.id == message.sender.id?'my-message':'other-message';
  }

  onBasicUpload(t: any) {

    console.log(t);
  }

  uploadFileAndSendMessageWithIt(event: any) {
    const formdata: FormData = new FormData();

    if(event.files[0].size > 1024*1024*16) {
      alert("Maximum file size is 16MB");
      this.input.clear();
      return;
    }

    formdata.append('file', event.files[0]);

    const req = new HttpRequest('POST', this.service.configuration.basePath+'/files/add-file', formdata, {
      reportProgress: true,
      responseType: 'json'
    });
    this.http.request(req).subscribe((ev: any) => {
      if (ev.type === HttpEventType.UploadProgress) {
        console.log(Math.round(100 * ev.loaded / ev.total));
      } else if (ev instanceof HttpResponse) {
        console.log('File is completely uploaded!', ev);
        let mess: MessageDTO = {
          fileId: ev.body["error"],
          fileName: event.files[0].name,
          content: null
        } as MessageDTO;
        this.service.chatControllerSendMessagePOST(this.curChat.chatId, mess).subscribe(acc => {
          //console.log(acc);
          this.service
          .chatControllerGetChatGET(this.curChat.chatId as number)
          .subscribe((data) => {
            this.curChat = data as ChatReturnDTO;
          });
        }, dec => {
          console.log(dec);
        });
      }
    });
    this.input.clear();
  }

  downlaodFile(fileid: string) {
    window.location.href = this.service.configuration.basePath+'/files/get-file/'+fileid;
  }
}
