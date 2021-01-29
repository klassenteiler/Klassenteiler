import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { Form, FormControl, Validators } from '@angular/forms';
import { SelfReportedInEdit } from '../../teacher-clean-up.models';
import { timer } from 'rxjs';
import { ForbiddenNameValidator } from 'src/app/_shared/forbidden-name.directive';

@Component({
  selector: 'app-student-detail',
  templateUrl: './student-detail.component.html',
  styleUrls: ['./student-detail.component.css']
})
export class StudentDetailComponent implements OnInit {

  @Input() studentEntity!: SelfReportedInEdit;
  @Input() currentClassListNames!: string[];

  @Output() shouldBeDeleted  = new EventEmitter<boolean>();
  @Output() classListChanged = new EventEmitter<void>();

  sleeping: boolean = false;

  editMode: boolean = false;

  formControl!: FormControl;

  constructor() { }

  ngOnInit(): void {
    const validator = new ForbiddenNameValidator(this.currentClassListNames)
    this.formControl = new FormControl("", [
      Validators.required,
      validator.func.bind(validator)
    ]);
  }

  toggleEdit(){
      if(this.editMode){
        this.save()
      }else{
        this.edit()
      }
  }

  sleep(){
    this.sleeping = true;
    timer(200).subscribe(val => {this.sleeping = false;})
  }

  save(){
    if(this.editMode && !this.sleeping){
      if(this.formControl.valid){
        this.studentEntity.name = this.formControl.value;
        this.classListChanged.emit();
      }
      this.editMode = false;
      this.sleep();
    }
  }

  edit(){
    if(!this.editMode && !this.sleeping){
      this.formControl.setValue(this.studentEntity.name)
      this.editMode = true;
    }
  }

  delete(){
    const shouldBeDestructed: boolean = this.studentEntity.delete();
    if(shouldBeDestructed){
      this.shouldBeDeleted.emit(true);
    }
    this.classListChanged.emit();
  }

  recover(){
    this.studentEntity.recover();
    this.classListChanged.emit();
  }

}
