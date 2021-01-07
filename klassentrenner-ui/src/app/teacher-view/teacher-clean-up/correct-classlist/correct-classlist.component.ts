import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormControl, Validators } from '@angular/forms';
import { TeacherService } from 'src/app/_services/teacher.service';
import {  SelfReportedInEdit } from '../teacher-clean-up.models';

@Component({
  selector: 'app-correct-classlist',
  templateUrl: './correct-classlist.component.html',
  styleUrls: ['./correct-classlist.component.css']
})
export class CorrectClasslistComponent implements OnInit {

  @Input() classList!: Array<SelfReportedInEdit>;
  @Output() classListChanged = new EventEmitter<void>()

  newStudentControl = new FormControl("", Validators.required);

  constructor() { }

  ngOnInit(): void {

  }

  delete(i: number){
    this.classList.splice(i, 1)
    this.triggerClassListChanged();
  }

  add(){
    if(this.newStudentControl.valid){
      const newStudent: SelfReportedInEdit = SelfReportedInEdit.makeTeacherAdded(this.newStudentControl.value)
      this.newStudentControl.setValue("")

      this.classList.push(newStudent);
      this.triggerClassListChanged();
    }
  }

  triggerClassListChanged(){
    this.classListChanged.emit()
  }

}
