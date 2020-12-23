import { TestBed } from '@angular/core/testing';
import { BackendService } from './backend.service';

import { TeacherService } from './teacher.service';

class MockBackendService{

}

describe('TeacherService', () => {
  let service: TeacherService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [{provide: BackendService, useClass: MockBackendService}]
    });
    service = TestBed.inject(TeacherService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
