import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TeacherCleanUpComponent } from './teacher-clean-up.component';

describe('TeacherCleanUpComponent', () => {
  let component: TeacherCleanUpComponent;
  let fixture: ComponentFixture<TeacherCleanUpComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ TeacherCleanUpComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TeacherCleanUpComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
