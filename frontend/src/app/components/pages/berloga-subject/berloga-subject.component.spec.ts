import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BerlogaSubjectComponent } from './berloga-subject.component';

describe('BerlogaSubjectComponent', () => {
  let component: BerlogaSubjectComponent;
  let fixture: ComponentFixture<BerlogaSubjectComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ BerlogaSubjectComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(BerlogaSubjectComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
