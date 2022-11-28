import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BerlogaDefaultComponent } from './berloga-default.component';

describe('BerlogaDefaultComponent', () => {
  let component: BerlogaDefaultComponent;
  let fixture: ComponentFixture<BerlogaDefaultComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ BerlogaDefaultComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(BerlogaDefaultComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
