import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { Observable, of } from 'rxjs';
import { ClassTeacher, SchoolClass } from 'src/app/_tools/enc-tools.service';
import { MergeService } from './merge.service';

import { TeacherCleanUpComponent } from './teacher-clean-up.component';
import { StudentInEdit } from './teacher-clean-up.models';


class MockMergeService{

  getMergeState(schoolClass:SchoolClass, classTeacher: ClassTeacher): Observable<[string, StudentInEdit[]]>{
    const students: Array<StudentInEdit> = [
      StudentInEdit.makeSelfReported( "Max Müller", 23),
      StudentInEdit.makeSelfReported( "Peter Was", 24),
      StudentInEdit.makeSelfReported( "Maria Dieter", 25),
      StudentInEdit.makeTeacherAdded("Teacher Added")
    ]

    students[2].name = "changed Name"
    students[0].delete()
    return of(['testHash', students])
  }
}

describe('TeacherCleanUpComponent', () => {
  let component: TeacherCleanUpComponent;
  let fixture: ComponentFixture<TeacherCleanUpComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports:[NgbModule],
      providers: [
        {provide: MergeService, useClass: MockMergeService}
      ],
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
