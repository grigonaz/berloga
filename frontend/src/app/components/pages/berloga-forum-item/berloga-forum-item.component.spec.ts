import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BerlogaForumItemComponent } from './berloga-forum-item.component';

describe('BerlogaForumItemComponent', () => {
  let component: BerlogaForumItemComponent;
  let fixture: ComponentFixture<BerlogaForumItemComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ BerlogaForumItemComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(BerlogaForumItemComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
