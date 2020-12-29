import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SchoolClassService } from '../_services/school-class.service';

import { CreateComponent } from './create.component';
import { Location} from '@angular/common';
import { AppConfigService, MockAppConfigService } from '../app-config.service';


class MockSchoolClassService{

}


describe('CreateComponent', () => {
  let component: CreateComponent;
  let fixture: ComponentFixture<CreateComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      providers: [
        {provide: SchoolClassService, useClass: MockSchoolClassService},
        {provide: AppConfigService, useClass: MockAppConfigService},
        Location
      ],
      declarations: [ CreateComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CreateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
