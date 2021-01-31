import { Component, Input, OnInit } from '@angular/core';
import { merge } from 'rxjs';
import { ClassTeacher, ClearLocalStudent, SchoolClass } from 'src/app/_tools/enc-tools.service';
import { MergeService } from '../merge.service';
import { FriendReported2Match, MergeCommandsDict, SelfReportedInEdit } from '../teacher-clean-up.models';

@Component({
  selector: 'app-summary',
  templateUrl: './summary.component.html',
  styleUrls: ['./summary.component.css']
})
export class SummaryComponent implements OnInit {
  @Input() schoolClass!: SchoolClass;
  @Input() classTeacher!: ClassTeacher;

  @Input() classList! : SelfReportedInEdit[];
  @Input() friendReportedList!: FriendReported2Match[];

  mergeCommands: MergeCommandsDict| null = null;
  problems: string[] | null = null;

  constructor(private mergeService: MergeService) { }

  ngOnInit(): void {
    console.log("summary")
    console.log(this.classList)
    console.log(this.friendReportedList)

    this.buildMergeCommandsDict();
  }

  assert(b: boolean){
    if(!b){throw new Error("An assertion went wrong")}
  }

  buildMergeCommandsDict(){
    const toAdd: ClearLocalStudent[] = [];
    const toRename: ClearLocalStudent[] = [];
    const toDelete: number[] = [];

    this.classList.forEach((student: SelfReportedInEdit) => {
        if(student.teacherAdded){
          toAdd.push(new ClearLocalStudent(student.name, true))
        } else if (student.deleted){
          this.assert(!student.teacherAdded)
          toDelete.push(student.id)
        }
        else if (student.changed){
          this.assert(student.changed)
          toRename.push(new ClearLocalStudent(student.name, true, student.id))
        }
        else {
          console.log(`nothing was done with student ${student.name}`)
        }
    })

    const aliasList: Array<[number, string]> = [];

    const classListStrings: string[] = this.classList.filter(s=>!s.deleted).map(s => s.name)
    const isInClassList = function(s: string){return classListStrings.indexOf(s)!== -1}

    const problems: string[] = []

    this.friendReportedList.forEach((friendR: FriendReported2Match) => {
      if(friendR.wasMatched){
        if(isInClassList(friendR.matchedSelfReportedName)){
          aliasList.push([friendR.id, friendR.matchedSelfReportedName])
        } else {
          problems.push(`Der Name ${friendR.name} wurde dem Namen ${friendR.matchedSelfReportedName} zugeordnet. Dieser scheint nicht in der Klassenliste zu sein.`)
        }
      }
      else {
        // student was not matched but might be auto matched
        if(isInClassList(friendR.name)){
          // student was auto matched
          console.log(`ignored student ${friendR.name} as automatched`)
        }
        else if(friendR.shouldBeDeleted){
          toDelete.push(friendR.id)
        }
        else {
          problems.push(`Der Name ${friendR.name} wurde keinem Namen in der Klassenliste zugeordnet.`)
        }
      }
    })


    if(problems.length === 0){
      this.mergeCommands = new MergeCommandsDict(toAdd, toRename, toDelete, aliasList)
    }else{
      this.problems = problems
    }
  }


  submit(){
    if(this.mergeCommands === null){throw new Error("Cant submit")}

    const mergeCommandsT = this.mergeCommands.toTransport(this.schoolClass)
    console.log(this.mergeCommands)
    console.log(mergeCommandsT)

    this.mergeService.submitMergeCommandsAndStartCalculation(this.schoolClass, this.classTeacher, mergeCommandsT).subscribe(msg=>
      {
        console.log(msg)
        window.location.reload()
      })
  }
}
