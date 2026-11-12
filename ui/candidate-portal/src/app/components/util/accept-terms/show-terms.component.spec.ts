import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ShowTermsComponent } from './show-terms.component';

describe('ShowTermsComponent', () => {
    let component: ShowTermsComponent;
    let fixture: ComponentFixture<ShowTermsComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [ShowTermsComponent]
        }).compileComponents();

        fixture = TestBed.createComponent(ShowTermsComponent);
        component = fixture.componentInstance;
    });

    it('should emit termsRead when content does not need scrolling', () => {
        component.requestTermsRead = true;
        const emitSpy = vi.spyOn(component.termsRead, 'emit').mockReturnValue(undefined);
        component.termsBox = {
            nativeElement: {
                scrollHeight: 200,
                clientHeight: 400
            } as HTMLElement
        } as any;

        component.ngAfterViewInit();

        expect(component.scrolledToBottom).toBe(true);
        expect(emitSpy).toHaveBeenCalledWith(true);
    });

    it('should not emit termsRead on init when scrolling is required', () => {
        component.requestTermsRead = true;
        const emitSpy = vi.spyOn(component.termsRead, 'emit').mockReturnValue(undefined);
        component.termsBox = {
            nativeElement: {
                scrollHeight: 800,
                clientHeight: 400
            } as HTMLElement
        } as any;

        component.ngAfterViewInit();

        expect(component.scrolledToBottom).toBe(false);
        expect(emitSpy).not.toHaveBeenCalled();
    });

    it('should emit termsRead after scrolling to bottom', () => {
        const emitSpy = vi.spyOn(component.termsRead, 'emit').mockReturnValue(undefined);
        const element = {
            scrollTop: 400,
            clientHeight: 400,
            scrollHeight: 800
        } as HTMLElement;

        component.onScroll(element);

        expect(component.scrolledToBottom).toBe(true);
        expect(emitSpy).toHaveBeenCalledWith(true);
    });
});
