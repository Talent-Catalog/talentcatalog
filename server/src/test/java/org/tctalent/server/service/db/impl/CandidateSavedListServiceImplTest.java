package org.tctalent.server.service.db.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tctalent.server.exception.EntityExistsException;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.InvalidSessionException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.exception.UnauthorisedActionException;
import org.tctalent.server.model.db.AttachmentType;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateAttachment;
import org.tctalent.server.model.db.CandidateSavedList;
import org.tctalent.server.model.db.CandidateSavedListKey;
import org.tctalent.server.model.db.Country;
import org.tctalent.server.model.db.SavedList;
import org.tctalent.server.model.db.SavedSearch;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.CandidateAttachmentRepository;
import org.tctalent.server.repository.db.CandidateSavedListRepository;
import org.tctalent.server.repository.db.SavedListRepository;
import org.tctalent.server.request.candidate.IHasSetOfSavedLists;
import org.tctalent.server.request.candidate.UpdateCandidateContextNoteRequest;
import org.tctalent.server.request.candidate.UpdateCandidateShareableDocsRequest;
import org.tctalent.server.request.candidate.source.CopySourceContentsRequest;
import org.tctalent.server.request.list.ContentUpdateType;
import org.tctalent.server.request.list.UpdateExplicitSavedListContentsRequest;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.service.db.FileSystemService;
import org.tctalent.server.service.db.SavedListService;
import org.tctalent.server.service.db.UserService;
import org.tctalent.server.util.filesystem.GoogleFileSystemFile;

@ExtendWith(MockitoExtension.class)
class CandidateSavedListServiceImplTest {

  @Mock
  private AuthService authService;

  @Mock
  private CandidateService candidateService;

  @Mock
  private FileSystemService fileSystemService;

  @Mock
  private SavedListService savedListService;

  @Mock
  private SavedListRepository savedListRepository;

  @Mock
  private UserService userService;

  @Mock
  private CandidateAttachmentRepository candidateAttachmentRepository;

  @Mock
  private CandidateSavedListRepository candidateSavedListRepository;

  @InjectMocks
  private CandidateSavedListServiceImpl service;

  @Test
  void clearCandidateSavedListsRemovesCandidateFromEverySavedList() {
    Candidate candidate = mock(Candidate.class);
    SavedList first = mock(SavedList.class);
    SavedList second = mock(SavedList.class);

    when(candidate.getSavedLists()).thenReturn(Set.of(first, second));

    service.clearCandidateSavedLists(candidate);

    verify(savedListService).removeCandidateFromList(candidate, first);
    verify(savedListService).removeCandidateFromList(candidate, second);
  }

  @Test
  void clearCandidateSavedListsByIdReturnsFalseWhenCandidateDoesNotExist() {
    when(candidateService.findByIdLoadSavedLists(10L)).thenReturn(null);

    assertFalse(service.clearCandidateSavedLists(10L));

    verifyNoInteractions(savedListService);
  }

  @Test
  void clearCandidateSavedListsByIdReturnsTrueAndClearsWhenCandidateExists() {
    Candidate candidate = mock(Candidate.class);
    SavedList savedList = mock(SavedList.class);

    when(candidateService.findByIdLoadSavedLists(10L)).thenReturn(candidate);
    when(candidate.getSavedLists()).thenReturn(Set.of(savedList));

    assertTrue(service.clearCandidateSavedLists(10L));

    verify(savedListService).removeCandidateFromList(candidate, savedList);
  }

  @Test
  void clearSavedListCandidatesRemovesEveryCandidateFromList() {
    SavedList savedList = mock(SavedList.class);
    Candidate first = mock(Candidate.class);
    Candidate second = mock(Candidate.class);

    when(savedList.getCandidates()).thenReturn(Set.of(first, second));

    service.clearSavedListCandidates(savedList);

    verify(savedListService).removeCandidateFromList(first, savedList);
    verify(savedListService).removeCandidateFromList(second, savedList);
  }

  @Test
  void clearSavedListReturnsFalseWhenListDoesNotExist() {
    when(savedListRepository.findByIdLoadCandidates(99L)).thenReturn(Optional.empty());

    assertFalse(service.clearSavedList(99L));

    verifyNoInteractions(savedListService);
  }

  @Test
  void clearSavedListReturnsTrueAndClearsCandidatesWhenListExists() {
    SavedList savedList = mock(SavedList.class);
    Candidate candidate = mock(Candidate.class);

    when(savedListRepository.findByIdLoadCandidates(99L)).thenReturn(Optional.of(savedList));
    when(savedList.getCandidates()).thenReturn(Set.of(candidate));

    assertTrue(service.clearSavedList(99L));

    verify(savedListService).removeCandidateFromList(candidate, savedList);
  }

  @Test
  void copyByIdThrowsWhenSourceListDoesNotExist() {
    CopySourceContentsRequest request = mock(CopySourceContentsRequest.class);

    when(savedListRepository.findByIdLoadCandidates(1L)).thenReturn(Optional.empty());

    assertThrows(NoSuchObjectException.class, () -> service.copy(1L, request));
  }

  @Test
  void copyByIdCopiesWhenSourceListExists()
      throws EntityExistsException, NoSuchObjectException {
    SavedList source = mock(SavedList.class);
    SavedList target = mock(SavedList.class);
    Candidate candidate = mock(Candidate.class);
    CopySourceContentsRequest request = mock(CopySourceContentsRequest.class);

    when(savedListRepository.findByIdLoadCandidates(1L)).thenReturn(Optional.of(source));
    when(request.getSavedListId()).thenReturn(0L);
    when(request.getNewListName()).thenReturn("Copied");
    when(request.getUpdateType()).thenReturn(null);
    when(savedListService.createSavedList(request)).thenReturn(target);
    when(source.getSavedSearchSource()).thenReturn(null);
    when(source.getCandidates()).thenReturn(Set.of(candidate));

    SavedList result = service.copy(1L, request);

    assertSame(target, result);
    verify(request).setName("Copied");
    verify(savedListService).addCandidatesToList(target, Set.of(candidate), source);
    verify(savedListService).saveIt(target);
  }

  @Test
  void copyCreatesNewListCopiesNameSavedSearchAndContents()
      throws EntityExistsException, NoSuchObjectException {
    SavedList source = mock(SavedList.class);
    SavedList target = mock(SavedList.class);
    SavedSearch savedSearch = mock(SavedSearch.class);
    Candidate candidate = mock(Candidate.class);
    CopySourceContentsRequest request = mock(CopySourceContentsRequest.class);

    when(request.getSavedListId()).thenReturn(0L);
    when(request.getNewListName()).thenReturn("New copied list");
    when(request.getUpdateType()).thenReturn(null);
    when(savedListService.createSavedList(request)).thenReturn(target);

    when(source.getSavedSearchSource()).thenReturn(savedSearch);
    when(source.getCandidates()).thenReturn(Set.of(candidate));

    SavedList result = service.copy(source, request);

    assertSame(target, result);
    verify(request).setName("New copied list");
    verify(savedListService).createSavedList(request);
    verify(target).setSavedSearchSource(savedSearch);
    verify(savedListService).addCandidatesToList(target, Set.of(candidate), source);
    verify(savedListService).saveIt(target);
  }

  @Test
  void copyToExistingListThrowsWhenTargetListDoesNotExist() {
    SavedList source = mock(SavedList.class);
    CopySourceContentsRequest request = mock(CopySourceContentsRequest.class);

    when(request.getSavedListId()).thenReturn(44L);
    when(savedListRepository.findByIdLoadCandidates(44L)).thenReturn(Optional.empty());

    assertThrows(NoSuchObjectException.class, () -> service.copy(source, request));
  }

  @Test
  void copyToExistingListWithReplaceInheritsSavedSearchAndClearsTarget()
      throws EntityExistsException, NoSuchObjectException {
    SavedList source = mock(SavedList.class);
    SavedList target = mock(SavedList.class);
    SavedSearch savedSearch = mock(SavedSearch.class);
    Candidate candidate = mock(Candidate.class);
    CopySourceContentsRequest request = mock(CopySourceContentsRequest.class);

    when(request.getSavedListId()).thenReturn(44L);
    when(request.getUpdateType()).thenReturn(ContentUpdateType.replace);

    when(savedListRepository.findByIdLoadCandidates(44L))
        .thenReturn(Optional.of(target))
        .thenReturn(Optional.empty());

    when(target.getId()).thenReturn(44L);
    when(source.getSavedSearchSource()).thenReturn(savedSearch);
    when(source.getCandidates()).thenReturn(Set.of(candidate));

    SavedList result = service.copy(source, request);

    assertSame(target, result);
    verify(target).setSavedSearchSource(savedSearch);
    verify(savedListService).addCandidatesToList(target, Set.of(candidate), source);
    verify(savedListService).saveIt(target);
  }

  @Test
  void copyToExistingListWithoutReplaceDoesNotInheritSavedSearchOrClearTarget()
      throws EntityExistsException, NoSuchObjectException {
    SavedList source = mock(SavedList.class);
    SavedList target = mock(SavedList.class);
    Candidate candidate = mock(Candidate.class);
    CopySourceContentsRequest request = mock(CopySourceContentsRequest.class);

    when(request.getSavedListId()).thenReturn(44L);
    when(request.getUpdateType()).thenReturn(null);
    when(savedListRepository.findByIdLoadCandidates(44L)).thenReturn(Optional.of(target));
    when(source.getCandidates()).thenReturn(Set.of(candidate));

    SavedList result = service.copy(source, request);

    assertSame(target, result);
    verify(target, never()).setSavedSearchSource(any());
    verify(target, never()).getId();
    verify(savedListService).addCandidatesToList(target, Set.of(candidate), source);
    verify(savedListService).saveIt(target);
  }

  @Test
  void copyContentsWithoutReplaceAddsCandidatesAndSavesDestination() {
    SavedList source = mock(SavedList.class);
    SavedList destination = mock(SavedList.class);
    Candidate candidate = mock(Candidate.class);

    when(source.getCandidates()).thenReturn(Set.of(candidate));

    service.copyContents(source, destination, false);

    verify(savedListRepository, never()).findByIdLoadCandidates(anyLong());
    verify(savedListService).addCandidatesToList(destination, Set.of(candidate), source);
    verify(savedListService).saveIt(destination);
  }

  @Test
  void copyContentsFromExplicitRequestWithReplaceSourceSearchAndCandidates() {
    UpdateExplicitSavedListContentsRequest request =
        mock(UpdateExplicitSavedListContentsRequest.class);
    SavedList destination = mock(SavedList.class);
    SavedList source = mock(SavedList.class);
    SavedSearch savedSearch = mock(SavedSearch.class);
    Candidate candidate = mock(Candidate.class);

    when(request.getUpdateType()).thenReturn(ContentUpdateType.replace);
    when(destination.getId()).thenReturn(7L);
    when(savedListRepository.findByIdLoadCandidates(7L)).thenReturn(Optional.empty());
    when(savedListService.fetchSourceList(request)).thenReturn(source);
    when(source.getSavedSearchSource()).thenReturn(savedSearch);
    when(savedListService.fetchCandidates(request)).thenReturn(Set.of(candidate));

    service.copyContents(request, destination);

    verify(destination).setSavedSearchSource(savedSearch);
    verify(savedListService).addCandidatesToList(destination, Set.of(candidate), source);
    verify(savedListService).saveIt(destination);
  }

  @Test
  void copyContentsFromExplicitRequestWithSourceButNoSavedSearchDoesNotSetSavedSearch() {
    UpdateExplicitSavedListContentsRequest request =
        mock(UpdateExplicitSavedListContentsRequest.class);
    SavedList destination = mock(SavedList.class);
    SavedList source = mock(SavedList.class);
    Candidate candidate = mock(Candidate.class);

    when(request.getUpdateType()).thenReturn(null);
    when(savedListService.fetchSourceList(request)).thenReturn(source);
    when(source.getSavedSearchSource()).thenReturn(null);
    when(savedListService.fetchCandidates(request)).thenReturn(Set.of(candidate));

    service.copyContents(request, destination);

    verify(destination, never()).setSavedSearchSource(any());
    verify(savedListService).addCandidatesToList(destination, Set.of(candidate), source);
    verify(savedListService).saveIt(destination);
  }

  @Test
  void copyContentsFromExplicitRequestHandlesNoSourceList() {
    UpdateExplicitSavedListContentsRequest request =
        mock(UpdateExplicitSavedListContentsRequest.class);
    SavedList destination = mock(SavedList.class);
    Candidate candidate = mock(Candidate.class);

    when(request.getUpdateType()).thenReturn(null);
    when(savedListService.fetchSourceList(request)).thenReturn(null);
    when(savedListService.fetchCandidates(request)).thenReturn(Set.of(candidate));

    service.copyContents(request, destination);

    verify(destination, never()).setSavedSearchSource(any());
    verify(savedListService).addCandidatesToList(destination, Set.of(candidate), null);
    verify(savedListService).saveIt(destination);
  }

  @Test
  void deleteSavedListReturnsFalseWhenListDoesNotExist() {
    when(savedListRepository.findByIdLoadCandidates(20L)).thenReturn(Optional.empty());
    when(authService.getLoggedInUser()).thenReturn(Optional.of(mock(User.class)));

    assertFalse(service.deleteSavedList(20L));

    verify(savedListRepository, never()).delete(any(SavedList.class));
  }

  @Test
  void deleteSavedListReturnsFalseWhenNoLoggedInUser() {
    SavedList savedList = mock(SavedList.class);

    when(savedListRepository.findByIdLoadCandidates(20L)).thenReturn(Optional.of(savedList));
    when(authService.getLoggedInUser()).thenReturn(Optional.empty());

    assertFalse(service.deleteSavedList(20L));

    verify(savedListRepository, never()).delete(any(SavedList.class));
  }

  @Test
  void deleteSavedListThrowsWhenLoggedInUserDoesNotOwnList() {
    SavedList savedList = mock(SavedList.class);
    User owner = mock(User.class);
    User loggedInUser = mock(User.class);

    when(savedListRepository.findByIdLoadCandidates(20L)).thenReturn(Optional.of(savedList));
    when(authService.getLoggedInUser()).thenReturn(Optional.of(loggedInUser));
    when(savedList.getCreatedBy()).thenReturn(owner);
    when(owner.getId()).thenReturn(1L);
    when(loggedInUser.getId()).thenReturn(2L);

    assertThrows(InvalidRequestException.class, () -> service.deleteSavedList(20L));

    verify(savedListRepository, never()).delete(savedList);
  }

  @Test
  void deleteSavedListClearsRelationsAndDeletesWhenOwner() {
    SavedList savedList = mock(SavedList.class);
    Candidate candidate = mock(Candidate.class);
    User owner = mock(User.class);

    when(savedListRepository.findByIdLoadCandidates(20L))
        .thenReturn(Optional.of(savedList))
        .thenReturn(Optional.of(savedList));

    when(authService.getLoggedInUser()).thenReturn(Optional.of(owner));
    when(savedList.getCreatedBy()).thenReturn(owner);
    when(owner.getId()).thenReturn(1L);
    when(savedList.getId()).thenReturn(20L);
    when(savedList.getCandidates()).thenReturn(Set.of(candidate));

    assertTrue(service.deleteSavedList(20L));

    verify(savedListService).removeCandidateFromList(candidate, savedList);
    verify(savedList).setWatcherIds(null);
    verify(savedList).setUsers(null);
    verify(savedListRepository).delete(savedList);
  }

  @Test
  void findByCandidateIdsMapsCandidateSavedListsByCandidateId() {
    Candidate firstCandidate = mock(Candidate.class);
    Candidate secondCandidate = mock(Candidate.class);
    CandidateSavedList first = mock(CandidateSavedList.class);
    CandidateSavedList second = mock(CandidateSavedList.class);

    when(firstCandidate.getId()).thenReturn(10L);
    when(secondCandidate.getId()).thenReturn(20L);
    when(first.getCandidate()).thenReturn(firstCandidate);
    when(second.getCandidate()).thenReturn(secondCandidate);
    when(candidateSavedListRepository.findBySavedList_Id(5L))
        .thenReturn(List.of(first, second));

    Map<Long, CandidateSavedList> result = service.findByCandidateIds(Set.of(10L), 5L);

    assertEquals(2, result.size());
    assertSame(first, result.get(10L));
    assertSame(second, result.get(20L));
  }

  @Test
  void mergeCandidateSavedListsReturnsFalseWhenCandidateMissing() {
    IHasSetOfSavedLists request = mock(IHasSetOfSavedLists.class);

    when(candidateService.findByIdLoadSavedLists(1L)).thenReturn(null);

    assertFalse(service.mergeCandidateSavedLists(1L, request));

    verify(candidateService, never()).save(any());
  }

  @Test
  void mergeCandidateSavedListsAddsFetchedListsAndSavesCandidate() {
    Candidate candidate = mock(Candidate.class);
    SavedList first = mock(SavedList.class);
    SavedList second = mock(SavedList.class);
    IHasSetOfSavedLists request = mock(IHasSetOfSavedLists.class);

    when(candidateService.findByIdLoadSavedLists(1L)).thenReturn(candidate);
    when(request.getSavedListIds()).thenReturn(Set.of(11L, 22L));
    when(savedListService.get(11L)).thenReturn(first);
    when(savedListService.get(22L)).thenReturn(second);

    assertTrue(service.mergeCandidateSavedLists(1L, request));

    ArgumentCaptor<Set<SavedList>> captor = ArgumentCaptor.forClass(Set.class);
    verify(candidate).addSavedLists(captor.capture());
    assertTrue(captor.getValue().contains(first));
    assertTrue(captor.getValue().contains(second));
    verify(candidateService).save(candidate);
  }

  @Test
  void mergeCandidateSavedListsHandlesNullSavedListIdsAsEmptySet() {
    Candidate candidate = mock(Candidate.class);
    IHasSetOfSavedLists request = mock(IHasSetOfSavedLists.class);

    when(candidateService.findByIdLoadSavedLists(1L)).thenReturn(candidate);
    when(request.getSavedListIds()).thenReturn(null);

    assertTrue(service.mergeCandidateSavedLists(1L, request));

    ArgumentCaptor<Set<SavedList>> captor = ArgumentCaptor.forClass(Set.class);
    verify(candidate).addSavedLists(captor.capture());
    assertTrue(captor.getValue().isEmpty());
    verify(candidateService).save(candidate);
  }

  @Test
  void removeFromCandidateSavedListsReturnsFalseWhenCandidateMissing() {
    IHasSetOfSavedLists request = mock(IHasSetOfSavedLists.class);

    when(candidateService.findByIdLoadSavedLists(1L)).thenReturn(null);

    assertFalse(service.removeFromCandidateSavedLists(1L, request));

    verify(savedListService, never()).removeCandidateFromList(any(), any());
  }

  @Test
  void removeFromCandidateSavedListsRemovesFetchedLists() {
    Candidate candidate = mock(Candidate.class);
    SavedList first = mock(SavedList.class);
    SavedList second = mock(SavedList.class);
    IHasSetOfSavedLists request = mock(IHasSetOfSavedLists.class);

    when(candidateService.findByIdLoadSavedLists(1L)).thenReturn(candidate);
    when(request.getSavedListIds()).thenReturn(Set.of(11L, 22L));
    when(savedListService.get(11L)).thenReturn(first);
    when(savedListService.get(22L)).thenReturn(second);

    assertTrue(service.removeFromCandidateSavedLists(1L, request));

    verify(savedListService).removeCandidateFromList(candidate, first);
    verify(savedListService).removeCandidateFromList(candidate, second);
  }

  @Test
  void updateCandidateContextNoteUpdatesAndSavesWhenRelationExists() {
    UpdateCandidateContextNoteRequest request =
        mock(UpdateCandidateContextNoteRequest.class);
    CandidateSavedList candidateSavedList = mock(CandidateSavedList.class);

    when(request.getCandidateId()).thenReturn(10L);
    when(request.getContextNote()).thenReturn("Important context");
    when(candidateSavedListRepository.findById(any(CandidateSavedListKey.class)))
        .thenReturn(Optional.of(candidateSavedList));

    service.updateCandidateContextNote(5L, request);

    verify(candidateSavedList).setContextNote("Important context");
    verify(candidateSavedListRepository).save(candidateSavedList);
  }

  @Test
  void updateCandidateContextNoteDoesNothingWhenRelationMissing() {
    UpdateCandidateContextNoteRequest request =
        mock(UpdateCandidateContextNoteRequest.class);

    when(request.getCandidateId()).thenReturn(10L);
    when(candidateSavedListRepository.findById(any(CandidateSavedListKey.class)))
        .thenReturn(Optional.empty());

    service.updateCandidateContextNote(5L, request);

    verify(candidateSavedListRepository, never()).save(any());
  }

  @Test
  void updateCandidateShareableDocsUpdatesAndSavesWhenRelationExists() {
    CandidateSavedList candidateSavedList = mock(CandidateSavedList.class);
    CandidateAttachment cv = mock(CandidateAttachment.class);
    CandidateAttachment doc = mock(CandidateAttachment.class);

    when(candidateSavedListRepository.findById(any(CandidateSavedListKey.class)))
        .thenReturn(Optional.of(candidateSavedList));

    service.updateCandidateShareableDocs(1L, 2L, cv, doc);

    verify(candidateSavedList).setShareableCv(cv);
    verify(candidateSavedList).setShareableDoc(doc);
    verify(candidateSavedListRepository).save(candidateSavedList);
  }

  @Test
  void updateCandidateShareableDocsDoesNothingWhenRelationMissing() {
    CandidateAttachment cv = mock(CandidateAttachment.class);
    CandidateAttachment doc = mock(CandidateAttachment.class);

    when(candidateSavedListRepository.findById(any(CandidateSavedListKey.class)))
        .thenReturn(Optional.empty());

    service.updateCandidateShareableDocs(1L, 2L, cv, doc);

    verify(candidateSavedListRepository, never()).save(any());
  }

  @Test
  void updateShareableDocsThrowsWhenUserNotLoggedIn() {
    UpdateCandidateShareableDocsRequest request =
        mock(UpdateCandidateShareableDocsRequest.class);

    when(authService.getLoggedInUser()).thenReturn(Optional.empty());

    assertThrows(InvalidSessionException.class,
        () -> service.updateShareableDocs(1L, request));
  }

  @Test
  void updateShareableDocsThrowsWhenCandidateNotFound() {
    UpdateCandidateShareableDocsRequest request =
        mock(UpdateCandidateShareableDocsRequest.class);
    User user = mock(User.class);
    Set<Country> countries = Set.of(mock(Country.class));

    when(authService.getLoggedInUser()).thenReturn(Optional.of(user));
    when(userService.getDefaultSourceCountries(user)).thenReturn(countries);
    when(candidateService.findByIdLoadUser(1L, countries)).thenReturn(null);

    assertThrows(NoSuchObjectException.class,
        () -> service.updateShareableDocs(1L, request));
  }

  @Test
  void updateShareableDocsThrowsWhenCvAttachmentNotFound() {
    UpdateCandidateShareableDocsRequest request =
        mock(UpdateCandidateShareableDocsRequest.class);
    User user = mock(User.class);
    Candidate candidate = mock(Candidate.class);
    Set<Country> countries = Set.of(mock(Country.class));

    when(authService.getLoggedInUser()).thenReturn(Optional.of(user));
    when(userService.getDefaultSourceCountries(user)).thenReturn(countries);
    when(candidateService.findByIdLoadUser(1L, countries)).thenReturn(candidate);
    when(request.getShareableCvAttachmentId()).thenReturn(100L);
    when(candidateAttachmentRepository.findById(100L)).thenReturn(Optional.empty());

    assertThrows(NoSuchObjectException.class,
        () -> service.updateShareableDocs(1L, request));
  }

  @Test
  void updateShareableDocsThrowsWhenDocAttachmentNotFound() {
    UpdateCandidateShareableDocsRequest request =
        mock(UpdateCandidateShareableDocsRequest.class);
    User user = mock(User.class);
    Candidate candidate = mock(Candidate.class);
    CandidateAttachment cv = mock(CandidateAttachment.class);
    Set<Country> countries = Set.of(mock(Country.class));

    when(authService.getLoggedInUser()).thenReturn(Optional.of(user));
    when(userService.getDefaultSourceCountries(user)).thenReturn(countries);
    when(candidateService.findByIdLoadUser(1L, countries)).thenReturn(candidate);
    when(request.getShareableCvAttachmentId()).thenReturn(100L);
    when(request.getShareableDocAttachmentId()).thenReturn(200L);
    when(candidateAttachmentRepository.findById(100L)).thenReturn(Optional.of(cv));
    when(candidateAttachmentRepository.findById(200L)).thenReturn(Optional.empty());
    when(cv.getCandidate()).thenReturn(candidate);

    assertThrows(NoSuchObjectException.class,
        () -> service.updateShareableDocs(1L, request));
  }

  @Test
  void updateShareableDocsThrowsWhenAttachmentDoesNotBelongToCandidate() {
    UpdateCandidateShareableDocsRequest request =
        mock(UpdateCandidateShareableDocsRequest.class);
    User user = mock(User.class);
    Candidate candidate = mock(Candidate.class);
    Candidate otherCandidate = mock(Candidate.class);
    CandidateAttachment cv = mock(CandidateAttachment.class);
    Set<Country> countries = Set.of(mock(Country.class));

    when(authService.getLoggedInUser()).thenReturn(Optional.of(user));
    when(userService.getDefaultSourceCountries(user)).thenReturn(countries);
    when(candidateService.findByIdLoadUser(1L, countries)).thenReturn(candidate);
    when(request.getShareableCvAttachmentId()).thenReturn(100L);
    when(candidateAttachmentRepository.findById(100L)).thenReturn(Optional.of(cv));
    when(cv.getCandidate()).thenReturn(otherCandidate);
    when(cv.getName()).thenReturn("wrong.pdf");
    when(candidate.getCandidateNumber()).thenReturn("CAND-001");

    assertThrows(InvalidRequestException.class,
        () -> service.updateShareableDocs(1L, request));
  }

  @Test
  void updateShareableDocsWithoutAttachmentsClearsCandidateDocsDirectly()
      throws UnauthorisedActionException, IOException {
    UpdateCandidateShareableDocsRequest request =
        mock(UpdateCandidateShareableDocsRequest.class);
    User user = mock(User.class);
    Candidate candidate = mock(Candidate.class);
    Set<Country> countries = Set.of(mock(Country.class));

    when(authService.getLoggedInUser()).thenReturn(Optional.of(user));
    when(userService.getDefaultSourceCountries(user)).thenReturn(countries);
    when(candidateService.findByIdLoadUser(1L, countries)).thenReturn(candidate);
    when(request.getShareableCvAttachmentId()).thenReturn(null);
    when(request.getShareableDocAttachmentId()).thenReturn(null);
    when(request.getSavedListId()).thenReturn(null);
    when(candidateService.save(candidate)).thenReturn(candidate);

    Candidate result = service.updateShareableDocs(1L, request);

    assertSame(candidate, result);
    verify(candidate).setShareableCv(null);
    verify(candidate).setShareableDoc(null);
    verify(fileSystemService, never()).publishFile(any());
    verify(candidateService).save(candidate);
  }

  @Test
  void updateShareableDocsWithoutSavedListSetsCandidateDocsDirectly()
      throws UnauthorisedActionException, IOException {
    UpdateCandidateShareableDocsRequest request =
        mock(UpdateCandidateShareableDocsRequest.class);
    User user = mock(User.class);
    Candidate candidate = mock(Candidate.class);
    CandidateAttachment cv = mock(CandidateAttachment.class);
    CandidateAttachment doc = mock(CandidateAttachment.class);
    Set<Country> countries = Set.of(mock(Country.class));

    when(authService.getLoggedInUser()).thenReturn(Optional.of(user));
    when(userService.getDefaultSourceCountries(user)).thenReturn(countries);
    when(candidateService.findByIdLoadUser(1L, countries)).thenReturn(candidate);
    when(request.getShareableCvAttachmentId()).thenReturn(100L);
    when(request.getShareableDocAttachmentId()).thenReturn(200L);
    when(request.getSavedListId()).thenReturn(null);
    when(candidateAttachmentRepository.findById(100L)).thenReturn(Optional.of(cv));
    when(candidateAttachmentRepository.findById(200L)).thenReturn(Optional.of(doc));
    when(cv.getCandidate()).thenReturn(candidate);
    when(doc.getCandidate()).thenReturn(candidate);
    when(cv.getType()).thenReturn(null);
    when(doc.getType()).thenReturn(null);
    when(candidateService.save(candidate)).thenReturn(candidate);

    Candidate result = service.updateShareableDocs(1L, request);

    assertSame(candidate, result);
    verify(candidate).setShareableCv(cv);
    verify(candidate).setShareableDoc(doc);
    verify(fileSystemService, never()).publishFile(any());
    verify(candidateService).save(candidate);
  }

  @Test
  void updateShareableDocsWithSavedListPublishesGoogleFilesUpdatesRelationAndSetsMissingCandidateDocs()
      throws Exception {
    UpdateCandidateShareableDocsRequest request =
        mock(UpdateCandidateShareableDocsRequest.class);
    User user = mock(User.class);
    Candidate candidate = mock(Candidate.class);
    CandidateAttachment cv = mock(CandidateAttachment.class);
    CandidateAttachment doc = mock(CandidateAttachment.class);
    CandidateSavedList candidateSavedList = mock(CandidateSavedList.class);
    Set<Country> countries = Set.of(mock(Country.class));

    when(authService.getLoggedInUser()).thenReturn(Optional.of(user));
    when(userService.getDefaultSourceCountries(user)).thenReturn(countries);
    when(candidateService.findByIdLoadUser(1L, countries)).thenReturn(candidate);

    when(request.getShareableCvAttachmentId()).thenReturn(100L);
    when(request.getShareableDocAttachmentId()).thenReturn(200L);
    when(request.getSavedListId()).thenReturn(9L);

    when(candidateAttachmentRepository.findById(100L)).thenReturn(Optional.of(cv));
    when(candidateAttachmentRepository.findById(200L)).thenReturn(Optional.of(doc));

    when(cv.getCandidate()).thenReturn(candidate);
    when(doc.getCandidate()).thenReturn(candidate);
    when(cv.getType()).thenReturn(AttachmentType.googlefile);
    when(doc.getType()).thenReturn(AttachmentType.googlefile);
    when(cv.getUrl()).thenReturn("https://drive.google.com/cv");
    when(doc.getUrl()).thenReturn("https://drive.google.com/doc");

    when(candidate.getShareableCv()).thenReturn(null);
    when(candidate.getShareableDoc()).thenReturn(null);

    when(candidateSavedListRepository.findById(any(CandidateSavedListKey.class)))
        .thenReturn(Optional.of(candidateSavedList));
    when(candidateService.save(candidate)).thenReturn(candidate);

    Candidate result = service.updateShareableDocs(1L, request);

    assertSame(candidate, result);
    verify(fileSystemService, times(2)).publishFile(any(GoogleFileSystemFile.class));
    verify(candidateSavedList).setShareableCv(cv);
    verify(candidateSavedList).setShareableDoc(doc);
    verify(candidateSavedListRepository).save(candidateSavedList);
    verify(candidate).setShareableCv(cv);
    verify(candidate).setShareableDoc(doc);
    verify(candidateService).save(candidate);
  }

  @Test
  void updateShareableDocsWithSavedListDoesNotOverwriteExistingCandidateDocs()
      throws Exception {
    UpdateCandidateShareableDocsRequest request =
        mock(UpdateCandidateShareableDocsRequest.class);
    User user = mock(User.class);
    Candidate candidate = mock(Candidate.class);
    CandidateAttachment existingCv = mock(CandidateAttachment.class);
    CandidateAttachment existingDoc = mock(CandidateAttachment.class);
    CandidateAttachment newCv = mock(CandidateAttachment.class);
    CandidateAttachment newDoc = mock(CandidateAttachment.class);
    CandidateSavedList candidateSavedList = mock(CandidateSavedList.class);
    Set<Country> countries = Set.of(mock(Country.class));

    when(authService.getLoggedInUser()).thenReturn(Optional.of(user));
    when(userService.getDefaultSourceCountries(user)).thenReturn(countries);
    when(candidateService.findByIdLoadUser(1L, countries)).thenReturn(candidate);

    when(request.getShareableCvAttachmentId()).thenReturn(100L);
    when(request.getShareableDocAttachmentId()).thenReturn(200L);
    when(request.getSavedListId()).thenReturn(9L);

    when(candidateAttachmentRepository.findById(100L)).thenReturn(Optional.of(newCv));
    when(candidateAttachmentRepository.findById(200L)).thenReturn(Optional.of(newDoc));

    when(newCv.getCandidate()).thenReturn(candidate);
    when(newDoc.getCandidate()).thenReturn(candidate);
    when(newCv.getType()).thenReturn(null);
    when(newDoc.getType()).thenReturn(null);

    when(candidate.getShareableCv()).thenReturn(existingCv);
    when(candidate.getShareableDoc()).thenReturn(existingDoc);

    when(candidateSavedListRepository.findById(any(CandidateSavedListKey.class)))
        .thenReturn(Optional.of(candidateSavedList));
    when(candidateService.save(candidate)).thenReturn(candidate);

    Candidate result = service.updateShareableDocs(1L, request);

    assertSame(candidate, result);
    verify(candidateSavedList).setShareableCv(newCv);
    verify(candidateSavedList).setShareableDoc(newDoc);
    verify(candidate, never()).setShareableCv(newCv);
    verify(candidate, never()).setShareableDoc(newDoc);
    verify(candidateService).save(candidate);
  }

}