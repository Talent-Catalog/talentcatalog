/*
 * Copyright (c) 2025 Talent Catalog.
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

package org.tctalent.server.model.db.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ValueMapping;
import org.tctalent.server.model.db.EducationType;
import org.tctalent.server.model.db.Exam;
import org.tctalent.server.model.db.Gender;
import org.tctalent.server.model.db.HasPassport;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.model.db.YesNo;
import org.tctalent.server.model.db.YesNoUnsure;

/**
 * Map public and internal versions of enums
 *
 * @author John Cameron
 */
@Mapper
public interface EnumsMapper {

    @ValueMapping(target="Associate", source = "ASSOCIATE")
    @ValueMapping(target="Vocational", source = "VOCATIONAL")
    @ValueMapping(target="Bachelor", source = "BACHELOR")
    @ValueMapping(target="Masters", source = "MASTERS")
    @ValueMapping(target="Doctoral", source = "DOCTORAL")
    EducationType publicToInternalEducationType(org.tctalent.anonymization.model.EducationType publicEnum);

    @ValueMapping(target="NoResponse", source = "NO_RESPONSE")
    @ValueMapping(target="OET", source = "OET")
    @ValueMapping(target="OETRead", source = "OET_READ")
    @ValueMapping(target="OETList", source = "OET_LIST")
    @ValueMapping(target="OETLang", source = "OET_LANG")
    @ValueMapping(target="IELTSGen", source = "IELTS_GEN")
    @ValueMapping(target="IELTSAca", source = "IELTS_ACA")
    @ValueMapping(target="TOEFL", source = "TOEFL")
    @ValueMapping(target="Other", source = "OTHER")
    @ValueMapping(target="DETOfficial", source = "DET_OFFICIAL")
    Exam publicToInternalExam(org.tctalent.anonymization.model.Exam publicEnum);

    @ValueMapping(target="male", source = "MALE")
    @ValueMapping(target="female", source = "FEMALE")
    @ValueMapping(target="other", source = "OTHER")
    Gender publicToInternalGender(org.tctalent.anonymization.model.Gender publicGender);

    @ValueMapping(target="NoResponse", source = "NO_RESPONSE")
    @ValueMapping(target="ValidPassport", source = "VALID_PASSPORT")
    @ValueMapping(target="InvalidPassport", source = "INVALID_PASSPORT")
    @ValueMapping(target="NoPassport", source = "NO_PASSPORT")
    HasPassport publicToInternalHasPassport(org.tctalent.anonymization.model.HasPassport publicEnum);

    @ValueMapping(target="active", source = "ACTIVE")
    @ValueMapping(target="inactive", source = "INACTIVE")
    @ValueMapping(target="deleted", source = "DELETED")
    Status publicToInternalStatus(org.tctalent.anonymization.model.Status publicStatus);

    @ValueMapping(target="NoResponse", source = "NO_RESPONSE")
    @ValueMapping(target="Yes", source = "YES")
    @ValueMapping(target="No", source = "NO")
    YesNo publicToInternalYesNo(org.tctalent.anonymization.model.YesNo publicEnum);

    @ValueMapping(target="NoResponse", source = "NO_RESPONSE")
    @ValueMapping(target="Yes", source = "YES")
    @ValueMapping(target="No", source = "NO")
    @ValueMapping(target="Unsure", source = "UNSURE")
    YesNoUnsure publicToInternalYesNoUnsure(org.tctalent.anonymization.model.YesNoUnsure publicEnum);
}
