import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BerlogaLoginComponent } from './berloga-login.component';

describe('BerlogaLoginComponent', () => {
  let component: BerlogaLoginComponent;
  let fixture: ComponentFixture<BerlogaLoginComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ BerlogaLoginComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(BerlogaLoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
