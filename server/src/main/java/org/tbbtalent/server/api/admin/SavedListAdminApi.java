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
import org.tbbtalent.server.exception.InvalidRequestException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.SavedList;
import org.tbbtalent.server.request.list.CreateSavedListRequest;
import org.tbbtalent.server.request.list.SearchSavedListRequest;
import org.tbbtalent.server.request.list.UpdateSavedListInfoRequest;
import org.tbbtalent.server.service.SavedListService;
import org.tbbtalent.server.util.dto.DtoBuilder;

@RestController()
@RequestMapping("/api/admin/saved-list")
public class SavedListAdminApi {

    //TODO JC This should implement the new API
    
    private final SavedListService savedListService;
    private final SavedListBuilderSelector builderSelector = new SavedListBuilderSelector();

    @Autowired
    public SavedListAdminApi(SavedListService savedListService) {
        this.savedListService = savedListService;
    }

    /**
     * Creates a new SavedList.
     * <p>
     *   If {@link CreateSavedListRequest#getCandidateIds()} is not null then
     *   it initializes the contents of the list, otherwise the list is
     *   initialized as empty.
     * </p>
     * @param request Request defining new list plus optional initial contents.
     * @return The details about the list - but not the contents.  
     * @throws EntityExistsException if a list with this name already exists.
     */
    @PostMapping
    public Map<String, Object> create(@Valid @RequestBody CreateSavedListRequest request) 
            throws EntityExistsException {
        SavedList savedList = savedListService.createSavedList(request);
        DtoBuilder builder = builderSelector.selectBuilder();
        return builder.build(savedList);
    }

    /**
     * Deletes the saved list with the given id.
     * @param id Requested id
     * @return True if list was deleted, false if it was not found.
     * @throws InvalidRequestException if not authorized to delete this list.
     */
    @DeleteMapping("{id}")
    public boolean delete(@PathVariable("id") long id) 
            throws InvalidRequestException {
        return savedListService.deleteSavedList(id);
    }

    /**
     * Gets the save list with the given id.
     * @param id Requested id
     * @return The details about the list - but not the contents.
     * @throws NoSuchObjectException if there is no saved list with this id. 
     */
    @GetMapping("{id}")
    public Map<String, Object> get(@PathVariable("id") long id) 
            throws NoSuchObjectException {
        SavedList savedList = savedListService.get(id);
        DtoBuilder builder = builderSelector.selectBuilder();
        return builder.build(savedList);
    }

    /**
     * Returns all saved lists matching the request. 
     * On this API any paging or sorting info in the request is ignored.
     * The sort is hard coded to ascending by name.
     * <p/>
     * See also {@link #search(SearchSavedListRequest)} .
     * @param request Defines which lists should be returned. Any paging or
     *                sorting fields in the request are ignored.
     * @return All matching SavedLists
     */
    @PostMapping("list")
    public List<Map<String, Object>> listSavedLists(
            @Valid @RequestBody SearchSavedListRequest request) {
        List<SavedList> savedLists = savedListService.listSavedLists(request);
        DtoBuilder builder = builderSelector.selectBuilder();
        return builder.buildList(savedLists);
    }

    /**
     * Returns the requested page of saved lists matching the request. 
     * On this API any sorting info in the request is ignored.
     * The sort is hard coded to ascending by name.
     * <p/>
     * See also {@link #listSavedLists(SearchSavedListRequest)} 
     * @param request Defines which lists should be returned. Any sorting fields 
     *                in the request are ignored.
     * @return Requested page of matching SavedLists
     */
    @PostMapping("search")
    public Map<String, Object> search(
            @Valid @RequestBody SearchSavedListRequest request) {
        Page<SavedList> savedLists = savedListService.searchSavedLists(request);
        DtoBuilder builder = builderSelector.selectBuilder();
        return builder.buildPage(savedLists);
    }

    @PutMapping("{id}")
    public Map<String, Object> update(@PathVariable("id") long id,
                        @Valid @RequestBody UpdateSavedListInfoRequest request)
            throws NoSuchObjectException, EntityExistsException {
        SavedList savedList = savedListService.updateSavedList(id, request);
        DtoBuilder builder = builderSelector.selectBuilder();
        return builder.build(savedList);
    }
}
