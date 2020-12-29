import { Component, Input, OnInit } from '@angular/core';
import { ClassTeacher, SchoolClass } from 'src/app/_tools/enc-tools.service';

@Component({
  selector: 'app-waiting-for-result',
  templateUrl: './waiting-for-result.component.html',
  styleUrls: ['./waiting-for-result.component.css']
})
export class WaitingForResultComponent implements OnInit {
  @Input() schoolClass!: SchoolClass;
  @Input() classTeacher!: ClassTeacher;

  constructor() { }

  ngOnInit(): void {
  }

}
