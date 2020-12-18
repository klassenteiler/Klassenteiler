import { Injectable } from '@angular/core';
import { ClassTeacherT, SchoolClassT } from '../models';
import { SchoolClass } from '../_tools/enc-tools.service';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { AppConfigService } from '../app-config.service';
import { Observable, of } from 'rxjs';

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
     const req = this.http.get<SchoolClassT>(`${this.config.apiBaseUrl}/getClass/${classId}/${classSecret}`) 

     return req
  }
}
