import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormBuilder } from '@angular/forms';
import { By } from '@angular/platform-browser';
import { ActivatedRoute } from '@angular/router';
import { AppConfigService, MockAppConfigService } from '../app-config.service';
import { SchoolClassSurveyStatus } from '../models';
import { SchoolClassService } from '../_services/school-class.service';
import { ClassTeacher, EncTools, SchoolClass } from '../_tools/enc-tools.service';

import { StudentViewComponent } from './student-view.component';

class MockSchoolClassService{
  // constructor(private schoolClass: SchoolClass){}
}


class MockRoute  {
  snapshot: any;

  constructor(private cls: SchoolClass){
    this.snapshot = {data: { schoolClass: 
      cls
  }}
}

}

describe('StudentViewComponent', () => {
  let component: StudentViewComponent;
  let fixture: ComponentFixture<StudentViewComponent>;

  let schoolClass: SchoolClass;
  let teacher: ClassTeacher;

  beforeAll( async () => {
    await EncTools.makeClass("test school", "test class", "test password", 8).toPromise().then(
      ([sCls, teeach]: [SchoolClass,  ClassTeacher]) => {
        sCls.id = 23;
        sCls.surveyStatus = SchoolClassSurveyStatus.open;
        schoolClass = sCls;
        teacher = teeach;
      }
    );
  });

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ StudentViewComponent ],
      providers: [
        FormBuilder,
        {provide: AppConfigService, useClass: MockAppConfigService},
        {provide: SchoolClassService, useClass: MockSchoolClassService},
        {provide: ActivatedRoute, useValue: new MockRoute(schoolClass)}
      ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(StudentViewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  }); 

  it('should NOT render form when closed', () => {
    schoolClass.surveyStatus = SchoolClassSurveyStatus.closed;
    const fixture = TestBed.createComponent(StudentViewComponent);
    fixture.detectChanges();
    const element = fixture.debugElement.query(By.css('#studentSurvey'));
    expect(element).toBeFalsy();

    const element2 = fixture.debugElement.query(By.css('#error'));
    expect(element2).toBeTruthy();

    expect(element2.nativeElement.textContent.trim()).toContain("Der Lehrer hat die Umfrage bereits beendet")
  });

  it('should render form when open', () => {
    schoolClass.surveyStatus = SchoolClassSurveyStatus.open;
    const fixture = TestBed.createComponent(StudentViewComponent);
    fixture.detectChanges();
    const element = fixture.debugElement.query(By.css('#studentSurvey'));
    expect(element).toBeTruthy();

    const element2 = fixture.debugElement.query(By.css('#error'));
    expect(element2).toBeFalsy();
  });
  // TODO has to show closed when status is not open
  // TODO has to show closed when request failed with is closed
});
