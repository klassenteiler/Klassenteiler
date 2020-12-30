import { HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import {
  Router, Resolve,
  RouterStateSnapshot,
  ActivatedRouteSnapshot
} from '@angular/router';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { SchoolClassStatus, SchoolClassT } from '../models';
import { BackendService } from '../_services/backend.service';
import { impl, SchoolClass } from '../_tools/enc-tools.service';
import { Location} from '@angular/common';

@Injectable({
  providedIn: 'root'
})
export class SchoolClassResolver implements Resolve<SchoolClass> {

  constructor(private backendService: BackendService, private router: Router, private location: Location){}

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<SchoolClass> {
    console.log('in schoolClass resolver')
    const classId: string | null = route.paramMap.get('id')
    const classSecret: string |null = route.paramMap.get('classSecret')

    if ((classId == null) || (classSecret == null)){
      throw new Error("Class ID or classSecret missing.")
    }
    const classObservable: Observable<SchoolClassT> = this.backendService.getClass(classId, classSecret).pipe(catchError((error: HttpErrorResponse) =>{
      console.log("error in school class resolver")
      console.log(error)
      console.log(this.router.url)
      console.log(route)
      this.router.navigateByUrl('notFound', {skipLocationChange: true}).then(() => {
        this.location.go(route.url.join('/'))
      })
      // throw error;
      // return impl<SchoolClassT>({
      //   id: 2, className: "", schoolName: "", classSecret: "", publicKey: ""
      // })
      return of(null as unknown as SchoolClassT)
    }));

    const obs2: Observable<SchoolClass> = classObservable.pipe(map(cls => SchoolClass.fromTransport(cls)))
    return obs2
  }
}
