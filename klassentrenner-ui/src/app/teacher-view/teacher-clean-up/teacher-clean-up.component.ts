import { Component, Input, OnInit } from '@angular/core';
import { ClassTeacher, SchoolClass } from 'src/app/_tools/enc-tools.service';

@Component({
  selector: 'app-teacher-clean-up',
  templateUrl: './teacher-clean-up.component.html',
  styleUrls: ['./teacher-clean-up.component.css']
})
export class TeacherCleanUpComponent implements OnInit {
  @Input() schoolClass!: SchoolClass;
  @Input() classTeacher!: ClassTeacher;

  constructor() { }

  ngOnInit(): void {
  }

}
