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
import org.tbbtalent.server.model.SavedList;
import org.tbbtalent.server.request.list.SearchSavedListRequest;
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

    /**
     * Returns all saved lists matching the request. 
     * On this API any paging or sorting info in the request is ignored.
     * The sort is hard coded to ascending by name.
     * @param request Defines which lists should be returned. Any paging or
     *                sorting fields in the request are ignored.
     * @return All matching SavedLists
     */
    @GetMapping()
    public List<Map<String, Object>> listSavedLists(
            @Valid @RequestBody SearchSavedListRequest request) {
        List<SavedList> savedLists = savedListService.listSavedLists(request);
        return savedListDto().buildList(savedLists);
    }

    /**
     * Returns the requested page of saved lists matching the request. 
     * On this API any sorting info in the request is ignored.
     * The sort is hard coded to ascending by name.
     * @param request Defines which lists should be returned. Any sorting fields 
     *                in the request are ignored.
     * @return Requested page of matching SavedLists
     */
    @PostMapping("search")
    public Map<String, Object> search(
            @Valid @RequestBody SearchSavedListRequest request) {
        Page<SavedList> savedLists = savedListService.searchSavedLists(request);
        return savedListDto().buildPage(savedLists);
    }

    @PostMapping
    public Map<String, Object> create(@Valid @RequestBody UpdateSavedListRequest request) throws EntityExistsException {
        SavedList savedList = savedListService.createSavedList(request);
        return savedListDto().build(savedList);
    }

    @DeleteMapping("{id}")
    public boolean delete(@PathVariable("id") long id) throws EntityReferencedException {
        return savedListService.deleteSavedList(id);
    }

    @PutMapping("{id}")
    public Map<String, Object> replace(@PathVariable("id") long id,
                                          @Valid @RequestBody UpdateSavedListRequest request) throws EntityExistsException {
        SavedList savedList = savedListService.replaceSavedList(id, request); 
        return savedListDto().build(savedList);
    }

    @PutMapping("{id}/merge")
    public Map<String, Object> merge(@PathVariable("id") long id,
                                          @Valid @RequestBody UpdateSavedListRequest request) throws EntityExistsException {
        SavedList savedList = savedListService.mergeSavedList(id, request); 
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
