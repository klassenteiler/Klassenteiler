import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MergeService } from '../merge.service';

import { SummaryComponent } from './summary.component';

class MockMergeService{}

describe('SummaryComponent', () => {
  let component: SummaryComponent;
  let fixture: ComponentFixture<SummaryComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SummaryComponent ],
      providers: [
        {provide: MergeService, useClass: MockMergeService}
      ],
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SummaryComponent);
    component = fixture.componentInstance;
    component.classList = [] // TODO do a proper test with a class list here
    component.friendReportedList = [] 
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
