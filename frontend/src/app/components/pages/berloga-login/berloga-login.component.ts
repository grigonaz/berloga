import { Router } from '@angular/router';
import { DefaultService, LoginDTO, StatusDTO } from '@anona/berloga-api-client';
import { Component, OnInit } from '@angular/core';
import { ActiveAccountServiceService } from '../../services/active-account.service';

@Component({
  selector: 'app-berloga-login',
  templateUrl: './berloga-login.component.html',
  styleUrls: ['./berloga-login.component.scss']
})
export class BerlogaLoginComponent implements OnInit {

  email: String;
  password: String;

  constructor(private apiModule: DefaultService, private routing: Router, private loggedService: ActiveAccountServiceService) {
  }

  ngOnInit(): void {
    this.email = '';
    this.password = '';
  }

  login() {
    let userDTO = {
      username: this.email,
      password: this.password
    } as LoginDTO;
    this.apiModule.userControllerLoginUserPOST(userDTO)
    .subscribe((obj: StatusDTO) => {
      if(obj.status === 'logged') {
        this.loggedService.refresh();
        this.routing.navigate(['portal/welcome']);
      }
    });
  }
}
