import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BerlogaWelcomeComponent } from './berloga-welcome.component';

describe('BerlogaWelcomeComponent', () => {
  let component: BerlogaWelcomeComponent;
  let fixture: ComponentFixture<BerlogaWelcomeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ BerlogaWelcomeComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(BerlogaWelcomeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
