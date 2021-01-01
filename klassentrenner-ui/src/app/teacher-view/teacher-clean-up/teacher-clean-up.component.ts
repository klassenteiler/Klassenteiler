import { Component, Input, OnInit } from '@angular/core';
import { TeacherService } from 'src/app/_services/teacher.service';
import { ClassTeacher, ClearLocalStudent, SchoolClass } from 'src/app/_tools/enc-tools.service';
import { SelfReportedStudent, Student4Edit } from './teacher-clean-up.models';

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

  classList: Array<Student4Edit>| null = null;



  constructor(private teacherService: TeacherService) { }

  ngOnInit(): void {
    this.teacherService.getSelfReported(this.schoolClass, this.classTeacher).subscribe( (students: Array<ClearLocalStudent>) => {
      const editStudents: Array<Student4Edit>  = students.map(s => {
        return new SelfReportedStudent(s.id!, s.decryptedName);
      })
      this.classList = editStudents;
    })
  }

  nextStep(){
    console.log(this.activeStep)

    if(this.activeStep < this.lastStep){
      this.activeStep += 1;
    }
  }
  prevStep(){
    if(this.activeStep > 1){
      this.activeStep -= 1;
    }
  }

}
