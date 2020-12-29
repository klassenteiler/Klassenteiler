import { Component, OnInit } from '@angular/core';
import { SchoolClassService } from '../_services/school-class.service';
import { ClassTeacher, SchoolClass } from '../_tools/enc-tools.service';
import { Location} from '@angular/common';
import { AppConfigService } from '../app-config.service';

import * as jspdf from 'jspdf';  
import html2canvas from 'html2canvas'; 
import { PdfTools } from '../_tools/pdf-tools';

@Component({
  selector: 'app-create',
  templateUrl: './create.component.html',
  styleUrls: ['./create.component.css']
})
export class CreateComponent implements OnInit {

  formIsOpen:boolean =true;
  creatingClass: boolean = false;
  buttonText: string = "Klasse erstellen"

  password: string| undefined;
  generatedSchoolClass: SchoolClass | undefined;
  teacherViewLinkStr: string | undefined;

  constructor(
    private config: AppConfigService,
    private schoolClassService: SchoolClassService, 
    private location: Location) { }

  ngOnInit(): void {
  }

  deactivateForm(){
    this.formIsOpen = false;
  }

  createClass(schoolName: string, className: string){
    this.creatingClass = true;
    this.buttonText = "Bitte warten"
    this.deactivateForm();

    this.schoolClassService.makeSchoolClass(schoolName, className).subscribe(
      ([ password, schoolCls, teacher] : [string, SchoolClass,  ClassTeacher]) => {
        this.creatingClass = false;
        this.buttonText = "Erfolg!"
        this.password = password
        this.generatedSchoolClass = schoolCls
        // this.teacherViewLinkStr = this.getTeacherViewLink()
        this.location.go(this.getTeacherViewLink(false));
      }
     );
    }

    getTeacherViewLink( full: boolean){
      if( this.generatedSchoolClass === undefined){
        throw new Error("SchoolClass has not been set yet, cant make teacher view link")
      }
      if (full){
        return `${this.config.frontendUrl}/teacher/${this.generatedSchoolClass.url}`
      }
      else{
        return `teacher/${this.generatedSchoolClass.url}`
      }
    }

    savePDFsummary(){
      if((this.generatedSchoolClass === undefined) || (this.password === undefined)){throw new Error("Cant make PDF cause class is not yet generated")}
        PdfTools.teacherSummaryPDF(this.generatedSchoolClass.name(), this.password, this.getTeacherViewLink(true));
    }

}
