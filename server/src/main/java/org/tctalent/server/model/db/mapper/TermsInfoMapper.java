// Copyright 2008 Orc Software AB. All rights reserved.
// Reproduction in whole or in part in any form or medium without express
// written permission of Orc Software AB is strictly prohibited.

package org.tctalent.server.model.db.mapper;

import org.mapstruct.Mapper;
import org.tctalent.server.model.db.TermsInfo;
import org.tctalent.server.model.db.TermsInfoDto;

/**
 * TODO JC Doc
 *
 * @author John Cameron
 */
@Mapper
public interface TermsInfoMapper {
  TermsInfoDto toDto(TermsInfo termsInfo);
}
