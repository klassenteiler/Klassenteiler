import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CorrectClasslistComponent } from './correct-classlist.component';

describe('CorrectClasslistComponent', () => {
  let component: CorrectClasslistComponent;
  let fixture: ComponentFixture<CorrectClasslistComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CorrectClasslistComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CorrectClasslistComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
