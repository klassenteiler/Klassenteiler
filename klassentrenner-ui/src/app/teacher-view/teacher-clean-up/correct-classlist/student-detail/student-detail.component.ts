import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormControl, Validators } from '@angular/forms';
import { StudentInEdit } from '../../teacher-clean-up.models';

@Component({
  selector: 'app-student-detail',
  templateUrl: './student-detail.component.html',
  styleUrls: ['./student-detail.component.css']
})
export class StudentDetailComponent implements OnInit {

  @Input() studentEntity!: StudentInEdit;
  @Output() shouldBeDeleted  = new EventEmitter<boolean>();

  @Output() classListChanged = new EventEmitter<void>();

  editMode: boolean = false;

  formControl = new FormControl("", Validators.required);

  constructor() { }

  ngOnInit(): void {
  }

  toggleEdit(){
    if(this.editMode){
      this.save()
    }else{
      this.edit()
    }
  }

  save(){
    if(this.editMode){
      if(this.formControl.valid){
        this.studentEntity.name = this.formControl.value;
        this.classListChanged.emit();
      }
      this.editMode = false;
    }
  }

  edit(){
    if(!this.editMode){
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
