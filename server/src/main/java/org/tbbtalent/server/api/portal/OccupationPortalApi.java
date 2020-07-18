package org.tbbtalent.server.api.portal;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbbtalent.server.model.db.Occupation;
import org.tbbtalent.server.service.OccupationService;
import org.tbbtalent.server.util.dto.DtoBuilder;

@RestController()
@RequestMapping("/api/portal/occupation")
public class OccupationPortalApi {

    private final OccupationService occupationService;

    @Autowired
    public OccupationPortalApi(OccupationService occupationService) {
        this.occupationService = occupationService;
    }

    @GetMapping()
    public List<Map<String, Object>> listAllIndustries() {
        List<Occupation> industries = occupationService.listOccupations();
        return occupationDto().buildList(industries);
    }

    private DtoBuilder occupationDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                ;
    }

}
