import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { ClassTeacherT } from '../models';
import { ClassTeacher, SchoolClass } from '../_tools/enc-tools.service';
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

}
