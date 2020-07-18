package org.tbbtalent.server.api.portal;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbbtalent.server.model.db.Industry;
import org.tbbtalent.server.service.db.IndustryService;
import org.tbbtalent.server.util.dto.DtoBuilder;

@RestController()
@RequestMapping("/api/portal/industry")
public class IndustryPortalApi {

    private final IndustryService industryService;

    @Autowired
    public IndustryPortalApi(IndustryService industryService) {
        this.industryService = industryService;
    }

    @GetMapping()
    public List<Map<String, Object>> listAllIndustries() {
        List<Industry> industries = industryService.listIndustries();
        return industryDto().buildList(industries);
    }

    private DtoBuilder industryDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                ;
    }

}
