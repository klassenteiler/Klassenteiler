import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MatchStudentsComponent } from './match-students.component';

describe('MatchStudentsComponent', () => {
  let component: MatchStudentsComponent;
  let fixture: ComponentFixture<MatchStudentsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ MatchStudentsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(MatchStudentsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
