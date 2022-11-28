import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import {
  QuestionForumDTO,
  DefaultService,
  SubjectDTO,
} from '@anona/berloga-api-client';
import { BerlogaDefaultComponent } from '../berloga-default/berloga-default.component';
@Component({
  selector: 'app-berloga-forum',
  templateUrl: './berloga-forum.component.html',
  styleUrls: ['./berloga-forum.component.scss'],
})
export class BerlogaForumComponent implements OnInit {
  displayAddQuestion = false;
  question: string;
  subject: SubjectDTO;
  questions: QuestionForumDTO[] = [];
  searchFroumString: string = '';
  subjects: SubjectDTO[];

  constructor(
    private service: DefaultService,
    private def: BerlogaDefaultComponent,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadQuestions();
    this.loadSubjects();
  }

  loadQuestions() {
    this.service.forumControllerGetForumGET().subscribe((data) => {
      this.questions = data as QuestionForumDTO[];
    });
  }

  loadSubjects() {
    this.service.subjectControllerListAllSubjectsGET().subscribe((data) => {
      this.subjects = [...data];
    });
  }

  searchQuestion() {
    if (this.searchFroumString === '') {
      this.loadQuestions();
    } else {
      this.service
        .forumControllerGetForumWithSearchPOST(this.searchFroumString)
        .subscribe(
          (data) => {
            console.log(data);
            this.questions = data as QuestionForumDTO[];
          },
          (err) => {
            this.def.showError(err.error);
          }
        );
    }
  }

  addQuestion() {
    let questionDTO = {
      question: this.question,
      subjectId: this.subject ? this.subject.id : null,
    } as QuestionForumDTO;
    this.question = '';
    this.displayAddQuestion = false;
    this.service.forumControllerCreateQuestionPOST(questionDTO).subscribe(
      (data) => {
        this.def.showSuccess(data.status as string);
        this.loadQuestions();
      },
      (err) => {
        this.def.showError(err.error);
      }
    );
  }

  showAddQuestionDialog() {
    this.displayAddQuestion = true;
  }

  routeToQuestion(id: number) {
    this.router.navigate(['portal/question/' + id]);
  }
}
