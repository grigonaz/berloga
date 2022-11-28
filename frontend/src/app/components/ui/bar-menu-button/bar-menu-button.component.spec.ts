import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BarMenuButtonComponent } from './bar-menu-button.component';

describe('BarMenuButtonComponent', () => {
  let component: BarMenuButtonComponent;
  let fixture: ComponentFixture<BarMenuButtonComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ BarMenuButtonComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(BarMenuButtonComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
