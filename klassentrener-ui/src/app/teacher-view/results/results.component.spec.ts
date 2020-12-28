import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { TeacherService } from 'src/app/_services/teacher.service';
import { ClearLocalStudent } from 'src/app/_tools/enc-tools.service';

import { ResultsComponent } from './results.component';

export class MochTeacherService{
  getResults(){
    const namesAndGroups: [string, number][] = [["tim ", 1], ["peter", 2], ['dieter nuhr', 2], ['max müller', 1]]

    const students: ClearLocalStudent[] = namesAndGroups.map(([nname, group]: [string, number]) => 
      {
        const s = new ClearLocalStudent(nname, undefined, true, group);
        return s
      }
    );


    return of(students)
  }
}


describe('ResultsComponent', () => {
  let component: ResultsComponent;
  let fixture: ComponentFixture<ResultsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      providers: [
        {provide: TeacherService, useClass: MochTeacherService}
      ],
      declarations: [ ResultsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ResultsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  fit('should set groupA and groupB', () => {
    expect(component.groupA).toBeTruthy()
    
  })
});
