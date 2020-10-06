package org.tbbtalent.server.model.db;

import org.tbbtalent.server.util.EnumHelper;

import javax.persistence.AttributeConverter;
import java.util.List;

public class IntRecruitReasonConverter
        implements AttributeConverter<List<IntRecruitReason>, String> {
    @Override
    public String convertToDatabaseColumn(List<IntRecruitReason> intRecruitReasons) {
        return EnumHelper.toString(intRecruitReasons);
    }

    @Override
    public List<IntRecruitReason> convertToEntityAttribute(String intRecruitReasonString) {
        return EnumHelper.fromString(IntRecruitReason.class, intRecruitReasonString);
    }
}
