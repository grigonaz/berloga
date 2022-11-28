import { Observable } from 'rxjs';
import { DefaultService } from '@anona/berloga-api-client';
import { UserDTO } from 'src/api';
import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class ActiveAccountServiceService {

  private loggedAccount: UserDTO = null;
  private t: Observable<UserDTO> = null;

  constructor(private service: DefaultService) {
    this.refreshCall();
  }

  private refreshCall() {
    if(this.t != null) {
      return;
    }
    this.t = this.service.userControllerGetLoggedGET("body");
    this.t.subscribe(data => {
      this.loggedAccount = data;
      this.t = null;
    }, error => {
      if(error['status'] == 403) {
        console.log('You must log in');
      }
      console.log(error);
      this.t = null;
    });
  }

  public refresh() {
    this.refreshCall();
  }

  public async getLoggedAccount() {
    if(this.t != null) {
      await this.t.toPromise();
    }
    return this.loggedAccount;
  }
}
