import { Component, Input, OnInit } from '@angular/core';
import { FriendReported2Match, SelfReportedInEdit } from '../teacher-clean-up.models';

@Component({
  selector: 'app-summary',
  templateUrl: './summary.component.html',
  styleUrls: ['./summary.component.css']
})
export class SummaryComponent implements OnInit {
  @Input() classList! : SelfReportedInEdit[];
  @Input() friendReportedList!: FriendReported2Match[];

  constructor() { }

  ngOnInit(): void {
    console.log("summary")
    console.log(this.classList)
    console.log(this.friendReportedList)
  }

}
