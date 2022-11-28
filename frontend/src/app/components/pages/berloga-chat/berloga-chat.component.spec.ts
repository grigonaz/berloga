import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BerlogaChatComponent } from './berloga-chat.component';

describe('BerlogaChatComponent', () => {
  let component: BerlogaChatComponent;
  let fixture: ComponentFixture<BerlogaChatComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ BerlogaChatComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(BerlogaChatComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
