import { HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import {
  Router, Resolve,
  RouterStateSnapshot,
  ActivatedRouteSnapshot
} from '@angular/router';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { SchoolClassT } from '../models';
import { BackendService } from '../_services/backend.service';
import { SchoolClass } from '../_tools/enc-tools.service';

@Injectable({
  providedIn: 'root'
})
export class SchoolClassResolver implements Resolve<SchoolClass> {

  constructor(private backendService: BackendService, private router: Router){}

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<SchoolClass> {
    const classId: string | null = route.paramMap.get('id')
    const classSecret: string |null = route.paramMap.get('classSecret')

    if ((classId == null) || (classSecret == null)){
      throw new Error("Class ID or classSecret missing.")
    }
    const classObservable: Observable<SchoolClassT> = this.backendService.getClass(classId, classSecret).pipe(catchError((error: HttpErrorResponse) =>{
      console.log(error)
      this.router.navigateByUrl('notFound', {skipLocationChange: true})
      throw error;
    }));

    const obs2: Observable<SchoolClass> = classObservable.pipe(map(cls => SchoolClass.fromTransport(cls)))
    return obs2
  }
}
