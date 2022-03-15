/*
 * Copyright (c) 2022 Talent Beyond Boundaries.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tbbtalent.server.request.candidate;

import org.tbbtalent.server.model.db.UnhcrStatus;
import org.tbbtalent.server.model.db.YesNo;
import org.tbbtalent.server.model.db.YesNoUnsure;

public class UpdateCandidateRegistrationRequest {
    private String externalId;
    private String externalIdSource;
    private YesNoUnsure unhcrRegistered;
    private UnhcrStatus unhcrStatus;
    private YesNo unhcrConsent;
    private String unhcrNumber;
    private YesNoUnsure unrwaRegistered;
    private String unrwaNumber;

    public String getExternalId() {return externalId;}

    public void setExternalId(String externalId) {this.externalId = externalId;}

    public String getExternalIdSource() {return externalIdSource;}

    public void setExternalIdSource(String externalIdSource) {this.externalIdSource = externalIdSource;}

    public UnhcrStatus getUnhcrStatus() {
        return unhcrStatus;
    }

    public void setUnhcrStatus(UnhcrStatus unhcrStatus) {
        this.unhcrStatus = unhcrStatus;
    }

    public String getUnhcrNumber() {
        return unhcrNumber;
    }

    public void setUnhcrNumber(String unhcrNumber) {
        this.unhcrNumber = unhcrNumber;
    }

    public YesNoUnsure getUnhcrRegistered() {return unhcrRegistered;}

    public void setUnhcrRegistered(YesNoUnsure unhcrRegistered) {this.unhcrRegistered = unhcrRegistered;}

    public YesNo getUnhcrConsent() {return unhcrConsent;}

    public void setUnhcrConsent(YesNo unhcrConsent) {this.unhcrConsent = unhcrConsent;}

    public YesNoUnsure getUnrwaRegistered() {return unrwaRegistered;}

    public void setUnrwaRegistered(YesNoUnsure unrwaRegistered) {this.unrwaRegistered = unrwaRegistered;}

    public String getUnrwaNumber() {return unrwaNumber;}

    public void setUnrwaNumber(String unrwaNumber) {this.unrwaNumber = unrwaNumber;}
}
