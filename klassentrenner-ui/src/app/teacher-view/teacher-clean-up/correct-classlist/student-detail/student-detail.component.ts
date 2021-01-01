import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormControl, Validators } from '@angular/forms';
import { Student4Edit } from '../../teacher-clean-up.models';

@Component({
  selector: 'app-student-detail',
  templateUrl: './student-detail.component.html',
  styleUrls: ['./student-detail.component.css']
})
export class StudentDetailComponent implements OnInit {

  @Input() studentEntity!: Student4Edit;
  @Output() shouldBeDeleted  = new EventEmitter<boolean>();

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
  }

  recover(){
    this.studentEntity.recover();
  }

}
