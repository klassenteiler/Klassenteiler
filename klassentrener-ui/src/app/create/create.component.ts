import { Component, OnInit } from '@angular/core';
import { SchoolClassService } from '../_services/school-class.service';
import { ClassTeacher, SchoolClass } from '../_tools/enc-tools.service';

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
  // teacherViewLinkStr: string | undefined;

  constructor(private schoolClassService: SchoolClassService) { }

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
      }
     );
    }

    getTeacherViewLink(){
      if( this.generatedSchoolClass === undefined){
        throw new Error("SchoolClass has not been set yet, cant make teacher view link")
      }
      return `teacher/${this.generatedSchoolClass.url}`
    }
}
