import {ComponentFixture, TestBed, waitForAsync} from "@angular/core/testing";
import {ViewJobPreparationItemsComponent} from "./view-job-preparation-items.component";
import {MockJobPrepItem} from "../../../../../MockData/MockJobPrepItem";



describe('ViewJobPreparationItemsComponent', () => {
  let component: ViewJobPreparationItemsComponent;
  let fixture: ComponentFixture<ViewJobPreparationItemsComponent>;
  let compiled: HTMLElement;
  let mockItem: MockJobPrepItem;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ViewJobPreparationItemsComponent]
    })
    .compileComponents().then(() => {
      fixture = TestBed.createComponent(ViewJobPreparationItemsComponent);
      component = fixture.componentInstance;
      compiled = fixture.nativeElement;
      fixture.detectChanges();
      mockItem= new MockJobPrepItem("Mock Item Description", "Mock Tab", true);
    });
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should emit itemSelected event when an item is clicked', () => {
    component.jobPrepItems = [mockItem];
    spyOn(component.itemSelected, 'emit');

    fixture.detectChanges();
    compiled.querySelector('li').click();

    expect(component.itemSelected.emit).toHaveBeenCalledWith(mockItem);
  });

  it('should apply "selected" class to the clicked item', () => {
    component.jobPrepItems = [mockItem];
    spyOn(component.itemSelected, 'emit');

    fixture.detectChanges();
    compiled.querySelector('li').click();

    fixture.detectChanges();

    const clickedItem = compiled.querySelector('li.selected');
    expect(clickedItem.textContent).toContain(mockItem.description);
  });

  it('should not apply "selected" class to other items', () => {
    const mockItem2 = new MockJobPrepItem("Mock Item Description 2", "Mock Tab 2", false);
    component.jobPrepItems = [mockItem, mockItem2];

    fixture.detectChanges();

    const firstItem = compiled.querySelectorAll('li')[0];
    const secondItem = compiled.querySelectorAll('li')[1];

    firstItem.click();
    fixture.detectChanges();

    expect(firstItem.classList).toContain('selected');
    expect(secondItem.classList).not.toContain('selected');

    secondItem.click();
    fixture.detectChanges();

    expect(firstItem.classList).not.toContain('selected');
    expect(secondItem.classList).toContain('selected');
  });
});
