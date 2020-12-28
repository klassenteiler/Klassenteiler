import { Component, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { SchoolClassStatus } from '../models';
import { SchoolClassService } from '../_services/school-class.service';
import { TeacherService } from '../_services/teacher.service';
import { ClassTeacher, SchoolClass } from '../_tools/enc-tools.service';

@Component({
  selector: 'app-teacher-view',
  templateUrl: './teacher-view.component.html',
  styleUrls: ['./teacher-view.component.css']
})
export class TeacherViewComponent implements OnInit {

  readonly  SchoolClassStatusLocal = SchoolClassStatus

  schoolClass: SchoolClass;

  classTeacher: ClassTeacher | null = null ; 

  constructor(private classService: SchoolClassService, 
    private teacherService: TeacherService,
     private route: ActivatedRoute,
     private title: Title,
     ) {
    const tmpClass: SchoolClass = this.route.snapshot.data.schoolClass as SchoolClass;
    this.schoolClass = tmpClass
    if (this.schoolClass.id === undefined){
      throw new Error("undefined schoolClass.id in teacherView")
    }
   }

  ngOnInit(): void {
    this.classTeacher = this.teacherService.getLocalTeacher(this.schoolClass.id!)

    this.title.setTitle(`${this.schoolClass.name()} - LehrerIn`)

    console.log(this.schoolClass)
  }

  authenticateTeacher(password: string): void {
    this.teacherService.authenticateTeacher(this.schoolClass, password).subscribe((teach: ClassTeacher)=>{
      this.classTeacher= teach;
      console.log(this.classTeacher)
    })
  }
}
