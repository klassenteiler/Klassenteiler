import { Component, Input, OnInit } from '@angular/core';
import { Observable } from 'rxjs';
import { mergeMap , map} from 'rxjs/operators';
import { AppConfigService } from 'src/app/app-config.service';
import { DemoService } from 'src/app/_services/demo.service';
import { TeacherService } from 'src/app/_services/teacher.service';
import { ClassTeacher, SchoolClass } from 'src/app/_tools/enc-tools.service';

@Component({
  selector: 'app-survey-open',
  templateUrl: './survey-open.component.html',
  styleUrls: ['./survey-open.component.css']
})
export class SurveyOpenComponent implements OnInit {
  @Input() schoolClass!: SchoolClass;
  @Input() classTeacher!: ClassTeacher;

  studentUrl!: string;
  nSignups:number=0;

  submittingDemo = false;

  
  constructor(private configService: AppConfigService, private teacherService: TeacherService, private demoService: DemoService) { 
  }

  get studentURLhttp():string {
    return `http://${this.studentUrl}`
  }


  ngOnInit(): void {
    this.studentUrl = `${this.configService.frontendUrl}/${this.schoolClass.studentURL}`

    this.teacherService.nSignups(this.schoolClass, this.classTeacher).subscribe(n => {
      console.log(`nSignups = ${n}`)
      this.nSignups = n})

    console.log(this.showDemoDataButton())
  }

  showDemoDataButton():boolean {
    return this.demoService.demoActive() && (this.nSignups == 0) && this.demoService.schoolEligable(this.schoolClass)
  }

  submitSampleData(){
    if(this.showDemoDataButton()){
      this.submittingDemo = true;
      this.demoService.submitSampleData(this.schoolClass).subscribe(
        msg => {
          console.log("Generating the sample data was successfull")
          this.submittingDemo = false
          window.location.reload()
        },
        error => {
          console.log(error)
          alert("Irgendetwas ist schief gegangen. Der Error wurde in die Konsole geloggt. Vieleicht hilft es die Seite neu zu laden")
        }
        )
    }
  }

  test(){
    console.log(this.nSignups == 0)
    console.log(this.showDemoDataButton())
  }

  closeSurvey(): void {
    const closeObs = this.teacherService.closeSurvey(this.schoolClass, this.classTeacher)
    
    let finalObs: Observable<string>;

    if(this.configService.skipMerging){
      finalObs = closeObs.pipe(mergeMap((msg:string) => {
        console.log(msg)
        return this.teacherService.startCalculatingWithMerge(this.schoolClass, this.classTeacher, {});
      }))
    }
    else{
      finalObs = closeObs
    }
    
    finalObs.subscribe(msg => {
      console.log(msg);
      window.location.reload();
    })

  }

}
