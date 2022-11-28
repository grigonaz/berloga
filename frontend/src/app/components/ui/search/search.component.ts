import {
  Component,
  EventEmitter,
  forwardRef,
  Input,
  OnInit,
  Output,
} from '@angular/core';

@Component({
  selector: 'app-search',
  templateUrl: './search.component.html',
  styleUrls: ['./search.component.scss']
})
export class SearchComponent implements OnInit{
  @Input() placeholder: string;
  @Input() text: string;
  @Output() textChange = new EventEmitter<string>();

  constructor() { }

  ngOnInit(): void {
  }

  changeValue(event: any) {
    this.text = event;
    this.textChange.emit(event);
  }
}
