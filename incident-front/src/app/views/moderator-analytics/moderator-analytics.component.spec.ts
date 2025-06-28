import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ModeratorAnalyticsComponent } from './moderator-analytics.component';

describe('ModeratorAnalyticsComponent', () => {
  let component: ModeratorAnalyticsComponent;
  let fixture: ComponentFixture<ModeratorAnalyticsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ModeratorAnalyticsComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ModeratorAnalyticsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
