import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FriendReported2Match } from '../teacher-clean-up.models';

import { MatchStudentsComponent } from './match-students.component';

describe('MatchStudentsComponent', () => {
  let component: MatchStudentsComponent;
  let fixture: ComponentFixture<MatchStudentsComponent>;

  const classList: string[] = ["Hans Peter", "Dieter Mühler"]
  const toMatchList: FriendReported2Match[] = [
    FriendReported2Match.makeFriendReported2Match("Hanss Peter", 12),
    FriendReported2Match.makeFriendReported2Match("Diter Müller", 12),
  ]

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ MatchStudentsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(MatchStudentsComponent);
    component = fixture.componentInstance;
    component.classList = classList;
    component.friendReportedList = toMatchList;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
