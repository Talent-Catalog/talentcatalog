import {CandidateAssistanceType, OfferToAssist} from './offer-to-assist';
import {User} from "./user";
import {Partner} from "./partner";

describe('OfferToAssist', () => {
  it('should create a valid OfferToAssist object', () => {
    const createdBy: User = { id: 1, name: 'Creator' } as User;
    const updatedBy: User = { id: 2, name: 'Updater' } as User;
    const partner: Partner = { id: 1, name: 'Partner' } as Partner;
    // Mock data
    const ota: OfferToAssist = {
      id: 1,
      createdDate: new Date("2024-05-01"),
      additionalNotes: 'notes',
      partner: partner,
      publicId: '1234abcd',
      reason: CandidateAssistanceType.JOB_OPPORTUNITY
    };

    // Assertions
    expect(ota.id).toEqual(1);
    expect(ota.createdDate).toBeTruthy();
    expect(ota.additionalNotes).toEqual('notes');
    expect(ota.partner).toEqual(partner);
    expect(ota.publicId).toEqual('1234abcd');
    expect(ota.reason).toEqual('Job Opportunity');
  });
});
