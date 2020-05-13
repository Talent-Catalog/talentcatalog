package org.tbbtalent.server.api.admin;

import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbbtalent.server.exception.EntityExistsException;
import org.tbbtalent.server.exception.EntityReferencedException;
import org.tbbtalent.server.model.SavedList;
import org.tbbtalent.server.request.list.UpdateSavedListRequest;
import org.tbbtalent.server.service.SavedListService;
import org.tbbtalent.server.util.dto.DtoBuilder;

@RestController()
@RequestMapping("/api/admin/saved-list")
public class SavedListAdminApi {

    private final SavedListService savedListService;

    @Autowired
    public SavedListAdminApi(SavedListService savedListService) {
        this.savedListService = savedListService;
    }

    @PostMapping
    public Map<String, Object> create(@Valid @RequestBody UpdateSavedListRequest request) throws EntityExistsException {
        SavedList savedList = this.savedListService.createSavedList(request);
        return savedListDto().build(savedList);
    }

    @DeleteMapping("{id}")
    public boolean delete(@PathVariable("id") long id) throws EntityReferencedException {
        return this.savedListService.deleteSavedList(id);
    }

    @PutMapping("{id}")
    public Map<String, Object> replace(@PathVariable("id") long id,
                                          @Valid @RequestBody UpdateSavedListRequest request) throws EntityExistsException {
        SavedList savedList = this.savedListService.replaceSavedList(id, request);  //TODO JC 
        return savedListDto().build(savedList);
    }

    @PutMapping("{id}/merge")
    public Map<String, Object> merge(@PathVariable("id") long id,
                                          @Valid @RequestBody UpdateSavedListRequest request) throws EntityExistsException {
        SavedList savedList = this.savedListService.mergeSavedList(id, request);  //TODO JC 
        return savedListDto().build(savedList);
    }

    private DtoBuilder savedListDto() {
        return new DtoBuilder()
                .add("id")
                .add("status")
                .add("name")
                .add("fixed")
                .add("createdBy", userDto())
                .add("createdDate")
                .add("updatedBy", userDto())
                .add("updatedDate")
                //TODO JC List contents - just ids?
                ;
    }

    private DtoBuilder userDto() {
        return new DtoBuilder()
                .add("id")
                .add("firstName")
                .add("lastName")
                ;
    }






}
