import { ActiveAccountServiceService } from './../../services/active-account.service';
import { Router } from '@angular/router';
import { Component, OnInit } from '@angular/core';
import { DefaultService, SubjectDTO } from '@anona/berloga-api-client';

@Component({
  selector: 'app-berloga-subject',
  templateUrl: './berloga-subject.component.html',
  styleUrls: ['./berloga-subject.component.scss']
})
export class BerlogaSubjectComponent implements OnInit {

  subjectList: SubjectDTO[];
  adminVisibility: boolean = false;
  addSubjectDialog: boolean = false;
  createSubjectDTO: SubjectDTO = null;

  constructor(
    private service: DefaultService,
    private router: Router,
    private loggedService: ActiveAccountServiceService
    ) { }

  async ngOnInit() {
    this.createSubjectDTO = {} as SubjectDTO;
    this.service.subjectControllerListAllSubjectsGET()
    .subscribe((data) => {
      this.subjectList = data;
    });
    this.adminVisibility = (await this.loggedService.getLoggedAccount()).roles.find((item) => {
      return item == 'MODERATOR';
    }) != undefined;
  }

  routeTo(id: number) {
    this.router.navigate(['portal/subject/' + id])
  }

  addSubjectShow() {
    this.addSubjectDialog = true;
  }

  createSubject() {
    this.service.subjectControllerCreateSubjectPOST(this.createSubjectDTO).subscribe(returned => {
      // refresh
      this.addSubjectDialog = false;
      this.service.subjectControllerListAllSubjectsGET()
      .subscribe((data) => {
        this.subjectList = data;
      });
    });
  }

}
