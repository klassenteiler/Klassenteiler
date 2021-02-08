
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { NgbActiveModal, NgbModal } from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'ngbd-modal-content',
  template: `
    <div class="modal-header">
      <h4 class="modal-title">Fehler!</h4>
      <button type="button" class="close" aria-label="Close" (click)="activeModal.dismiss('Cross click')">
        <span aria-hidden="true">&times;</span>
      </button>
    </div>
    <div class="modal-body">
      <p> Der Name {{name}} hat schon mal existiert. Wurde aber mittlerweile geändert. Bitte stellen Sie den ursprünglichen Namen mit dem grünen Knopf wieder her, anstatt den Namen neu hinzuzufügen. 
    </div>
    <div class="modal-footer">
      <button type="button" class="btn btn-outline-dark" (click)="activeModal.close('Close click')">Close</button>
    </div>
  `
})
export class NgbdModalContent {
  @Input() name:string = '';
  constructor(public activeModal: NgbActiveModal) {}
}

export class OriginalClassListChecker{
  highlightUndo = new EventEmitter<string>()

  constructor(private modalService: NgbModal, private originalClassListNames: string[]) {}

  checkNameToAdd(namee: string): boolean{
    if(this.originalClassListNames.indexOf(namee) !== -1){
      this.open(namee).result.then( 
        _s =>this.highlightUndo.emit(namee),
        _s =>this.highlightUndo.emit(namee),
      )
      return false
    }
    else{
      return true
    }
  }

  open(namee: string) {
    const modalRef = this.modalService.open(NgbdModalContent);
    modalRef.componentInstance.name = namee;
    return modalRef
  }
}