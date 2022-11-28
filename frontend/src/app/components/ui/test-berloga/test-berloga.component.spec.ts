import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TestBerlogaComponent } from './test-berloga.component';

describe('TestBerlogaComponent', () => {
  let component: TestBerlogaComponent;
  let fixture: ComponentFixture<TestBerlogaComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ TestBerlogaComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TestBerlogaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
