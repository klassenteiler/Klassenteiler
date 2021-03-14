import { Component, ElementRef, EventEmitter, Input, OnInit, Output, ViewChild } from '@angular/core';
import { Form, FormControl, Validators } from '@angular/forms';
import { SelfReportedInEdit } from '../../teacher-clean-up.models';
import { timer } from 'rxjs';
import { ForbiddenNameValidator } from 'src/app/_shared/forbidden-name.directive';
import { OriginalClassListChecker } from '../original-classList-checker';
import { animate, AnimationEvent, state, style, transition, trigger } from '@angular/animations';
import { AppModule } from 'src/app/app.module';
import { ViewportScroller } from '@angular/common';
import { EncTools } from 'src/app/_tools/enc-tools.service';

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

  // @ViewChild("myDiv") divView: ElementRef;

  sleeping: boolean = false;
  undoFocusedFlag  = false;

  editMode: boolean = false;

  formControl!: FormControl;

  constructor(
    private viewportScroller: ViewportScroller 
    ) { }

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

  scrollToMe(){
    // const elem = document.getElementById(this.studentEntity.uniqueID)
    // elem!.scrollIntoView({ behavior: "smooth", block: "start" });
    console.log('scrolling')
    this.viewportScroller.scrollToAnchor(this.studentEntity.uniqueID);
  }

  highlightUndo() {
    console.log(`highlighting ${this.studentEntity.origName}`)
    this.scrollToMe()
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
        const rawName: string = this.formControl.value;
        const toChange: string = EncTools.cleanName(rawName);
        if (toChange === this.studentEntity.origName){
          this.studentEntity.recover()
        }
        else if (this.originalNamesChecker.checkNameToAdd(toChange)) {
          this.studentEntity.name = toChange;
        }
        this.classListChanged.emit();
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
