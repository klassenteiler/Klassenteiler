import { Component, Input, OnInit } from '@angular/core';
import { TeacherService } from 'src/app/_services/teacher.service';
import { ClassTeacher, SchoolClass,  ClearLocalStudent} from 'src/app/_tools/enc-tools.service';



@Component({
  selector: 'app-results',
  templateUrl: './results.component.html',
  styleUrls: ['./results.component.css']
})
export class ResultsComponent implements OnInit {
  @Input() schoolClass!: SchoolClass;
  @Input() classTeacher!: ClassTeacher;

  resultClassList: Array<ClearLocalStudent>|undefined;
  groupA: Array<ClearLocalStudent>| undefined;
  groupB: Array<ClearLocalStudent>| undefined;

  constructor(private teacherService: TeacherService) { }

  ngOnInit(): void {
    this.teacherService.getResults(this.schoolClass, this.classTeacher).subscribe(
      students => {this.setResults(students)},
      (err => {console.log("something went wrong"); console.log(err)})
    )
  }

  click(){
    console.log("click")
    console.log(this.groupA)
  }

  setResults(students: Array<ClearLocalStudent>){
        // console.log(students);
        this.resultClassList = students;

        this.groupA = ClearLocalStudent.filterAndSort(students, 1);
        this.groupB = ClearLocalStudent.filterAndSort(students, 2);

        console.log('in set results')
        console.log(this.groupA)
        console.log(this.groupB)
        this.click();
  }


}
