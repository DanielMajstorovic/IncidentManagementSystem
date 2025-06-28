import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AlertParamsComponent } from './alert-params.component';

describe('AlertParamsComponent', () => {
  let component: AlertParamsComponent;
  let fixture: ComponentFixture<AlertParamsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AlertParamsComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(AlertParamsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
