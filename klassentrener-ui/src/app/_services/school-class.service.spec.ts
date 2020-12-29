import { TestBed } from '@angular/core/testing';
import { from } from 'rxjs';
import { ClassTeacher, EncTools, SchoolClass } from '../_tools/enc-tools.service';
import { BackendService } from './backend.service';

import { SchoolClassService } from './school-class.service';

import * as forge from 'node-forge';
import { AppConfigService, MockAppConfigService } from '../app-config.service';

class MockBackendService{
}

describe('SchoolClassService', () => {
  let service: SchoolClassService;
  const passwordG = "testPassword2939"

  let schoolClass: SchoolClass|null =null;
  let classTeacher: ClassTeacher|null =null;

  beforeEach(async () => {
    const promise : Promise<[SchoolClass, ClassTeacher]> = EncTools.makeClass(
      "test school", "test class", passwordG
    ).toPromise();

    const [schoolClassTmp, classTeacherTmp]  = await promise;
    schoolClass = schoolClassTmp;
    classTeacher = classTeacherTmp;
  });

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        {provide: AppConfigService, useClass: MockAppConfigService},
        {provide: BackendService, useClass: MockBackendService}
      ]
    });
    service = TestBed.inject(SchoolClassService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should make the correct student submission', () => {
    const ownName = "its me";
    const friends: Array<string> = ["friend one", "friend two", "friend three"];

    const studentSubmission = service.prepareStudentSurveySubmission(schoolClass!, ownName, friends);
    console.log(studentSubmission);
  })

  it('trying the promises', () => {
    const promies = new Promise(resolve => {
      const key = forge.pki.rsa.generateKeyPair()
      const pw = EncTools.createTeacherPassword()
      resolve(pw);
    } )
    console.log('created promis')

    const obs = from(promies)
    console.log('created obs')


    obs.subscribe(data=>{
      console.log(`the password is ${data}`)
    })
  })

  // it('should execute the function asynchronously', () =>{
  //   const obs = service.makeSchoolClass('testName', 'testname')

  //   obs.subscribe((data=>
  //     console.log(data)))
  // });
});
