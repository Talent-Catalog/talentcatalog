package org.tctalent.server.model.db.mapper;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import org.tctalent.server.model.db.TermsInfo;
import org.tctalent.server.model.db.TermsInfoDto;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-05-27T21:31:33+0100",
    comments = "version: 1.6.3, compiler: javac, environment: Java 17.0.1 (Oracle Corporation)"
)
@Component
public class TermsInfoMapperImpl implements TermsInfoMapper {

    @Override
    public TermsInfoDto toDto(TermsInfo termsInfo) {
        if ( termsInfo == null ) {
            return null;
        }

        TermsInfoDto termsInfoDto = new TermsInfoDto();

        if ( termsInfo.getId() != null ) {
            termsInfoDto.setId( termsInfo.getId() );
        }
        termsInfoDto.setContent( termsInfo.getContent() );
        termsInfoDto.setCreatedDate( termsInfo.getCreatedDate() );
        termsInfoDto.setType( termsInfo.getType() );
        termsInfoDto.setVersion( termsInfo.getVersion() );

        return termsInfoDto;
    }
}
