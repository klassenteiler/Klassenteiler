import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { FormArray, FormBuilder, FormControl, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { config, of, throwError } from 'rxjs';
import { catchError, retry } from 'rxjs/operators';
import { AppConfigService } from '../app-config.service';
import { SchoolClassSurveyStatus } from '../models';
import { SchoolClassService } from '../_services/school-class.service';
import { SchoolClass } from '../_tools/enc-tools.service';

@Component({
  selector: 'app-student-view',
  templateUrl: './student-view.component.html',
  styleUrls: ['./student-view.component.css']
})
export class StudentViewComponent implements OnInit {
  schoolClass! : SchoolClass;
  schoolClassSurveyStatus = SchoolClassSurveyStatus;
  maxFriends = this.config.maxFriends;

  errorMessage:string = "";
  successMessage: string = "";
  surveyOpen:boolean = true;

  studentSurvey =  this.fb.group({
    ownName: ['', Validators.required],
    friendsNames: this.fb.array([])
  })

  constructor(private route: ActivatedRoute, private fb: FormBuilder, private config: AppConfigService, private classService: SchoolClassService) { 

  }

  ngOnInit(): void {
    const tmpClass: SchoolClass = this.route.snapshot.data.schoolClass as SchoolClass;
    this.schoolClass = tmpClass
    if (this.schoolClass.id === undefined){
      throw new Error("undefined schoolClass.id in teacherView")
    }
    if (this.schoolClass.surveyStatus !== this.schoolClassSurveyStatus.open){
      this.errorAlreadyClosed();
    } else {
      this.surveyOpen = true;
    }
  }
  get friendsNames(){
    return this.studentSurvey.get('friendsNames') as FormArray;
  }

  addFriend(): void {
    //TODO add check that there are not to many friends
    // this.friendsNames.push("")
    this.friendsNames.push(this.fb.control('', Validators.required))
  }

  // updateName(event: Event, id: number): void{
  //   console.log(`update: ${id}`)
  //   console.log(event)
  //   const element = event.currentTarget as HTMLInputElement
  //   const value: string = element.value
  //   // const newName = !(event.target.value);
  //   console.log(value)
  //   // this.friendsNames[id] = value
  // }

  errorAlreadyClosed(){
    this.errorMessage = "Der Lehrer hat die Umfrage bereits beendet."
    this.surveyOpen = false;
  }

  getTeacherURL(): string{
    return `${this.config.frontendUrl}/${this.schoolClass.teacherURL}`
  }

  errorHandler(error: HttpErrorResponse){
    console.log("in error handler")
    console.log(error.error)
    console.log(error.status)
    if (error.status === 403){
      console.log('settnig error message')
      this.errorMessage = "Du hast dieses Formular bereits abgeschickt";
      this.surveyOpen = false;
    } else if ( error.status === 410) {
      this.errorAlreadyClosed();
    }
    // return throwError(error);
    // return of(null);
  }

  onSubmit(): void{
    console.log("submitted>>")
    console.log(this.studentSurvey.value)
    const ownName: string = this.studentSurvey.value.ownName as string;
    const friendsNames: Array<string> = this.studentSurvey.value.friendsNames ;
    this.classService.submitStudentSurvey(this.schoolClass, ownName, friendsNames).subscribe(
    result => {
      console.log("Successfull")
      console.log(result)
      this.surveyOpen = false;
      this.successMessage = "Vielen Dank. Deine Daten wurden übermittelt."
    },
    error => this.errorHandler(error)
    )
  }
  
  delete(id: number): void {
    this.friendsNames.removeAt(id)
  }

  check(): void {
    // console.log(this.friendsNames)
    console.log(this.studentSurvey.value)
  }

}
