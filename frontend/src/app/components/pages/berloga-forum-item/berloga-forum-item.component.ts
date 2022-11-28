import { THIS_EXPR, ThrowStmt } from '@angular/compiler/src/output/output_ast';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Router } from '@angular/router';

import {
  CommentDTO,
  DefaultService,
  QuestionForumDTO,
  UserDTO,
} from '@anona/berloga-api-client';
import { ConfirmationService } from 'primeng/api';
import { BerlogaDefaultComponent } from '../berloga-default/berloga-default.component';

@Component({
  selector: 'app-berloga-forum-item',
  templateUrl: './berloga-forum-item.component.html',
  styleUrls: ['./berloga-forum-item.component.scss'],
  providers: [ConfirmationService],
})
export class BerlogaForumItemComponent implements OnInit {
  id: number = null;

  question: QuestionForumDTO;

  comment: string;

  displayAddComment = false;

  displayEditQuestion = false;

  displayDeleteComment = false;

  logged: UserDTO = null;

  commentToEdit: CommentDTO = null;

  constructor(
    private route: ActivatedRoute,
    private def: BerlogaDefaultComponent,
    private service: DefaultService,
    private router: Router,
    private confirmationService: ConfirmationService
  ) {}

  isModerator(element: UserDTO.RolesEnum) {
    return element === UserDTO.RolesEnum.Moderator;
  }
  ngOnInit(): void {
    this.route.params.subscribe((params) => {
      this.id = params['id'];
      this.getQuestion();
      this.getUser();
    });
  }

  showAddComment() {
    this.displayAddComment = true;
  }

  showEditQuestion() {
    this.displayEditQuestion = true;
  }

  showDeleteComment() {
    this.displayDeleteComment = true;
  }

  getQuestion() {
    this.service.forumControllerGetQuestionGET(this.id).subscribe(
      (data) => {
        this.question = data as QuestionForumDTO;
      },
      (err) => {
        this.def.showError(err.error);
        this.router.navigate(['portal/forum']);
      }
    );
  }

  addComment() {
    let comment = {
      content: this.comment,
      questionId: this.id,
    } as CommentDTO;
    this.service.forumControllerCreateCommentPOST(comment).subscribe(
      (data) => {
        this.def.showSuccess(data.status);
        this.getQuestion();
        this.displayAddComment = false;
      },
      (err) => {
        this.def.showError(err.error);
      }
    );
  }

  confirmDeleteComment(id : number) {
    this.confirmationService.confirm({
      message: 'Are you sure that you want to delete comment?',
      header: 'Confirmation',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.service
          .forumControllerRemoveCommentDELETE(id)
          .subscribe(
            (data) => {
              this.def.showSuccess(data.status);
              this.getQuestion();
            },
            (err) => {
              this.def.showError(err.error);
            }
          );
      }
    });
  }

  confirmDeleteQuestion() {
    this.confirmationService.confirm({
      message: 'Are you sure that you want to delete question?',
      header: 'Confirmation',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.deleteQuestion();
      }
    });
  }

  deleteComment() {}

  getUser() {
    this.service.userControllerGetLoggedGET().subscribe((data) => {
      this.logged = data as UserDTO;
    });
  }

  markAsResolved() {
    this.service.forumControllerMarkDonePOST(this.question.id).subscribe(
      (data) => {
        this.def.showSuccess(data.status);
        this.getQuestion();
      },
      (err) => {
        this.def.showError(err.error);
      }
    );
  }

  editQuestion() {
    console.log(this.question.id);
    this.service.forumControllerEditQuestionPOST(this.question).subscribe(
      (data) => {
        this.def.showSuccess(data.status);
        this.getQuestion();
        this.displayEditQuestion = false;
      },
      (err) => {
        this.def.showError(err.error);
      }
    );
  }

  deleteQuestion() {
    this.service
      .forumControllerRemoveQuestionDELETE(this.question.id)
      .subscribe(
        (data) => {
          this.def.showSuccess(data.status);
          this.router.navigate(['portal/forum']);
        },
        (err) => {
          this.def.showError(err.error);
        }
      );
  }
}
