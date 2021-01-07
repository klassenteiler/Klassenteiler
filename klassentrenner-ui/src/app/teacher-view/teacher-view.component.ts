import { Component, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { AppConfigService } from '../app-config.service';
import { SchoolClassSurveyStatus } from '../models';
import { SchoolClassService } from '../_services/school-class.service';
import { TeacherService } from '../_services/teacher.service';
import { ClassTeacher, SchoolClass } from '../_tools/enc-tools.service';

@Component({
  selector: 'app-teacher-view',
  templateUrl: './teacher-view.component.html',
  styleUrls: ['./teacher-view.component.css']
})
export class TeacherViewComponent implements OnInit {

  readonly  SchoolClassSurveyStatusLocal = SchoolClassSurveyStatus

  pwLength: number = 7;
  schoolClass: SchoolClass;

  classTeacher: ClassTeacher | null = null ; 

  errorString: string ="";

  constructor(private classService: SchoolClassService, 
    private teacherService: TeacherService,
     private route: ActivatedRoute,
     private title: Title,
     private config: AppConfigService,
     ) {
    const tmpClass: SchoolClass = this.route.snapshot.data.schoolClass as SchoolClass;
    this.schoolClass = tmpClass
    if (this.schoolClass.id === undefined){
      throw new Error("undefined schoolClass.id in teacherView")
    }
   }

  ngOnInit(): void {
    this.pwLength = this.config.teacherPasswordLength;
    this.classTeacher = this.teacherService.getLocalTeacher(this.schoolClass.id!)

    this.title.setTitle(`${this.schoolClass.name()} - Lehrer:in`)

    console.log(this.schoolClass)
  }

  getStudentURL(): string{
    return `${this.config.frontendUrl}/${this.schoolClass.studentURL}`
  }

  authenticateTeacher(password: string): void {
    this.teacherService.authenticateTeacher(this.schoolClass, password).subscribe((teach: ClassTeacher)=>{
      this.classTeacher= teach;
      console.log(this.classTeacher)
    },
    error => {
      if(error.status === 401){
        this.errorString = "Das Klassenpasswort ist falsch."
      }
      else{
        throw error
      }
    }
    )
  }
}
