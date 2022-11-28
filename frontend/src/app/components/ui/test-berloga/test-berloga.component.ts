import { Component, Input, OnInit } from '@angular/core';

@Component({
  selector: 'app-test-berloga',
  templateUrl: './test-berloga.component.html',
  styleUrls: ['./test-berloga.component.scss']
})
export class TestBerlogaComponent implements OnInit {

  @Input() promenna: string;

  constructor() { }

  ngOnInit(): void {
  }

}
