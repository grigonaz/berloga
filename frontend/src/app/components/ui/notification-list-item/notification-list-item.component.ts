import { Component, Input, OnInit } from '@angular/core';
import { NotificationI } from '../../interfaces/NotificationI';

@Component({
  selector: 'app-notification-list-item',
  templateUrl: './notification-list-item.component.html',
  styleUrls: ['./notification-list-item.component.scss']
})
export class NotificationListItemComponent implements OnInit {

  @Input() item: NotificationI;

  constructor() { }

  ngOnInit(): void {
  }

}
