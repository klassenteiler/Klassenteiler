import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { forkJoin, Observable } from 'rxjs';
import { map, mergeMap } from 'rxjs/operators';
import { AppConfigService } from '../app-config.service';
import { SchoolClass } from '../_tools/enc-tools.service';
import { SchoolClassService } from './school-class.service';

@Injectable({
  providedIn: 'root'
})
export class DemoService {

  constructor(
    private schoolClassService: SchoolClassService, 
    private http: HttpClient,
    private config: AppConfigService
    ) { }

  private _getDemoData(fileName:string): Observable<any>{
    return this.http.get(`assets/${fileName}`)
  }

  getDemoData(): Observable<any>{
    if(this.config.demoData === ""){ throw new Error("no demo data file provided in config")}
    return this._getDemoData(this.config.demoData)
  }

  demoActive(): boolean{
    return this.config.demoData !== ""
  }

  schoolEligable(schoolClass: SchoolClass): boolean {
    const prefix = this.config.demoSchoolnamePrefix

    const schoolName: string = schoolClass.schoolName

    return schoolName.substring(0, prefix.length) === prefix
  }

  submitSampleData(schoolClass: SchoolClass): Observable<string>{
    if(!this.schoolEligable(schoolClass)){throw new Error("schoolClass with this name can not be filled with sample data")}

    return this.getDemoData().pipe(mergeMap(sampleData =>{

      const allSubmitCommands: Array<Observable<any>> = Object.entries(sampleData).map(([ego, friendsA]: [string, any]) => {
        const friends: string[] = friendsA as string[]

        const result: Observable<any> = this.schoolClassService.submitStudentSurvey(schoolClass, ego, friends)
        return result
      })

      const combined: Observable<string> = forkJoin(allSubmitCommands).pipe(map(stuff => {
        console.log(stuff)
        return 'ok'
      }))

      return combined
    }));
  }


}
