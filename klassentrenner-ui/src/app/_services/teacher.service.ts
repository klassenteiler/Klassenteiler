import { HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, of, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { ClassTeacherT, StudentT } from '../models';
import { ClassTeacher, ClearLocalStudent, SchoolClass } from '../_tools/enc-tools.service';
import { BackendService } from './backend.service';

@Injectable({
  providedIn: 'root'
})
export class TeacherService {
  knownTeachers: Map<number, ClassTeacher> = new Map()

  constructor(private backendService: BackendService) { }

  localTeacherKey(classId: number): string {
    return `t${classId}`
  }

  storeTeacherLocally(teacher: ClassTeacher){
    if (teacher.id === undefined){
      throw new Error("Id undefined")
    }

    const teach_string = teacher.toJsonString()

    sessionStorage.setItem(this.localTeacherKey(teacher.id), teach_string)
    this.knownTeachers.set(teacher.id, teacher)
  }

  deleteLocalTeacher(clasId: number): boolean {

    sessionStorage.removeItem(this.localTeacherKey(clasId));
    this.knownTeachers.delete(clasId);
    return true;
  }

  getLocalTeacher(clasId: number){
    var clsTeach: ClassTeacher | null = null;
    if (this.knownTeachers.has(clasId)){
      clsTeach = this.knownTeachers.get(clasId)!
    }
    else {
      const maybeTeachString: string | null = sessionStorage.getItem(this.localTeacherKey(clasId)) 

      if (maybeTeachString !== null){
        clsTeach = ClassTeacher.fromJsonString(maybeTeachString)
        if (clsTeach.id !== clasId) {throw new Error("Teacher recovered from local store weirdly has a wrong id")}
        this.knownTeachers.set(clsTeach.id!, clsTeach)
      }
    }
    return clsTeach
  }

  authenticateTeacher(schoolClass: SchoolClass, password: string): Observable<ClassTeacher>{
    if (schoolClass.id === undefined){
      throw new Error("undefined id when trying to get class")
    }

    const teacherSecret = schoolClass.deriveTeacherSecret(password)

    const teacherReq: Observable<ClassTeacherT> = this.backendService.teacherAuth(
      schoolClass.id, schoolClass.classSecret, teacherSecret
    );
    
    const second: Observable<ClassTeacher> = teacherReq.pipe(map((teach: ClassTeacherT) =>{
       const teacherInstance = ClassTeacher.fromTransport(teach, password);
      this.storeTeacherLocally(teacherInstance);
      return teacherInstance
      }
     ));

     return second
  }


  nSignups(schoolClass: SchoolClass, teacher: ClassTeacher): Observable<number> {
    const req = this.backendService.nSignups(schoolClass.id!, schoolClass.classSecret, teacher.teacherSecret
      ).pipe(catchError(e=>this.handleTeacherError(schoolClass.id!, e))).pipe(
        map(data => {
        console.log(data);
        if (data.value === undefined){throw new Error("The server response for nSignups does not have a field 'value'")}
        return data.value
      }))

    return req
  }

  closeSurvey(schoolClass: SchoolClass, teacher: ClassTeacher): Observable<string>{
    return this.backendService.closeSurvey(schoolClass.id!, schoolClass.classSecret, teacher.teacherSecret).pipe(
      catchError(e=>this.handleTeacherError(schoolClass.id!, e))).pipe(
        map(data=>{
      if (data.message === undefined){ throw new Error("The server response for ccloseSurvey has no field message")}
      return data.message
    }))
  }

  startCalculatingWithMerge(schoolClass: SchoolClass, teacher: ClassTeacher, mergeBody: any): Observable<string>{
    return this.backendService.startCalculatingWithMerge(schoolClass.id!, schoolClass.classSecret, teacher.teacherSecret, mergeBody).pipe(
      catchError(e=>this.handleTeacherError(schoolClass.id!, e))).pipe(
        map(data=>{
      if (data.message === undefined){ throw new Error("The server response for ccloseSurvey has no field message")}
      return data.message
    }))
  }

  getFriendReported(schoolClass: SchoolClass, teacher:ClassTeacher): Observable<Array<ClearLocalStudent>>{
    const req: Observable<Array<ClearLocalStudent>> = this.backendService.getFriendReported(schoolClass.id!, schoolClass.classSecret, teacher.teacherSecret).pipe(
      catchError(e=> this.handleTeacherError(schoolClass.id!, e))
    ).pipe(map((data: Array<StudentT>) => 
      data.map(s => teacher.clearLocalStudentFromTransport(s))
    ))
    return req
  }

  getSelfReported(schoolClass: SchoolClass, teacher:ClassTeacher): Observable<Array<ClearLocalStudent>>{
    const req: Observable<Array<ClearLocalStudent>> = this.backendService.getSelfReported(schoolClass.id!, schoolClass.classSecret, teacher.teacherSecret).pipe(
      catchError(e=> this.handleTeacherError(schoolClass.id!, e))
    ).pipe(map((data: Array<StudentT>) => 
      data.map(s => teacher.clearLocalStudentFromTransport(s))
    ))
    return req
  }

  getResults(schoolClass: SchoolClass, teacher:ClassTeacher): Observable<Array<ClearLocalStudent>>{
    const req: Observable<Array<ClearLocalStudent>> = this.backendService.getResults(schoolClass.id!, schoolClass.classSecret, teacher.teacherSecret).pipe(
      catchError(e=> this.handleTeacherError(schoolClass.id!, e))
    ).pipe(map((data: Array<StudentT>) => 
      data.map(s => teacher.clearLocalStudentFromTransport(s))
    ))
    
    return req
  }

  handleTeacherError(classId: number, error: HttpErrorResponse){
    if (error.status === 401){
      console.log('teacher secret wrong error')
      // TODO how do I get the class id in here
      this.deleteLocalTeacher(classId)
      window.location.reload();
    }
    return throwError(error);
  }

}
