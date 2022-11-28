import { Component, Input, OnInit } from '@angular/core';

@Component({
  selector: 'app-subject-list-item',
  templateUrl: './subject-list-item.component.html',
  styleUrls: ['./subject-list-item.component.scss']
})
export class SubjectListItemComponent implements OnInit {

  @Input() name: string;
  @Input() code: string;

  constructor() { }

  ngOnInit(): void {
  }

}
