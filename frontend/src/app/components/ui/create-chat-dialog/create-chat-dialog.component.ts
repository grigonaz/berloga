import { Component, OnInit } from '@angular/core';
import {DialogModule} from 'primeng/dialog';


@Component({
  selector: 'app-create-chat-dialog',
  templateUrl: './create-chat-dialog.component.html',
  styleUrls: ['./create-chat-dialog.component.scss']
})
export class CreateChatDialogComponent implements OnInit {

  constructor() { }

  ngOnInit(): void {
  }

  display: boolean = false;

  showDialog() {
      this.display = true;
  }
}
