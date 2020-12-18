import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { SchoolClassService } from '../_services/school-class.service';
import { SchoolClass } from '../_tools/enc-tools.service';

@Component({
  selector: 'app-teacher-view',
  templateUrl: './teacher-view.component.html',
  styleUrls: ['./teacher-view.component.css']
})
export class TeacherViewComponent implements OnInit {

  schoolClass: SchoolClass | null;

  constructor(private classService: SchoolClassService, private route: ActivatedRoute) {
    console.log('pre')
    const tmpClass: SchoolClass = this.route.snapshot.data.schoolClass as SchoolClass;
    this.schoolClass = tmpClass
   }

  ngOnInit(): void {
    // const classObservable: Observable<SchoolClass> = this.classService.getClassFromRoute(this.route)
  }
}
