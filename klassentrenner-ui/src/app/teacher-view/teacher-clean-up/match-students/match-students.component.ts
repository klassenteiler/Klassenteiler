import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormArray, FormBuilder, FormControl } from '@angular/forms';
import { timeStamp } from 'console';
import { FriendReported2Match } from '../teacher-clean-up.models';
import * as stringSim from 'string-similarity';

@Component({
  selector: 'app-match-students',
  templateUrl: './match-students.component.html',
  styleUrls: ['./match-students.component.css']
})
export class MatchStudentsComponent implements OnInit {
  @Input() friendReportedList: FriendReported2Match[] | null = null;
  @Input() classList: string[] | null = null;
  @Output() friendReportedChanged = new EventEmitter<void>();


  matchingControls! : FormArray;
  numOfStudentsToMatch: number = 0;

  // City: string[] = ['Florida', 'South Dakota', 'Tennessee', 'Michigan']

  constructor() { }

  ngOnInit(): void {
    if(this.friendReportedList===null){throw new Error("no list of students ot match was provided")}
    if(this.classList === null){throw new Error("no classlist was provided")}

    const groups = this.friendReportedList.map(s => {
      if(s.wasMatched && !this.nameIsInClasslist(s.matchedSelfReportedName)){
        console.log("a matching is not valid any more")
        s.reset()
      }

      const setValue: number|string = _MatchOption.setValueFromStudent(s)
      return new FormControl(setValue)
      // here we always get a string but we might also need the option values
    })
    this.matchingControls = new FormArray(groups);

    this.numOfStudentsToMatch = this.numStudentsThatNeedMatching(this.friendReportedList)
  }

  numStudentsThatNeedMatching(friendRlist: FriendReported2Match[]): number {
    const listToMatch = friendRlist.filter(s => {
      return !this.nameIsInClasslist(s.name)
    })
    const num = listToMatch.length
    console.log(`number of students that need matching is ${num}`)
    return num
  }

  getControl(entityId: number) : FormControl {
    // const formId:number = this.entityId2FormId.get(entityId)!;

    return this.matchingControls.at(entityId) as FormControl;
  }

  getClassListForId(entityId: number): Array<_MatchOption>{
    //TODO sort by similarity
    if(this.friendReportedList === null){throw new Error('friendRlist == null')}

    const query: string = this.friendReportedList[entityId].name

    const ratings: Array<{'target': string, 'rating': number}> = stringSim.findBestMatch(query, this.classList!).ratings

    const sortedClassList: string[] = ratings.sort((left, right) => right.rating - left.rating ).map(s => s.target)

    const opts: _MatchOption[] =  sortedClassList.map(s => new _MatchOption(s, s))
    return [_MatchOption.unkown].concat(opts)
  }

  changeMatching(entityId: number, e: Event){
    const control: FormControl = this.getControl(entityId)

    const formValue: string |number = control.value;

    const s = this.friendReportedList![entityId]

     if(formValue === _MatchOption.deletionMarker){
        s.delete()
      }
      else if (formValue === _MatchOption.notYetSetMarker){
        throw new Error("Tried to change studentMatchinig to notYetMatched")
      }
      else {
        const name: string = <string>formValue;
        if(!this.nameIsInClasslist(name)){throw new Error("tried to change matching to unkown name")}
        s.matchedSelfReportedName = name
      }
    

    // this.friendReportedList![entityId].matchedSelfReportedName = control.value;

    this.triggerFriendRChange();
  }

  triggerFriendRChange(){
    this.friendReportedChanged.emit();
  }

  nameIsInClasslist(nname:string):boolean{
    return this.classList!.indexOf(nname) !== -1
  }

}

export class _MatchOption{
  static readonly deletionMarker: number = -1;
  static readonly notYetSetMarker: number = 0;

  constructor(public displayValue :string , public internalValue: string|number){}

  static get unkown(): _MatchOption{
    return new _MatchOption("UNBEKANNT", _MatchOption.deletionMarker);
  }

  static setValueFromStudent(s: FriendReported2Match): string|number{
    // returns the value that should be written into the form based on the student
    if(!s.hasBeenAssigned){
      return _MatchOption.notYetSetMarker
    }
    else if (s.shouldBeDeleted){
      return _MatchOption.deletionMarker
    }
    else{
      return s.matchedSelfReportedName
    }
  }


}