import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { AppConfigService, MockAppConfigService } from '../app-config.service';
import { SchoolClassSurveyStatus } from '../models';
import { SchoolClassService } from '../_services/school-class.service';
import { TeacherService } from '../_services/teacher.service';
import { ClassTeacher, EncTools, SchoolClass } from '../_tools/enc-tools.service';

import { TeacherViewComponent } from './teacher-view.component';

class MockTeacher{

}

class MockRoute  {
  snapshot: any;

  constructor(private cls: SchoolClass){
    this.snapshot = {data: { schoolClass: 
      cls
  }}
}

}

class MockSchoolClassService{
  constructor(private schoolClass: SchoolClass){}

}

class MockTeacherService{
  getLocalTeacher(id: number){
    expect(id).toEqual(23);
    return new  MockTeacher();
  }
}


describe('TeacherViewComponent', () => {
  let component: TeacherViewComponent;
  let fixture: ComponentFixture<TeacherViewComponent>;

  let schoolClass: SchoolClass;
  let teacher: ClassTeacher;

  const clsSecretL = 8;
  const pwL=8;

  beforeAll( async () => {
    await EncTools.makeClass("test school", "test class", "test password", clsSecretL).toPromise().then(
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
      providers: [
        {provide: SchoolClassService, useValue: new MockSchoolClassService(schoolClass)},
        {provide: TeacherService, useClass: MockTeacherService},
        {provide: ActivatedRoute, useValue: new MockRoute(schoolClass)},
        {provide: AppConfigService, useClass: MockAppConfigService}
      ],
      declarations: [ TeacherViewComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TeacherViewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
