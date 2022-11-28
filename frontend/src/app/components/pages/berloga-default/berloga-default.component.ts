import { ActiveAccountServiceService } from './../../services/active-account.service';
import { DefaultService } from '@anona/berloga-api-client';
import { ActivatedRoute, Data, Router } from '@angular/router';
import { Component, OnInit } from '@angular/core';
import { MessageService } from 'primeng/api';

@Component({
  selector: 'app-berloga-default',
  templateUrl: './berloga-default.component.html',
  styleUrls: ['./berloga-default.component.scss'],
  providers: [MessageService],
})
export class BerlogaDefaultComponent implements OnInit {
  cesta: string;

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private messageService: MessageService,
    private service: DefaultService,
    private loggedService: ActiveAccountServiceService
  ) {
  }

  ngOnInit(): void {}

  showError(text: string) {
    this.messageService.add({
      severity: 'error',
      summary: 'Error',
      detail: text,
    });
  }

  showSuccess(text: string) {
    this.messageService.add({
      severity: 'success',
      summary: 'Success',
      detail: text,
    });
  }

  logout(): void {
    this.service.userControllerLogoutGET('response').subscribe(
      (data) => {
        this.loggedService.refresh();
        this.router.navigate(['']);
      },
      (error) => {
        console.log(error);
      }
    );
  }
}
