import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BerlogaMainPageComponent } from './berloga-main-page.component';

describe('BerlogaMainPageComponent', () => {
  let component: BerlogaMainPageComponent;
  let fixture: ComponentFixture<BerlogaMainPageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ BerlogaMainPageComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(BerlogaMainPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
