import { query } from '@angular/animations';
import { Injectable } from '@angular/core';
import { stat } from 'fs';
import { Observable, zip } from 'rxjs';
import { flatMap, map, mergeMap } from 'rxjs/operators';
import { StudentT } from 'src/app/models';
import { TeacherService } from 'src/app/_services/teacher.service';
import { ClassTeacher, ClearLocalStudent, EncTools, SchoolClass } from 'src/app/_tools/enc-tools.service';
import { FriendReported2Match, MergingCommandsT, SelfReportedInEdit } from './teacher-clean-up.models';

@Injectable({
  providedIn: 'root'
})
export class MergeService {

  constructor(private teacherService: TeacherService) { }


  classListKey(schoolClass: SchoolClass): string{
    return `mCL${schoolClass.id!}`
  }

  friendRListKey(schoolClass: SchoolClass): string{
    return `mFR${schoolClass.id!}`
  }

  stateHashKey(schoolClass: SchoolClass): string{
    return `mH${schoolClass.id!}`
  }

  setStateHash(sCls: SchoolClass, stateHash: string){
    localStorage.setItem(this.stateHashKey(sCls), stateHash);
  }

  removeStateHash(sCls: SchoolClass){
    localStorage.removeItem(this.stateHashKey(sCls))
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

  saveState2local(schoolClass: SchoolClass, queryHash: string, classList: Array<SelfReportedInEdit>|null, students2Match: FriendReported2Match[]|null): boolean {
    // queryHash is the starting state that the caller believes is current, if it is outdated no saving will happen
    const stateIsNotOutdated: boolean = this.checkStateHash(schoolClass, queryHash)

    if(stateIsNotOutdated){
      var somethingWasSaved = false;
      // its ok to save

      if(classList !== null){
        const classListS: string = SelfReportedInEdit.students2JSON(classList);
        localStorage.setItem(this.classListKey(schoolClass), classListS)
        somethingWasSaved = true;
      }
      if(students2Match !== null){
        const students2MatchS: string = FriendReported2Match.array2JSON(students2Match);
        localStorage.setItem(this.friendRListKey(schoolClass), students2MatchS)
        somethingWasSaved = true;
      }

      if(!somethingWasSaved){throw new Error("saveState2local was called without any data to save")}

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

  clearStateInLocal(schoolClass: SchoolClass){
    localStorage.removeItem(this.classListKey(schoolClass))
    localStorage.removeItem(this.friendRListKey(schoolClass))
    this.removeStateHash(schoolClass)
  }

  // state here means a partial state of merging
  getStateFromLocal(schoolClass: SchoolClass): [Array<SelfReportedInEdit>, FriendReported2Match[]]{
    const classListS: string | null = localStorage.getItem(this.classListKey(schoolClass));
    if(classListS === null){ throw new Error("Can not get ClassList because it does not exist in local storage")}
    const classList: Array<SelfReportedInEdit> = SelfReportedInEdit.json2Students(classListS) 

    const toMatchListS: string | null = localStorage.getItem(this.friendRListKey(schoolClass));
    if(toMatchListS === null){ throw new Error("Can not get FriendReportedList because it does not exist in local storage")}
    const matchList: FriendReported2Match[] = FriendReported2Match.json2array(toMatchListS)

    return [classList, matchList]
  }

  getStudentsFromDb(schoolClass: SchoolClass, classTeacher: ClassTeacher): Observable<[string, StudentT[], StudentT[]]>{
    // hash of DB state, list of self reported students, list of friend reported students
    const selfReportedStudentsObs: Observable<StudentT[]> = this.teacherService.getSelfReportedEnc(schoolClass, classTeacher);
    const friendReportedStudentsObs: Observable<StudentT[]> = this.teacherService.getFriendReportedEnc(schoolClass, classTeacher);

    const allStudentsObs: Observable<[StudentT[], StudentT[]]> = zip(selfReportedStudentsObs, friendReportedStudentsObs)
    
    const out: Observable<[string, StudentT[], StudentT[]]> = allStudentsObs.pipe(map( ([selfRstudents, friendRstudents]: [Array<StudentT>, StudentT[]]) => {
      const summary: string = JSON.stringify(selfRstudents) + JSON.stringify(friendRstudents)
      const stateHash: string = EncTools.sha256(summary)

      return [stateHash, selfRstudents, friendRstudents]
    }));
    return out
  }

  // get current state of merging, i.e. [Array<studentsInEdit>, Array<Students2Match>]
  // also return the state hash, (the state hash summarises which backend state (expressed by list of self reported students) the current merging intermediate result is valid for)
  getMergeState(schoolClass: SchoolClass, classTeacher: ClassTeacher): Observable<[string, SelfReportedInEdit[], FriendReported2Match[]]>{
    const encryptedDBstudents = this.getStudentsFromDb(schoolClass, classTeacher)

    return encryptedDBstudents.pipe(map( ([stateHash, selfRstudentsEnc, friendRstudentsEnc]: [string, Array<StudentT>, StudentT[]]) => {
      const stateIsValid: boolean = this.checkStateHash(schoolClass, stateHash)

      let finalResultSelfReportedStudents2Edit: SelfReportedInEdit[];
      let finalResultFriendReportedStudents2Match: FriendReported2Match[]

      if(stateIsValid){
        const [editStudentsRecovered, matchStudentsRecovered]: [SelfReportedInEdit[], FriendReported2Match[]] = this.getStateFromLocal(schoolClass);

        finalResultSelfReportedStudents2Edit = editStudentsRecovered;
        finalResultFriendReportedStudents2Match = matchStudentsRecovered;
      }
      else{
        // state is not valid. make a new state

        const selfRstudents: ClearLocalStudent[] = classTeacher.arrayStudentT2arrayStudents(selfRstudentsEnc)
        const friendRstudents: ClearLocalStudent[] = classTeacher.arrayStudentT2arrayStudents(friendRstudentsEnc)

        const editStudentsNew = this.selfRstudentsToStudentEdit(selfRstudents)
        const matchStudents = this.friendRstudents2edit(friendRstudents)
        finalResultSelfReportedStudents2Edit = editStudentsNew
        finalResultFriendReportedStudents2Match = matchStudents
        // here I have to set the state

        this.setStateHash(schoolClass, stateHash)
        this.saveState2local(schoolClass, stateHash, finalResultSelfReportedStudents2Edit, finalResultFriendReportedStudents2Match)
      }
      return [stateHash, finalResultSelfReportedStudents2Edit, finalResultFriendReportedStudents2Match]
    }
    ))
  }

  selfRstudentsToStudentEdit(students: Array<ClearLocalStudent>): Array<SelfReportedInEdit>{
      const editStudents: Array<SelfReportedInEdit>  = students.map(s => {
        if (!s.selfReported){ throw new Error("tried to make a student edit out of a student that is not self reported")}
        return SelfReportedInEdit.makeSelfReported(s.decryptedName, s.id!)
      }
      )
      return editStudents
   }

   friendRstudents2edit(students: Array<ClearLocalStudent>): Array<FriendReported2Match>{
     return students.map(s=>{
       if(s.selfReported){ throw new Error("tried to make a matching student ouf of a self reported student")}
       return FriendReported2Match.makeFriendReported2Match(s.decryptedName, s.id!)
     })
   }


   submitMergeCommandsAndStartCalculation(schoolClass: SchoolClass, classTeacher: ClassTeacher, mergeCommands: MergingCommandsT): Observable<string>{
    return this.getStudentsFromDb(schoolClass, classTeacher).pipe(mergeMap(([hash, _selfR, _friendR]: [string, StudentT[], StudentT[]])=>{
      const hash_ok = this.checkStateHash(schoolClass, hash)
      if(!hash_ok){
        throw new Error("It seems the database has changed in the meantime")
        window.location.reload()
      }
      else{
        return this.teacherService.startCalculatingWithMerge(schoolClass, classTeacher, mergeCommands).pipe(map(s=> {
          this.clearStateInLocal(schoolClass)
          return s
        }))
      }
    }))
   }
}
