import { query } from '@angular/animations';
import { Injectable } from '@angular/core';
import { stat } from 'fs';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { TeacherService } from 'src/app/_services/teacher.service';
import { ClassTeacher, ClearLocalStudent, EncTools, SchoolClass } from 'src/app/_tools/enc-tools.service';
import { StudentInEdit } from './teacher-clean-up.models';

@Injectable({
  providedIn: 'root'
})
export class MergeService {

  constructor(private teacherService: TeacherService) { }


  classListKey(schoolClass: SchoolClass): string{
    return `mCL${schoolClass.id!}`
  }

  stateHashKey(schoolClass: SchoolClass): string{
    return `mH${schoolClass.id!}`
  }

  setStateHash(sCls: SchoolClass, stateHash: string){
    localStorage.setItem(this.stateHashKey(sCls), stateHash);
  }

  checkStateHash(sCls: SchoolClass, queryHash: string): boolean {
    // returns true if the starting point of the merging operation had the same hash (of the current db state) as the query hash provided
    // if the hash is wrong clean up the local storage
    const val: string|null = localStorage.getItem(this.stateHashKey(sCls));
    const stateOK: boolean = val ? val===queryHash : false
    if(!stateOK){
      console.log(">>>> Had a wrong backend hash")
      localStorage.removeItem(this.stateHashKey(sCls))
      localStorage.removeItem(this.classListKey(sCls))
    }
    return stateOK
  }

  saveState2local(schoolClass: SchoolClass, queryHash: string, classList: Array<StudentInEdit>): boolean {
    // queryHash is the starting state that the caller believes is current, if it is outdated no saving will happen
    const stateIsNotOutdated: boolean = this.checkStateHash(schoolClass, queryHash)

    if(stateIsNotOutdated){
      // its ok to save
      const classListS: string = StudentInEdit.students2JSON(classList);

      localStorage.setItem(this.classListKey(schoolClass), classListS)
      return true;
    }
    else {
      // I am trying to save a state from a browser tab, that was started before a change in the backend occured
      // i.e. I should not overwrite a state that was saved with a different hash
      console.log("tried to overwrite a state that is derived from a different hash")
      // window.location.reload();
      this.windowReload();
      return false;
    }
  }

  windowReload(){
    window.location.reload();
  }

  getStateFromLocal(schoolClass: SchoolClass): Array<StudentInEdit>|null {
    const classListS: string | null = localStorage.getItem(this.classListKey(schoolClass));
    const classList: Array<StudentInEdit>|null = classListS ? StudentInEdit.json2Students(classListS) : null

    return classList
  }


  // get current state of merging, i.e. [Array<studentsInEdit>, Array<Students2Match>]
  // also return the state hash, (the state hash summarises which backend state (expressed by list of self reported students) the current merging intermediate result is valid for)
  getMergeState(schoolClass: SchoolClass, classTeacher: ClassTeacher): Observable<[string, StudentInEdit[]]>{
    const selfReportedStudentsObs: Observable<ClearLocalStudent[]> = this.teacherService.getSelfReported(schoolClass, classTeacher);

    return selfReportedStudentsObs.pipe(map( (students: Array<ClearLocalStudent>) => {
      const summary: string = JSON.stringify(students)
      const stateHash: string = EncTools.sha256(summary)

      const stateIsValid: boolean = this.checkStateHash(schoolClass, stateHash)

      let finalResultStudents: StudentInEdit[];

      if(stateIsValid){
        const editStudentsRecovered: StudentInEdit[]| null = this.getStateFromLocal(schoolClass);
        if(editStudentsRecovered === null){throw new Error("Could not read state from localStorage even though the hash was present and correct")}
        finalResultStudents = editStudentsRecovered;
      }
      else{
        // state is not valid. make a new state

        const editStudentsNew = this.studentsToStudentEdit(students)
        finalResultStudents = editStudentsNew
        // here I have to set the state

        this.setStateHash(schoolClass, stateHash)
        this.saveState2local(schoolClass, stateHash, finalResultStudents)
      }
      return [stateHash, finalResultStudents]
    }
    ))
  }

  studentsToStudentEdit(students: Array<ClearLocalStudent>): Array<StudentInEdit>{
      const editStudents: Array<StudentInEdit>  = students.map(s => 
        StudentInEdit.makeSelfReported(s.decryptedName, s.id!)
      )
      return editStudents
   }

}
