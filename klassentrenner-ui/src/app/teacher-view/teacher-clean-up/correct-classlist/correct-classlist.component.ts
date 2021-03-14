import { ChangeDetectorRef, Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { AbstractControl, FormControl, Validators } from '@angular/forms';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { timer } from 'rxjs';
import { TeacherService } from 'src/app/_services/teacher.service';
import { ForbiddenNameValidator } from 'src/app/_shared/forbidden-name.directive';
import { EncTools } from 'src/app/_tools/enc-tools.service';
import {  SelfReportedInEdit } from '../teacher-clean-up.models';
import { OriginalClassListChecker } from './original-classList-checker';

@Component({
  selector: 'app-correct-classlist',
  templateUrl: './correct-classlist.component.html',
  styleUrls: ['./correct-classlist.component.css']
})
export class CorrectClasslistComponent implements OnInit {

  @Input() classList!: Array<SelfReportedInEdit>;
  @Input() currentClassListNames!: string[];
  @Input() originalClassListNames!: string[];
  
  @Output() classListChanged = new EventEmitter<void>()
  @Output() sortLastNamesEvent = new EventEmitter<void>()

  originalNamesChecker!: OriginalClassListChecker;

  

  newStudentControl!: FormControl;
  // newStudentControl = new FormControl("", [Validators.required, forbiddenNameValidator(["a", "b"])]);
  constructor(
    private modalService: NgbModal,
    // private cdr: ChangeDetectorRef
    ) { 
  }

  ngOnInit(): void {
    const c: OriginalClassListChecker = new OriginalClassListChecker(this.modalService, this.originalClassListNames)
    this.originalNamesChecker = c
    
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

  onAddEnter(){
    // this.add();
    // https://github.com/angular/angular/issues/22426
    // https://stackoverflow.com/questions/39787038/how-to-manage-angular2-expression-has-changed-after-it-was-checked-exception-w
    // https://stackoverflow.com/questions/56891143/error-expressionchangedafterithasbeencheckederror-previous-value-ng-untouche
    console.log('on Enter currently does not work, due to angular change detection issues')
  }

  add(){
    console.log(this.currentClassListNames)
    const rraw_name = this.newStudentControl.value
    const name_to_add = EncTools.cleanName(rraw_name);

    if(this.newStudentControl.valid && name_to_add !== ""){
      if(this.originalNamesChecker.checkNameToAdd(name_to_add)){
        const newStudent: SelfReportedInEdit = SelfReportedInEdit.makeTeacherAdded(name_to_add)

        this.classList.push(newStudent);
        this.triggerClassListChanged();
      }
      // timer(10).subscribe(_s => {this.newStudentControl.setValue("")})
      this.newStudentControl.setValue("")
    }
  }

  triggerClassListChanged(){
    this.classListChanged.emit()
  }

  sortLastNames(){
    this.sortLastNamesEvent.emit()
  }
}
 