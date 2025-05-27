// Copyright 2008 Orc Software AB. All rights reserved.
// Reproduction in whole or in part in any form or medium without express
// written permission of Orc Software AB is strictly prohibited.

package org.tctalent.server.repository.db;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.tctalent.server.model.db.TermsInfo;
import org.tctalent.server.model.db.TermsType;

/**
 * TODO JC Doc
 *
 * @author John Cameron
 */
public interface TermsInfoRepository extends JpaRepository<TermsInfo, Long> {
    Optional<TermsInfo> findFirstByTypeOrderByCreatedDateDesc(TermsType type);
}
