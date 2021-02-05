import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { Form, FormControl, Validators } from '@angular/forms';
import { SelfReportedInEdit } from '../../teacher-clean-up.models';
import { timer } from 'rxjs';
import { ForbiddenNameValidator } from 'src/app/_shared/forbidden-name.directive';
import { OriginalClassListChecker } from '../original-classList-checker';
import { animate, AnimationEvent, state, style, transition, trigger } from '@angular/animations';
import { AppModule } from 'src/app/app.module';

@Component({
  selector: 'app-student-detail',
  templateUrl: './student-detail.component.html',
  styleUrls: ['./student-detail.component.css'],
  animations: [
    trigger('undoFocusedTrigger', [
      state('focused', style({
        borderColor: 'red',
        borderWidth: 'thick'
      })),
      state('normal', style({
        borderColor: 'black',
        borderWidth: '0pt'
      })),
      transition('normal <=> focused', [animate('0.5s')])
    ])
  ]
})
export class StudentDetailComponent implements OnInit {

  @Input() studentEntity!: SelfReportedInEdit;
  @Input() currentClassListNames!: string[];
  @Input() originalNamesChecker!: OriginalClassListChecker;

  @Output() shouldBeDeleted = new EventEmitter<boolean>();
  @Output() classListChanged = new EventEmitter<void>();

  sleeping: boolean = false;
  undoFocusedFlag  = false;

  editMode: boolean = false;

  formControl!: FormControl;

  constructor() { }

  ngOnInit(): void {
    const validator = new ForbiddenNameValidator()
    this.formControl = new FormControl("", [
      Validators.required,
      validator.func.bind(this)
    ]);

    if (!this.studentEntity.teacherAdded) {
      this.originalNamesChecker.highlightUndo.subscribe((name: string) => {
        if (name == this.studentEntity.origName) {
          this.highlightUndo();
        }
      })
    }
  }

  highlightUndo() {
    console.log(`highlighting ${this.studentEntity.origName}`)
    this.undoFocusedFlag = true;
  }
  animationEnd(event: AnimationEvent) {
    if(event.toState == "focused"){
      timer(2000).subscribe(s=>{
        this.undoFocusedFlag = false;
      })
    }
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
