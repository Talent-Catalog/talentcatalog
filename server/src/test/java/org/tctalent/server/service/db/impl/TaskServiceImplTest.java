/*
 * Copyright (c) 2026 Talent Catalog.
 */

package org.tctalent.server.service.db.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import java.beans.IntrospectionException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.tctalent.server.exception.EntityExistsException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.TaskImpl;
import org.tctalent.server.model.db.task.AllowedQuestionTaskAnswer;
import org.tctalent.server.model.db.task.QuestionTask;
import org.tctalent.server.repository.db.TaskRepository;
import org.tctalent.server.request.task.SearchTaskRequest;
import org.tctalent.server.request.task.UpdateTaskRequest;
import org.tctalent.server.util.BeanHelper;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

  @Mock
  private TaskRepository taskRepository;

  private TaskServiceImpl taskService() {
    return new TaskServiceImpl(taskRepository);
  }

  @Test
  @DisplayName("get returns task and populates enum allowed answers")
  void getReturnsTaskAndPopulatesTransientFields() {
    TestQuestionTask task = questionTask(1L, "availImmediate");

    given(taskRepository.findById(1L)).willReturn(Optional.of(task));

    TaskImpl result = taskService().get(1L);

    assertSame(task, result);
    assertAllowedAnswerNames(task, "Yes", "No");
  }

  @Test
  @DisplayName("get throws when task does not exist")
  void getThrowsWhenTaskMissing() {
    given(taskRepository.findById(99L)).willReturn(Optional.empty());

    assertThrows(NoSuchObjectException.class, () -> taskService().get(99L));
  }

  @Test
  @DisplayName("getByName returns task")
  void getByNameReturnsTask() {
    TaskImpl task = task(2L);

    given(taskRepository.findByLowerName("myTask")).willReturn(Optional.of(task));

    TaskImpl result = taskService().getByName("myTask");

    assertSame(task, result);
  }

  @Test
  @DisplayName("getByName throws when task does not exist")
  void getByNameThrowsWhenTaskMissing() {
    given(taskRepository.findByLowerName("missing")).willReturn(Optional.empty());

    assertThrows(NoSuchObjectException.class, () -> taskService().getByName("missing"));
  }

  @Test
  @DisplayName("listTasks returns sorted tasks and populates transient fields")
  void listTasksReturnsSortedTasksAndPopulatesTransientFields() {
    TestQuestionTask questionTask = questionTask(1L, "availImmediate");
    TaskImpl simpleTask = task(2L);
    List<TaskImpl> tasks = List.of(questionTask, simpleTask);

    given(taskRepository.findAll(Sort.by(Direction.ASC, "name"))).willReturn(tasks);

    List<TaskImpl> result = taskService().listTasks();

    assertSame(tasks, result);
    assertAllowedAnswerNames(questionTask, "Yes", "No");
  }

  @Test
  @DisplayName("searchTasks returns page and populates task content")
  void searchTasksReturnsPageAndPopulatesTransientFields() {
    SearchTaskRequest request = new SearchTaskRequest();
    request.setKeyword("visa");
    request.setPageNumber(0);
    request.setPageSize(10);

    TestQuestionTask questionTask = questionTask(1L, "candidateExams.score");
    Page<TaskImpl> page = new PageImpl<>(List.of(questionTask));

    given(taskRepository.findAll(any(Specification.class), eq(request.getPageRequest())))
        .willReturn(page);

    Page<TaskImpl> result = taskService().searchTasks(request);

    assertSame(page, result);
    assertNull(questionTask.getAllowedAnswers());
  }

  @Test
  @DisplayName("update saves all editable fields when doc link is present")
  void updateSavesEditableFieldsWhenDocLinkPresent() {
    TaskImpl task = task(1L);
    UpdateTaskRequest request = updateRequest("New display", "New description", 7,
        "https://example.org/doc", true, true);

    given(taskRepository.findById(1L)).willReturn(Optional.of(task));
    given(taskRepository.findByLowerDisplayName("New display")).willReturn(null);
    given(taskRepository.save(task)).willReturn(task);

    TaskImpl result = taskService().update(1L, request);

    assertSame(task, result);
    assertEquals("New display", task.getDisplayName());
    assertEquals("New description", task.getDescription());
    assertEquals(7, task.getDaysToComplete());
    assertEquals("https://example.org/doc", task.getDocLink());
    assertEquals(true, task.isOptional());
    assertEquals(true, task.isNotifyOnAssignment());
  }

  @Test
  @DisplayName("update clears doc link when request doc link is blank")
  void updateClearsDocLinkWhenBlank() {
    TaskImpl task = task(1L);
    task.setDocLink("https://old.example.org");

    TaskImpl duplicateWithSameId = task(1L);
    UpdateTaskRequest request = updateRequest("Same task", "Description", 3,
        "   ", false, false);

    given(taskRepository.findById(1L)).willReturn(Optional.of(task));
    given(taskRepository.findByLowerDisplayName("Same task")).willReturn(duplicateWithSameId);
    given(taskRepository.save(task)).willReturn(task);

    TaskImpl result = taskService().update(1L, request);

    assertSame(task, result);
    assertNull(task.getDocLink());
    assertEquals(false, task.isOptional());
    assertEquals(false, task.isNotifyOnAssignment());
  }

  @Test
  @DisplayName("update throws when task id does not exist")
  void updateThrowsWhenTaskMissing() {
    UpdateTaskRequest request = updateRequest("Missing", "Description", 1,
        null, false, false);

    given(taskRepository.findById(404L)).willReturn(Optional.empty());

    assertThrows(NoSuchObjectException.class, () -> taskService().update(404L, request));

    then(taskRepository).should(never()).findByLowerDisplayName(any());
    then(taskRepository).should(never()).save(any());
  }

  @Test
  @DisplayName("update throws when another task has requested display name")
  void updateThrowsWhenDuplicateDisplayNameExists() {
    TaskImpl task = task(1L);
    TaskImpl duplicate = task(2L);
    UpdateTaskRequest request = updateRequest("Duplicate", "Description", 1,
        null, false, false);

    given(taskRepository.findById(1L)).willReturn(Optional.of(task));
    given(taskRepository.findByLowerDisplayName("Duplicate")).willReturn(duplicate);

    assertThrows(EntityExistsException.class, () -> taskService().update(1L, request));

    then(taskRepository).should(never()).save(any());
  }

  @Test
  @DisplayName("populateTransientFields does nothing for non-question task")
  void populateTransientFieldsDoesNothingForNonQuestionTask() {
    TaskImpl task = task(1L);

    taskService().populateTransientFields(task);

    assertNull(task.getDocLink());
  }

  @Test
  @DisplayName("populateTransientFields maps explicit answers when candidate field is null")
  void populateTransientFieldsMapsExplicitAnswers() {
    TestQuestionTask task = questionTask(1L, null);
    task.setExplicitAllowedAnswers(List.of("A", "B"));

    taskService().populateTransientFields(task);

    assertAllowedAnswerNames(task, "A", "B");
    assertAllowedAnswerDisplayNames(task, "A", "B");
  }

  @Test
  @DisplayName("populateTransientFields does nothing when candidate field and explicit answers are null")
  void populateTransientFieldsDoesNothingWhenNoFieldAndNoExplicitAnswers() {
    TestQuestionTask task = questionTask(1L, null);

    taskService().populateTransientFields(task);

    assertNull(task.getAllowedAnswers());
  }

  @Test
  @DisplayName("populateTransientFields skips candidateExams answer fields")
  void populateTransientFieldsSkipsCandidateExamsFields() {
    TestQuestionTask task = questionTask(1L, "candidateExams.exam");

    taskService().populateTransientFields(task);

    assertNull(task.getAllowedAnswers());
  }

  @Test
  @DisplayName("populateTransientFields does nothing for non-enum candidate field")
  void populateTransientFieldsDoesNothingForNonEnumCandidateField() {
    TestQuestionTask task = questionTask(1L, "candidateNumber");

    taskService().populateTransientFields(task);

    assertNull(task.getAllowedAnswers());
  }

  @Test
  @DisplayName("populateTransientFields throws when candidate field does not exist")
  void populateTransientFieldsThrowsWhenCandidateFieldMissing() {
    TestQuestionTask task = questionTask(1L, "notARealCandidateField");

    assertThrows(NoSuchObjectException.class, () -> taskService().populateTransientFields(task));
  }

  @Test
  @DisplayName("populateTransientFields throws when candidate bean introspection fails")
  void populateTransientFieldsThrowsWhenBeanIntrospectionFails() {
    TestQuestionTask task = questionTask(1L, "brokenField");

    try (MockedStatic<BeanHelper> beanHelper = Mockito.mockStatic(BeanHelper.class)) {
      beanHelper
          .when(() -> BeanHelper.getPropertyDescriptor(Candidate.class, "brokenField"))
          .thenThrow(new IntrospectionException("boom"));

      assertThrows(NoSuchObjectException.class, () -> taskService().populateTransientFields(task));
    }
  }

  private static TaskImpl task(Long id) {
    TaskImpl task = new TaskImpl();
    task.setId(id);
    task.setName("task-" + id);
    task.setDisplayName("Task " + id);
    return task;
  }

  private static TestQuestionTask questionTask(Long id, String candidateAnswerField) {
    TestQuestionTask task = new TestQuestionTask();
    task.setId(id);
    task.setName("question-" + id);
    task.setDisplayName("Question " + id);
    task.setCandidateAnswerField(candidateAnswerField);
    return task;
  }

  private static UpdateTaskRequest updateRequest(
      String displayName,
      String description,
      Integer daysToComplete,
      String docLink,
      boolean optional,
      boolean notifyOnAssignment
  ) {
    UpdateTaskRequest request = new UpdateTaskRequest();
    request.setDisplayName(displayName);
    request.setDescription(description);
    request.setDaysToComplete(daysToComplete);
    request.setDocLink(docLink);
    request.setOptional(optional);
    request.setNotifyOnAssignment(notifyOnAssignment);
    return request;
  }

  private static void assertAllowedAnswerNames(TestQuestionTask task, String... expectedNames) {
    assertNotNull(task.getAllowedAnswers());
    assertEquals(
        List.of(expectedNames),
        task.getAllowedAnswers().stream()
            .map(AllowedQuestionTaskAnswer::getName)
            .toList()
    );
  }

  private static void assertAllowedAnswerDisplayNames(
      TestQuestionTask task,
      String... expectedDisplayNames
  ) {
    assertNotNull(task.getAllowedAnswers());
    assertEquals(
        List.of(expectedDisplayNames),
        task.getAllowedAnswers().stream()
            .map(AllowedQuestionTaskAnswer::getDisplayName)
            .toList()
    );
  }

  private static class TestQuestionTask extends TaskImpl implements QuestionTask {
    private String candidateAnswerField;
    private List<String> explicitAllowedAnswers;
    private List<AllowedQuestionTaskAnswer> allowedAnswers;

    @Override
    public List<String> getExplicitAllowedAnswers() {
      return explicitAllowedAnswers;
    }

    void setExplicitAllowedAnswers(List<String> explicitAllowedAnswers) {
      this.explicitAllowedAnswers = explicitAllowedAnswers;
    }

    @Override
    public List<AllowedQuestionTaskAnswer> getAllowedAnswers() {
      return allowedAnswers;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setAllowedAnswers(List allowedAnswers) {
      this.allowedAnswers = allowedAnswers;
    }

    @Override
    public String getCandidateAnswerField() {
      return candidateAnswerField;
    }

    void setCandidateAnswerField(String candidateAnswerField) {
      this.candidateAnswerField = candidateAnswerField;
    }
  }
}