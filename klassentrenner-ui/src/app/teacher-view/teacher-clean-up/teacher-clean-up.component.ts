import { Component, Input, OnInit } from '@angular/core';
import { TeacherService } from 'src/app/_services/teacher.service';
import { ClassTeacher, ClearLocalStudent, SchoolClass } from 'src/app/_tools/enc-tools.service';
import { MergeService } from './merge.service';
import { FriendReported2Match } from './teacher-clean-up.models';
import { SelfReportedInEdit } from './teacher-clean-up.models';

@Component({
  selector: 'app-teacher-clean-up',
  templateUrl: './teacher-clean-up.component.html',
  styleUrls: ['./teacher-clean-up.component.css']
})
export class TeacherCleanUpComponent implements OnInit {
  @Input() schoolClass!: SchoolClass;
  @Input() classTeacher!: ClassTeacher;

  activeStep: number = 2;
  readonly lastStep: number = 3;

  classList: Array<SelfReportedInEdit>| null = null;
  friendRstudents: Array<FriendReported2Match> | null = null;

  // the current merging intermediate state is derived from a list of self reported students in the backend
  // this is summarised by the stateHash
  // it the list of self reported students in the backend changes, we can use this together with the stateHash
  // to realize that the intermediate result in this browser is bullshit
  stateHash: string = "";
  constructor(private mergeService: MergeService) { }

  ngOnInit(): void {
    //
      this.mergeService.getMergeState(this.schoolClass, this.classTeacher).subscribe(( [stateHash, selfRstudents, friendRstudents]: [string, SelfReportedInEdit[], FriendReported2Match[]]) =>{
        this.classList = selfRstudents;
        this.friendRstudents = friendRstudents;
        this.stateHash = stateHash;
      });
  }

  saveClassList() {
    console.log("saving class state")

    if(this.classList === null){throw new Error("cant save classList cause it's not defined.")}
    this.mergeService.saveState2local(this.schoolClass, this.stateHash, this.classList!, null)
  }

  saveFriendReportedMatching(){
    console.log("saving friend r state")
    if(this.friendRstudents === null){throw new Error("cant save friendR list cause its null")}
    this.mergeService.saveState2local(this.schoolClass, this.stateHash, null, this.friendRstudents!)
  }

  nextStep(){
    console.log(this.activeStep)

    if(this.activeStep < this.lastStep){
      this.activeStep += 1;
    }
  }
  prevStep(){
    if(this.activeStep > 1){
      this.activeStep -= 1;
    }
  }

  getCurrentClasslist(): string[]{
    if(this.classList === null){ throw new Error("no classlist")}
    return this.classList.filter((s: SelfReportedInEdit) => !s.deleted).map(s => s.name)
  }

}
