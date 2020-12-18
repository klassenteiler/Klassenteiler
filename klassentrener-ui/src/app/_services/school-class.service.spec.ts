import { TestBed } from '@angular/core/testing';
import { from } from 'rxjs';
import { EncTools } from '../_tools/enc-tools.service';
import { BackendService } from './backend.service';

import { SchoolClassService } from './school-class.service';

import * as forge from 'node-forge';

class MockBackendService{

}

describe('SchoolClassService', () => {
  let service: SchoolClassService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [{provide: BackendService, useClass: MockBackendService}]
    });
    service = TestBed.inject(SchoolClassService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

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
