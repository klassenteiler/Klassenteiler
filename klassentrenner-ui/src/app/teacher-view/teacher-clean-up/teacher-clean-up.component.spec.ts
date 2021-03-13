import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { Observable, of } from 'rxjs';
import { TeacherService } from 'src/app/_services/teacher.service';
import { ClassTeacher, SchoolClass } from 'src/app/_tools/enc-tools.service';
import { MergeService } from './merge.service';

import { TeacherCleanUpComponent } from './teacher-clean-up.component';
import { SelfReportedInEdit } from './teacher-clean-up.models';

class MockTeacherService{

}

class MockMergeService{

  getMergeState(schoolClass:SchoolClass, classTeacher: ClassTeacher): Observable<[string, SelfReportedInEdit[]]>{
    const students: Array<SelfReportedInEdit> = [
      SelfReportedInEdit.makeSelfReported( "Max Müller", 23),
      SelfReportedInEdit.makeSelfReported( "Peter Was", 24),
      SelfReportedInEdit.makeSelfReported( "Maria Dieter", 25),
      SelfReportedInEdit.makeTeacherAdded("Teacher Added")
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
        {provide: MergeService, useClass: MockMergeService},
        {provide: TeacherService, useClass: MockTeacherService}
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
