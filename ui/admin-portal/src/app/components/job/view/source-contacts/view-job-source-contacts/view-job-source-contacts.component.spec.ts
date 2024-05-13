import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { ViewJobSourceContactsComponent } from './view-job-source-contacts.component';
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {LocalStorageModule} from "angular-2-local-storage";
import {MockPartner} from "../../../../../MockData/MockPartner";

fdescribe('ViewJobSourceContactsComponent', () => {
  let component: ViewJobSourceContactsComponent;
  let fixture: ComponentFixture<ViewJobSourceContactsComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ViewJobSourceContactsComponent],
      imports: [HttpClientTestingModule,
        LocalStorageModule.forRoot({}),
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewJobSourceContactsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should emit sourcePartnerSelection event when a source partner is selected', () => {
    const mockPartner: MockPartner = new MockPartner();    component.selectable = true;
    component.sourcePartners = [mockPartner]; // Set up a mock source partner
    spyOn(component.sourcePartnerSelection, 'emit'); // Spy on the emit method of sourcePartnerSelection

    component.selectCurrent(mockPartner); // Call the method to select the mock partner

    expect(component.sourcePartnerSelection.emit).toHaveBeenCalledWith(mockPartner); // Check if emit was called with the mock partner
  });
});
