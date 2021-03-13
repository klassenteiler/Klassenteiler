import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Observable, of } from 'rxjs';
import { SchoolClassSurveyStatus } from 'src/app/models';
import { SchoolClassService } from 'src/app/_services/school-class.service';

import { WaitingForResultComponent } from './waiting-for-result.component';

class MockSchoolClassService{
  getClassStatus(classs: any): Observable<number> {
    return of(SchoolClassSurveyStatus.calculating)
  }
}

describe('WaitingForResultComponent', () => {
  let component: WaitingForResultComponent;
  let fixture: ComponentFixture<WaitingForResultComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ WaitingForResultComponent ],
      providers: [
        {provide: SchoolClassService, useValue: new MockSchoolClassService()}
      ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(WaitingForResultComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
