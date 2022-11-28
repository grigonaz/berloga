import { InviteUserDTO } from './../../../../api/model/inviteUserDTO.d';
import { ActiveAccountServiceService } from '../../services/active-account.service';
import { SubjectExtendedDTO } from './../../../../api/model/subjectExtendedDTO.d';
import { DefaultService, SubjectDTO } from '@anona/berloga-api-client';
import { Component, OnInit } from '@angular/core';
import { UserDTO } from 'src/api';

@Component({
  selector: 'app-berloga-account',
  templateUrl: './berloga-account.component.html',
  styleUrls: ['./berloga-account.component.scss']
})
export class BerlogaAccountComponent implements OnInit {

  searchText: string = null;
  listAccounts: UserDTO[] = [];
  selectedAccount: UserDTO = {};
  selectedAccountSubjects: SubjectExtendedDTO[] = [];
  allSubjects: SubjectDTO[] = [];
  allRoles: UserDTO.RolesEnum[];
  displaySubjects: boolean = false;
  displayRole: boolean = false;
  displayCreateUser: boolean = false;
  displayEditUser: boolean = false;
  createUserDTO: InviteUserDTO = {} as InviteUserDTO;
  editUserDTO: UserDTO = {} as UserDTO;
  adminVisibility: boolean = false;

  constructor(private service: DefaultService, private loggedService: ActiveAccountServiceService) { }

  async ngOnInit() {
    this.service.userControllerListUserGET().subscribe((data: UserDTO[]) => {
      this.listAccounts = data;
      this.loadProfile(this.listAccounts[0].id);
    });
    this.adminVisibility = (await this.loggedService.getLoggedAccount()).roles.find((item) => {
      return item == 'MODERATOR';
    }) != undefined;
  }

  searchAcconts() {
    console.log('Tralala');
  }

  loadProfile(id: number) {
    // TODO
    this.selectedAccount = this.listAccounts.find(obj => {
      return obj.id == id
    });
    this.editUserDTO = this.selectedAccount;
    this.editUserDTO.password = '';
    this.allRoles = (['MODERATOR', 'TEACHER', 'STUDENT'] as UserDTO.RolesEnum[]).filter((item) => {
      return this.selectedAccount.roles.indexOf(item) == -1;
    });
    this.service.subjectControllerListUsersSubjectsGET(this.selectedAccount.id).subscribe((data: SubjectDTO[]) => {
      this.selectedAccountSubjects = data;
      this.service.subjectControllerListAllSubjectsGET().subscribe((list) => {
        this.allSubjects = list.filter((value) => {
          if(data.findIndex((t) => {
            if(t.id == value.id) {
              return true;
            }
          }) != -1) {
            return false;
          } else {
            return true;
          }
        });
      });
    }, (error) => {
      console.log(error);
    });
  }

  addSubject(subjectId: number, type: string) {
    this.displaySubjects = false;
    this.service.subjectControllerSubscribeUserPOST(subjectId, this.selectedAccount.id, type).subscribe(data => {
      this.loadProfile(this.selectedAccount.id);
    });
  }

  deleteSubject(subjectId: number) {
    this.service.subjectControllerUnsubscribeUserPOST(subjectId, this.selectedAccount.id).subscribe(data => {
      this.loadProfile(this.selectedAccount.id);
    });
  }

  createUser() {
    console.log(this.createUserDTO);
    this.service.userControllerInviteUserPOST(this.createUserDTO).subscribe((data) => {
      if(data.status == 'user created') {
        this.createUserDTO = {} as InviteUserDTO;
        this.service.userControllerListUserGET().subscribe((data: UserDTO[]) => {
          this.listAccounts = data;
          this.loadProfile(this.listAccounts[0].id);
        });
      }
    }, (error) => {
      console.log(error);
    });
  }

  modifyUser() {
    console.log(this.editUserDTO);
    this.service.userControllerEditProfilePOST(this.editUserDTO).subscribe(() => {
      this.service.userControllerListUserGET().subscribe((data: UserDTO[]) => {
        this.listAccounts = data;
        this.loadProfile(this.selectedAccount.id);
      });
    }, error => {
      console.log(error);
    });
    this.displayEditUser = false;
  }

  deleteUser() {
    console.log(this.selectedAccount.id);
  }

  addRole(role: UserDTO.RolesEnum) {
    this.service.userControllerAddRolePOST(this.selectedAccount.id, role).subscribe((data)=>{
      this.displayRole = false;
      this.service.userControllerListUserGET().subscribe((data: UserDTO[]) => {
        this.listAccounts = data;
        this.loadProfile(this.selectedAccount.id);
      });
    }, (error) => {
      console.log(error);
    });
  }

  deleteRole(role: UserDTO.RolesEnum) {
    this.service.userControllerDeleteRolePOST(this.selectedAccount.id, role).subscribe((data)=>{
      this.service.userControllerListUserGET().subscribe((data: UserDTO[]) => {
        this.listAccounts = data;
        this.loadProfile(this.selectedAccount.id);
      });
    }, (error) => {
      console.log(error);
    });
  }

}
