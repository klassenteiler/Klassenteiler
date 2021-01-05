import { Injectable } from '@angular/core';
import { ClassTeacher, ClearLocalStudent, SchoolClass, EncTools, impl } from '../_tools/enc-tools.service';
import { BackendService } from './backend.service';
import { concatMap, map, switchMap } from 'rxjs/operators';
import { ClassTeacherT, SchoolClassT, StudentT } from '../models';
import { Observable , from, defer, config} from 'rxjs';
import { ActivatedRoute, Router } from '@angular/router';
import { AppConfigService } from '../app-config.service';

@Injectable({
  providedIn: 'root'
})
export class SchoolClassService {

  constructor(private backendService: BackendService, private config: AppConfigService) { }

  submitStudentSurvey(schoolClass: SchoolClass, ownName: string, friendsNames: Array<string>){
    const payload = this.prepareStudentSurveySubmission(schoolClass, ownName, friendsNames);
    return this.backendService.submitStudentSurvey(payload, schoolClass.id!, schoolClass.classSecret )
    // console.log(payload)
  }

  prepareStudentSurveySubmission(schoolClass: SchoolClass, ownName: string, friendsNames: Array<string>){
    if( friendsNames.length > this.config.maxFriends){
      throw new Error("Trying to submit more friends than allowed");
    }

    const me: ClearLocalStudent = new ClearLocalStudent(ownName, true, undefined,  undefined)

    const friends: Array<ClearLocalStudent> = friendsNames.map((name:string)=>
      new ClearLocalStudent(name,  false, undefined, undefined)
    );

    const meEnc: StudentT = schoolClass.localStudentToTransport(me);
    const friendsEnc: Array<StudentT> = friends.map((student: ClearLocalStudent)=> schoolClass.localStudentToTransport(student));
    return {me: meEnc, friends: friendsEnc}
  }

  getClassFromRoute(route: ActivatedRoute): Observable<SchoolClass> {
    const classId: string | null = route.snapshot.paramMap.get('id')
    const classSecret: string |null = route.snapshot.paramMap.get('classSecret')

    if ((classId == null) || (classSecret == null)){
      throw new Error("Class ID or classSecret missing.")
    }
    const classObersvable: Observable<SchoolClassT> = this.backendService.getClass(classId, classSecret)

    const obs2: Observable<SchoolClass> = classObersvable.pipe(map(cls => SchoolClass.fromTransport(cls)))
    return obs2
  }

  // getCurrentUrl(): string{
  //   console.log(this.router.url)
  //   this.router.routerState.paramMap
  //   return this.router.url
  // }

  makeSchoolClass(schoolName: string, className:string): Observable<[string, SchoolClass, ClassTeacher]>{
    // let password: string;

    const pw = EncTools.createTeacherPassword(this.config.teacherPasswordLength)

    const pre1: Observable<[SchoolClass, ClassTeacher]> =  EncTools.makeClass(schoolName, className, pw, this.config.classSecretLength)

    const pre2 : Observable<[string, SchoolClass, ClassTeacher]> = pre1.pipe(concatMap(
      ([schoolClassLocal, clsTeach]:[SchoolClass, ClassTeacher]) =>
      {
        const backendProcessedObs: Observable<[string, SchoolClass, ClassTeacher]> =  this.backendService.createClassInBackend(
          schoolClassLocal.toTransport(), 
          clsTeach.toTransport(pw) // possibly some error handling has to go here as a pipe
          ).pipe(map((cls: SchoolClassT) => {
            const fullClass: SchoolClass = SchoolClass.fromTransport(cls); // has id etc filled out
            return [pw, fullClass, clsTeach]
        }))
        return backendProcessedObs
      }
    ));

    return pre2;

    // const createClassObs: Observable<[string, SchoolClass, ClassTeacher]> = new Observable((observer) => {

    //     console.log("starting slow function")
    //     const password = EncTools.createTeacherPassword();
    //     const [schoolClassLocal, clsTeach]: [SchoolClass, ClassTeacher] = EncTools.makeClass(schoolName, className, password)

    //     observer.next([password, schoolClassLocal, clsTeach])
    //     observer.complete()
    // });
      

    // const preOutput: Observable<[string, SchoolClass, ClassTeacher]> =  createClassObs.pipe(concatMap(
    //   ([password, schoolClassLocal, clsTeach]:[string, SchoolClass, ClassTeacher]) =>
    //   {
    //     const backendProcessedObs: Observable<[string, SchoolClass, ClassTeacher]> =  this.backendService.createClassInBackend(
    //       schoolClassLocal.toTransport(), 
    //       clsTeach.toTransport(password) // possibly some error handling has to go here as a pipe
    //       ).pipe(map((cls: SchoolClassT) => {
    //         const fullClass: SchoolClass = SchoolClass.fromTransport(cls); // has id etc filled out
    //         return [password, fullClass, clsTeach]
    //     }))
    //     return backendProcessedObs
    //   }));

    //   return preOutput
  }
}
