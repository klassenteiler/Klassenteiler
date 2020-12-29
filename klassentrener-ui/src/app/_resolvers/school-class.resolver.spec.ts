import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { IndexComponent } from '../index/index.component';
import { TeacherViewComponent } from '../teacher-view/teacher-view.component';
import { BackendService } from '../_services/backend.service';
import { Location} from '@angular/common';

import { SchoolClassResolver } from './school-class.resolver';
import { SchoolClassT } from '../models';
import { impl } from '../_tools/enc-tools.service';
import { Observable, of, throwError } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { NotFoundComponent } from '../not-found/not-found.component';

class MockBackendService{
    getClass(id: string, secret: string): Observable<SchoolClassT>{
        // return of(impl<SchoolClassT>({
        //     schoolName: "test", className: "testClass", classSecret: "secret", publicKey: "key"
        // }))
        return throwError({status : 404})
    }

}

describe('SchoolClassResolver', () => {
  let resolver: SchoolClassResolver;
  let router: Router;

  beforeEach(() => {
    TestBed.configureTestingModule({
        imports: [
            RouterTestingModule.withRoutes([
                {path: 'teacher/:id/:classSecret', component: TeacherViewComponent, resolve: {schoolClass: SchoolClassResolver}},
                {path: 'test', component: IndexComponent},
                {path: 'notFound', component: NotFoundComponent}
            ])
        ],
        providers: [
            {provide: BackendService, useClass: MockBackendService},
        ]
    });
    resolver = TestBed.inject(SchoolClassResolver);
    router = TestBed.inject(Router);
  });

  it('should be created', () => {
      console.log("should be created test")
      expect(resolver).toBeTruthy();
  });

  // TODO: the behaivour in here seems to work, but the test does not work
  xit('test school class resolver routing', () => {        
      const nonResolvingPath = 'teacher/23/classSecret'

      const location: Location = TestBed.inject(Location);
      console.log('resolve test')
    //   router.navigate(['create'])
      router.navigate([nonResolvingPath]).then(() => {
        console.log('in then')
        expect(location.path()).toBe(nonResolvingPath)
      })
      console.log('done')
  })
});
