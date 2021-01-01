import { Component, Input, OnInit } from '@angular/core';
import { FormControl, Validators } from '@angular/forms';
import { TeacherService } from 'src/app/_services/teacher.service';
import { Student4Edit, TeacherAddedStudent } from '../teacher-clean-up.models';

@Component({
  selector: 'app-correct-classlist',
  templateUrl: './correct-classlist.component.html',
  styleUrls: ['./correct-classlist.component.css']
})
export class CorrectClasslistComponent implements OnInit {

  @Input() classList!: Array<Student4Edit>;
  newStudentControl = new FormControl("", Validators.required);

  constructor() { }

  ngOnInit(): void {

  }

  delete(i: number){
    this.classList.splice(i, 1)
  }

  add(){
    if(this.newStudentControl.valid){
      const newStudent: Student4Edit = new TeacherAddedStudent(this.newStudentControl.value)
      this.newStudentControl.setValue("")

      this.classList.push(newStudent);
    }
  }

}
