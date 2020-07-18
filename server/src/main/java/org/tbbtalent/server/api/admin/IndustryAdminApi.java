package org.tbbtalent.server.api.admin;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbbtalent.server.exception.EntityExistsException;
import org.tbbtalent.server.exception.EntityReferencedException;
import org.tbbtalent.server.model.db.Industry;
import org.tbbtalent.server.request.industry.CreateIndustryRequest;
import org.tbbtalent.server.request.industry.SearchIndustryRequest;
import org.tbbtalent.server.request.industry.UpdateIndustryRequest;
import org.tbbtalent.server.service.IndustryService;
import org.tbbtalent.server.util.dto.DtoBuilder;

@RestController()
@RequestMapping("/api/admin/industry")
public class IndustryAdminApi {

    private final IndustryService industryService;

    @Autowired
    public IndustryAdminApi(IndustryService industryService) {
        this.industryService = industryService;
    }

    @GetMapping()
    public List<Map<String, Object>> listAllIndustries() {
        List<Industry> industries = industryService.listIndustries();
        return industryDto().buildList(industries);
    }

    @PostMapping("search")
    public Map<String, Object> search(@RequestBody SearchIndustryRequest request) {
        Page<Industry> industries = this.industryService.searchIndustries(request);
        return industryDto().buildPage(industries);
    }

    @GetMapping("{id}")
    public Map<String, Object> get(@PathVariable("id") long id) {
        Industry industry = this.industryService.getIndustry(id);
        return industryDto().build(industry);
    }

    @PostMapping
    public Map<String, Object> create(@Valid @RequestBody CreateIndustryRequest request) throws EntityExistsException {
        Industry industry = this.industryService.createIndustry(request);
        return industryDto().build(industry);
    }

    @PutMapping("{id}")
    public Map<String, Object> update(@PathVariable("id") long id,
                                      @Valid @RequestBody UpdateIndustryRequest request) throws EntityExistsException  {

        Industry industry = this.industryService.updateIndustry(id, request);
        return industryDto().build(industry);
    }

    @DeleteMapping("{id}")
    public boolean delete(@PathVariable("id") long id) throws EntityReferencedException {
        return this.industryService.deleteIndustry(id);
    }


    private DtoBuilder industryDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                .add("status")
                ;
    }

}
