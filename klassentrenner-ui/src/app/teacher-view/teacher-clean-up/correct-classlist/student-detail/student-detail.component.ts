import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { Form, FormControl, Validators } from '@angular/forms';
import { SelfReportedInEdit } from '../../teacher-clean-up.models';
import { timer } from 'rxjs';
import { ForbiddenNameValidator } from 'src/app/_shared/forbidden-name.directive';
import { OriginalClassListChecker } from '../original-classList-checker';

@Component({
  selector: 'app-student-detail',
  templateUrl: './student-detail.component.html',
  styleUrls: ['./student-detail.component.css']
})
export class StudentDetailComponent implements OnInit {

  @Input() studentEntity!: SelfReportedInEdit;
  @Input() currentClassListNames!: string[];
  @Input() originalNamesChecker!: OriginalClassListChecker;

  @Output() shouldBeDeleted = new EventEmitter<boolean>();
  @Output() classListChanged = new EventEmitter<void>();

  sleeping: boolean = false;

  editMode: boolean = false;

  formControl!: FormControl;

  constructor() { }

  ngOnInit(): void {
    const validator = new ForbiddenNameValidator()
    this.formControl = new FormControl("", [
      Validators.required,
      validator.func.bind(this)
    ]);
  }

  toggleEdit() {
    if (this.editMode) {
      this.save()
    } else {
      this.edit()
    }
  }

  sleep() {
    this.sleeping = true;
    timer(200).subscribe(val => { this.sleeping = false; })
  }

  save() {
    if (this.editMode && !this.sleeping) {
      if (this.formControl.valid) {
        const toChange: string = this.formControl.value;
        if (this.originalNamesChecker.checkNameToAdd(toChange)) {
          this.studentEntity.name = toChange;
          this.classListChanged.emit();
        }
      }
      this.editMode = false;
      this.sleep();
    }
  }

  edit() {
    if (!this.editMode && !this.sleeping) {
      this.formControl.setValue(this.studentEntity.name)
      this.editMode = true;
    }
  }

  delete() {
    const shouldBeDestructed: boolean = this.studentEntity.delete();
    if (shouldBeDestructed) {
      this.shouldBeDeleted.emit(true);
    }
    this.classListChanged.emit();
  }

  recover() {
    this.studentEntity.recover();
    this.classListChanged.emit();
  }

}
