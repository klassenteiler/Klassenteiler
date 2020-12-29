import { HttpClient } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';

import { BackendService } from './backend.service';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

describe('BackendServiceService', () => {
  let service: BackendService;
  let httpClient: HttpClient;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
      HttpClientTestingModule
      ]
    });
    httpClient = TestBed.inject(HttpClient);
    httpTestingController = TestBed.inject(HttpTestingController);
    service = TestBed.inject(BackendService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
