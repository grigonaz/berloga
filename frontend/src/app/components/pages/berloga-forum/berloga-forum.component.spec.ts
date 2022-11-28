import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BerlogaForumComponent } from './berloga-forum.component';

describe('BerlogaForumComponent', () => {
  let component: BerlogaForumComponent;
  let fixture: ComponentFixture<BerlogaForumComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ BerlogaForumComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(BerlogaForumComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
