import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ModeratorIncidentsComponent } from './moderator-incidents.component';

describe('ModeratorIncidentsComponent', () => {
  let component: ModeratorIncidentsComponent;
  let fixture: ComponentFixture<ModeratorIncidentsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ModeratorIncidentsComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ModeratorIncidentsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
