import { Component, Input, OnInit } from '@angular/core';

@Component({
  selector: 'app-bar-menu-button',
  templateUrl: './bar-menu-button.component.html',
  styleUrls: ['./bar-menu-button.component.scss']
})
export class BarMenuButtonComponent implements OnInit {

  @Input() text: string;

  constructor() { }

  ngOnInit(): void {
  }

}
