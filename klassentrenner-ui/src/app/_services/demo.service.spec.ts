import { HttpClient } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { AppConfigService, MockAppConfigService } from '../app-config.service';

import { DemoService } from './demo.service';
import { SchoolClassService } from './school-class.service';

class MockSchoolClassService{

}

class MockHttp{}

describe('DemoService', () => {
  let service: DemoService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        {provide: AppConfigService, useClass: MockAppConfigService},
        {provide: SchoolClassService, useClass: MockSchoolClassService},
        {provide: HttpClient, useClass: MockHttp}
      ]
    });
    service = TestBed.inject(DemoService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
