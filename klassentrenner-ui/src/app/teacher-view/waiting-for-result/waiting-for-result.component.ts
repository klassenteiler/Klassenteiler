import { Component, Input, OnInit } from '@angular/core';
import { Observable, Subject, timer } from 'rxjs';
import { retry, switchMap, takeUntil } from 'rxjs/operators';
import { SchoolClassSurveyStatus } from 'src/app/models';
import { SchoolClassService } from 'src/app/_services/school-class.service';
import { ClassTeacher, SchoolClass } from 'src/app/_tools/enc-tools.service';

@Component({
  selector: 'app-waiting-for-result',
  templateUrl: './waiting-for-result.component.html',
  styleUrls: ['./waiting-for-result.component.css']
})
export class WaitingForResultComponent implements OnInit {
  @Input() schoolClass!: SchoolClass;
  @Input() classTeacher!: ClassTeacher;

  private stopPolling = new Subject();

  constructor(private classService: SchoolClassService) { }

  ngOnInit(): void {
    const currentStatus: Observable<number> = timer(1, 5000).pipe(
      switchMap(() => this.classService.getClassStatus(this.schoolClass)),
      retry(),
      // tap(console.log),
      takeUntil(this.stopPolling)
    )

    currentStatus.subscribe(status =>{
      console.log(`current status ${status}`)
      if(status === SchoolClassSurveyStatus.done){
        window.location.reload()
      }
    })

    // after 5 minutes stop pooling
    timer(5*60*1000).subscribe(_tmp =>{
      console.log('stopping the pooling')
      this.stopPolling.next()
    })
  }

  ngOnDestroy() {
    this.stopPolling.next();
 }

}
