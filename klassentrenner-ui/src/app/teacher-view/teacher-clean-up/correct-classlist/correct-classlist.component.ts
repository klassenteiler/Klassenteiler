import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { AbstractControl, FormControl, Validators } from '@angular/forms';
import { TeacherService } from 'src/app/_services/teacher.service';
import { ForbiddenNameValidator } from 'src/app/_shared/forbidden-name.directive';
import {  SelfReportedInEdit } from '../teacher-clean-up.models';

@Component({
  selector: 'app-correct-classlist',
  templateUrl: './correct-classlist.component.html',
  styleUrls: ['./correct-classlist.component.css']
})
export class CorrectClasslistComponent implements OnInit {

  @Input() classList!: Array<SelfReportedInEdit>;
  @Input() currentClassListNames!: string[];
  @Output() classListChanged = new EventEmitter<void>()

  newStudentControl!: FormControl;
  // newStudentControl = new FormControl("", [Validators.required, forbiddenNameValidator(["a", "b"])]);
  constructor() { }

  ngOnInit(): void {
    const validator = new ForbiddenNameValidator()
    this.newStudentControl = new FormControl("", [
      validator.func.bind(this) // TODO this is some very hacky shit
      // this.validateName.bind(this) // inside validateName, the keyword 'this' is set through bind, to the value of this at calling. yolo
      ]);
  }

  delete(i: number){
    this.classList.splice(i, 1)
    this.triggerClassListChanged();
  }

  add(){
    console.log(this.currentClassListNames)
    const name_to_add = this.newStudentControl.value
    if(this.newStudentControl.valid &&  name_to_add !== ""){
      const newStudent: SelfReportedInEdit = SelfReportedInEdit.makeTeacherAdded(name_to_add)
      this.newStudentControl.setValue("")

      this.classList.push(newStudent);
      this.triggerClassListChanged();
    }
  }

  triggerClassListChanged(){
    this.classListChanged.emit()
  }
}
