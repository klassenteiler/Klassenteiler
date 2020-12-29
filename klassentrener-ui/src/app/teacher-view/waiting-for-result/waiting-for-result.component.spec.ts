import { ComponentFixture, TestBed } from '@angular/core/testing';

import { WaitingForResultComponent } from './waiting-for-result.component';

describe('WaitingForResultComponent', () => {
  let component: WaitingForResultComponent;
  let fixture: ComponentFixture<WaitingForResultComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ WaitingForResultComponent ]
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
