import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { SchoolClassService } from '../_services/school-class.service';
import { SchoolClass } from '../_tools/enc-tools.service';

import { TeacherViewComponent } from './teacher-view.component';

class MockSchoolClass{

}

const MockRoute = {
  snapshot: {data: { schoolClass: 
    new MockSchoolClass()
  }}
}

class MockSchoolClassService{


}


describe('TeacherViewComponent', () => {
  let component: TeacherViewComponent;
  let fixture: ComponentFixture<TeacherViewComponent>;


  beforeEach(async () => {
    await TestBed.configureTestingModule({
      providers: [
        {provide: SchoolClassService, useClass: MockSchoolClassService},
        {provide: ActivatedRoute, useValue: MockRoute},
      ],
      declarations: [ TeacherViewComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TeacherViewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
