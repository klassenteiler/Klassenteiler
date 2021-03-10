import { Component, Input, OnInit } from '@angular/core';
import { NgbActiveModal, NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { throwIfEmpty } from 'rxjs/operators';
import { TeacherService } from 'src/app/_services/teacher.service';
import { ClassTeacher, ClearLocalStudent, SchoolClass } from 'src/app/_tools/enc-tools.service';
import { MergeService } from './merge.service';
import { FriendReported2Match } from './teacher-clean-up.models';
import { SelfReportedInEdit } from './teacher-clean-up.models';


@Component({
  selector: 'ngbd-modal-content',
  template: `
    <div class="modal-header">
      <h4 class="modal-title">Fehlen noch Schüler:innen?</h4>
      <button type="button" class="close" aria-label="Close" (click)="activeModal.dismiss('Cross click')">
        <span aria-hidden="true">&times;</span>
      </button>
    </div>
    <div class="modal-body">
      <p>
      Wollen Sie die Umfrage wieder öffnen? Dann können noch weitere Schüler:innen an der Umfrage über den Link teilnehmen. 
      Korrekturen von Namen die Sie hier bereits gemacht haben werden dabei gelöscht.
      </p>
    </div>
    <div class="modal-footer">
      <button type="button" class="btn btn-outline-primary" (click)="activeModal.close('Confirm')">Wieder Öffnen!</button>
      <button type="button" class="btn btn-outline-dark" (click)="activeModal.dismiss('Cancel')">Doch Nicht!</button>
    </div>
  `
})
export class ReopenConfirmationModal{
  constructor(public activeModal: NgbActiveModal) {}
}

@Component({
  selector: 'app-teacher-clean-up',
  templateUrl: './teacher-clean-up.component.html',
  styleUrls: ['./teacher-clean-up.component.css']
})
export class TeacherCleanUpComponent implements OnInit {
  @Input() schoolClass!: SchoolClass;
  @Input() classTeacher!: ClassTeacher;

  activeStep: number = 1;
  readonly lastStep: number = 3;

  classList: Array<SelfReportedInEdit> | null = null;
  friendRstudents: Array<FriendReported2Match> | null = null;

  currentClassListNames: string[] = [];
  originalClassListNames: string[] = [];

  // the current merging intermediate state is derived from a list of self reported students in the backend
  // this is summarised by the stateHash
  // it the list of self reported students in the backend changes, we can use this together with the stateHash
  // to realize that the intermediate result in this browser is bullshit
  stateHash: string = "";
  constructor(
    private mergeService: MergeService,
    private modalService: NgbModal,
    private teacherService: TeacherService
  ) { }

  ngOnInit(): void {
    //
    this.mergeService.getMergeState(this.schoolClass, this.classTeacher).subscribe(([stateHash, selfRstudents, friendRstudents]: [string, SelfReportedInEdit[], FriendReported2Match[]]) => {
      this.classList = selfRstudents;
      this.friendRstudents = friendRstudents;
      this.stateHash = stateHash;
      this.originalClassListNames = this.getOriginalClassList(selfRstudents);
      this.inplaceSortClassList();
      this.updateCurrentClasslist();
    });
  }

  getOriginalClassList(clsList: SelfReportedInEdit[]): string[] {
    return clsList.filter(s => !s.teacherAdded).map(s => s.origName)
  }

  saveClassList() {
    console.log("saving class state")
    if (this.classList === null) { throw new Error("cant save classList cause it's not defined.") }

    this.updateCurrentClasslist()
    this.mergeService.saveState2local(this.schoolClass, this.stateHash, this.classList!, null)
  }

  saveFriendReportedMatching() {
    console.log("saving friend r state")
    if (this.friendRstudents === null) { throw new Error("cant save friendR list cause its null") }
    this.mergeService.saveState2local(this.schoolClass, this.stateHash, null, this.friendRstudents!)
  }

  nextStep() {
    console.log(this.activeStep)

    if (this.activeStep < this.lastStep) {
      this.activeStep += 1;
    }
  }
  prevStep() {
    if (this.activeStep > 1) {
      this.activeStep -= 1;
    }
  }

  sortClassList(clsList: SelfReportedInEdit[]): SelfReportedInEdit[] {
    return clsList.sort((a, b) => a.lastName.localeCompare(b.lastName))
  }

  inplaceSortClassList() {
    if (this.classList === null) { throw new Error("cant sort class list because it is null") }
    this.classList = this.sortClassList(this.classList)
    this.updateCurrentClasslist()
  }

  getCurrentClasslist(): string[] {
    if (this.classList === null) { throw new Error("no classlist") }
    return this.classList.filter((s: SelfReportedInEdit) => !s.deleted).map(s => s.name)
  }

  updateCurrentClasslist() {
    this.currentClassListNames = this.getCurrentClasslist();
  }

  reopenSurvey() {
    console.log('reopen')
    this.modalService.open(ReopenConfirmationModal).result.then(
      result => {
        console.log(`ok ${result}`)
        this.teacherService.reopenSurvey(this.schoolClass, this.classTeacher).subscribe(result => {
          console.log('result')
          window.alert('Die Umfrage wurde wieder geöffnet und weitere Schüler:innen können sich nun eintragen. Sie werden nun auf die vorherige Ansicht zurück gebracht.')
          window.location.reload()
        })
      },
      dismis =>{
        console.log(`dismiss ${dismis}`)
      }
      )
  }


}
