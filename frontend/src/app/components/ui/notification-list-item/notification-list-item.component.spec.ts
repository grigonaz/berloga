import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NotificationListItemComponent } from './notification-list-item.component';

describe('NotificationListItemComponent', () => {
  let component: NotificationListItemComponent;
  let fixture: ComponentFixture<NotificationListItemComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ NotificationListItemComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(NotificationListItemComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
