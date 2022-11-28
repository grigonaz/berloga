import { UserDTO } from 'src/api';
import { ActiveAccountServiceService } from './../../services/active-account.service';
import { RecordDTO } from './../../../../api/model/recordDTO.d';
import { SubjectExtendedDTO } from './../../../../api/model/subjectExtendedDTO.d';
import { ActivatedRoute } from '@angular/router';
import { DefaultService, SubjectDTO } from '@anona/berloga-api-client';
import { Component, OnInit } from '@angular/core';


@Component({
  selector: 'app-subject-page',
  templateUrl: './subject-page.component.html',
  styleUrls: ['./subject-page.component.scss']
})
export class SubjectPageComponent implements OnInit {

  id: number = null;
  name: string;
  subject: SubjectExtendedDTO;
  records: RecordDTO[];
  adminVisibility: boolean = false;
  content: string = "## ahoj \n ### Jak je?";
  isAdminOrTeacherInSubject: boolean = false;
  teachers: UserDTO[] = [];
  loggedUser: UserDTO = null;

  pagePart: string = 'DESCRIPTION';


  constructor(private service: DefaultService, private route: ActivatedRoute, private loggedService: ActiveAccountServiceService) {}

  async ngOnInit() {
    this.loggedUser = (await this.loggedService.getLoggedAccount());
    this.adminVisibility = this.loggedUser.roles.find((item) => {
      return item == 'MODERATOR';
    }) != undefined;

    this.route.params.subscribe((params) => {
      this.id = params['id'];
      console.log(this.id)
    });

    this.service.subjectControllerGetSubjectGET(this.id).subscribe((data) => {
      this.subject = data as SubjectExtendedDTO;
      this.teachers = this.subject.teachers;
      this.content = this.subject.pageData;
      this.isAdminOrTeacherInSubject = this.adminVisibility||this.teachers.find(a => { a.id == this.loggedUser.id})!= undefined;
    });
  }

  scrollTo(element:HTMLElement) {
    element.scrollIntoView();
  }

  getRecords() {
    this.service.calendarControllerGetCalendarGET().subscribe((data) => {
      this.records = data as RecordDTO[];
      this.records.filter(rec => rec.subjectId === this.subject.id);
    })
  }

  switchPage(page: 'DESCRIPTION'|'TEACHERS'|'DETAILS') {
    this.pagePart = page;
  }


  // createSubject() {
  //   let subjectDTO = {
  //     name: 'huita',
  //   } as SubjectDTO;

  //   this.service
  //     .subjectControllerCreateSubjectPOST(subjectDTO)
  //     .subscribe((data) => {
  //       console.log(data);
  //     });
  // }

  hidePreview() {

  }

  updatePage() {
    this.subject.pageData = this.content;
    this.service.subjectControllerEditSubjectPOST(this.subject.id, this.subject).subscribe(anyd => {
      this.service.subjectControllerGetSubjectGET(this.id).subscribe((data) => {
        this.subject = data as SubjectExtendedDTO;
        this.teachers = this.subject.teachers;
        this.isAdminOrTeacherInSubject = this.adminVisibility||this.teachers.find(a => { a.id == this.loggedUser.id})!= undefined;
      });
    });
  }

}
