// Copyright 2008 Orc Software AB. All rights reserved.
// Reproduction in whole or in part in any form or medium without express
// written permission of Orc Software AB is strictly prohibited.

package org.tctalent.server.service.db;

import org.springframework.lang.NonNull;
import org.tctalent.server.model.db.TermsInfo;
import org.tctalent.server.model.db.TermsType;

/**
 * TODO JC Doc
 *
 * @author John Cameron
 */
public interface TermsInfoService {

    @NonNull
    TermsInfo getCurrentByType(TermsType termsType);
}
