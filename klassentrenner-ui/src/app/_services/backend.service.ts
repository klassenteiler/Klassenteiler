import { Injectable } from '@angular/core';
import { ClassTeacherT, NumericValueT, SchoolClassT, StringMessageT, StudentT } from '../models';
import { SchoolClass } from '../_tools/enc-tools.service';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { AppConfigService } from '../app-config.service';
import { Observable, of } from 'rxjs';
import { map } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class BackendService {
  // this module only takes transport objects

  constructor(private http: HttpClient, private config: AppConfigService) { }

  createClassInBackend(schoolClass: SchoolClassT,  teacher: ClassTeacherT): Observable<SchoolClassT>{
    const data_to_send = {schoolClass: schoolClass, classTeacher: teacher}
    return this.http.post<SchoolClassT>(`${this.config.apiBaseUrl}/createClass`, data_to_send)
  }

  getClass(classId: string, classSecret: string): Observable<SchoolClassT> {
     const req = this.http.get<SchoolClassT>(`${this.config.apiBaseUrl}/getClass/${classId}/${classSecret}`).pipe(map(cls =>
        { 
          if ((cls.id === undefined) || (cls.surveyStatus === undefined)){
            throw new Error("Get Class returned a class with missing id or status")
          }
          return cls
        }
      )) 
     return req
  }

  submitStudentSurvey(payload:any, classId: number, classSecret:string){
    return this.http.post(`${this.config.apiBaseUrl}/submitStudentSurvey/${classId}/${classSecret}`, payload);
  }

  prepareTeacherGetRequest<T>(classId: number, classSecret: string, teacherSecret: string, functionName: string): Observable<T> {
    const headers = new HttpHeaders().set("teacherSecret", teacherSecret)
    const url: string = `${this.config.apiBaseUrl}/${functionName}/${classId}/${classSecret}`

    const req = this.http.get<T>(url, 
      {headers: headers}
    )
    return req
  }

  prepareTeacherPutRequest<T>(classId: number, classSecret: string, teacherSecret: string, functionName: string): Observable<T> {
    console.log('teacher put request')
    const headers = new HttpHeaders().set("teacherSecret", teacherSecret)
    const url: string = `${this.config.apiBaseUrl}/${functionName}/${classId}/${classSecret}`

    const req = this.http.put<T>(url, {}, {headers: headers}) // sending an empty body
    
    console.log(teacherSecret)
    console.log(headers)
    console.log(req)
    return req
  }

  teacherAuth(classId: number, classSecret: string, teacherSecret: string): Observable<ClassTeacherT> {
    // const headers = new HttpHeaders().set("teacherSecret", teacherSecret)
    // const req = this.http.get<ClassTeacherT>(`${this.config.apiBaseUrl}/teacherAuth/${classId}/${classSecret}`, 
    //   {headers: headers}
    // )
    return this.prepareTeacherGetRequest<ClassTeacherT>(classId, classSecret, teacherSecret, "teacherAuth");
  }

  nSignups(classId: number, classSecret: string, teacherSecret: string): Observable<NumericValueT> {
    return this.prepareTeacherGetRequest<NumericValueT>(classId, classSecret, teacherSecret, "nSignups");
  }

  closeSurvey(classId: number, classSecret: string, teacherSecret: string): Observable<StringMessageT> {
    return this.prepareTeacherPutRequest<StringMessageT>(classId, classSecret, teacherSecret, "closeSurvey");
  }

  getResults(classId: number, classSecret:string, teacherSecret: string): Observable<Array<StudentT>> {
    return this.prepareTeacherGetRequest<Array<StudentT>>(classId, classSecret, teacherSecret, "getResult");
  }

  getSelfReported(classId: number, classSecret:string, teacherSecret: string): Observable<Array<StudentT>> {
    return this.prepareTeacherGetRequest<Array<StudentT>>(classId, classSecret, teacherSecret, "getSelfReported");
  }
}
