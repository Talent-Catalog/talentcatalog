/*
 * Copyright (c) 2025 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.service.db.impl;

import java.io.IOException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateProperty;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.model.db.TaskAssignmentImpl;
import org.tctalent.server.model.db.TaskImpl;
import org.tctalent.server.model.db.UploadTaskAssignmentImpl;
import org.tctalent.server.model.db.task.TaskType;
import org.tctalent.server.model.db.task.UploadTask;
import org.tctalent.server.model.db.task.UploadType;
import org.tctalent.server.repository.db.CandidatePropertyRepository;
import org.tctalent.server.response.MetadataFieldResponse;
import org.tctalent.server.service.db.CandidateAttachmentService;
import org.tctalent.server.service.db.TaskProcessor;
import org.tctalent.server.service.db.TaskService;
import org.tctalent.server.util.validator.MetadataValidator;
import org.tctalent.server.util.validator.MetadataValidatorFactory;

@Service
public class UploadTaskProcessor implements TaskProcessor {
  private final CandidateAttachmentService candidateAttachmentService;
  private final CandidatePropertyRepository candidatePropertyRepository;
  private final TaskService taskService;

  @Autowired
  public UploadTaskProcessor(
      CandidateAttachmentService candidateAttachmentService,
      CandidatePropertyRepository candidatePropertyRepository,
      TaskService taskService) {
    this.candidateAttachmentService = candidateAttachmentService;
    this.candidatePropertyRepository = candidatePropertyRepository;
    this.taskService = taskService;
  }

  @Override
  public TaskType getTaskType() {
    return TaskType.Upload;
  }

  @Override
  public TaskAssignmentImpl createTaskAssignment() {
    return new UploadTaskAssignmentImpl();
  }

  @Override
  public TaskAssignmentImpl completeTask(TaskAssignmentImpl assignment, TaskCompletionContext context) {
    if (!(assignment.getTask() instanceof UploadTask)) {
      throw new IllegalArgumentException("Task is not an UploadTask");
    }
    UploadTask uploadTask = (UploadTask) assignment.getTask();
    Candidate candidate = assignment.getCandidate();
    MultipartFile[] files = context.files;
    Map<String, String> fieldAnswers = context.fieldAnswers;
    if (files == null || files.length == 0) {
      throw new IllegalArgumentException("At least one file must be uploaded");
    }

    // Validate metadata
    List<MetadataFieldResponse> requiredMetadata = uploadTask.getRequiredMetadata();
    if (requiredMetadata != null && !requiredMetadata.isEmpty()) {
      MetadataValidator validator = MetadataValidatorFactory.getValidator(uploadTask.getName());
      validator.validate(candidate, fieldAnswers, requiredMetadata);
      saveNonOverlappingMetadata(candidate, uploadTask, fieldAnswers, requiredMetadata, assignment);
    }

    // Upload files
    UploadType uploadType = uploadTask.getUploadType();
    String subFolderName = uploadTask.getUploadSubfolderName();
    for (MultipartFile file : files) {
      String uploadedName = candidate.getCandidateNumber() + "-" + uploadType + "-" + file.getOriginalFilename();
      try {
        candidateAttachmentService.uploadAttachment(candidate, uploadedName, subFolderName, file, uploadType);
      } catch (IOException e) {
        throw new RuntimeException("Failed to upload file: " + file.getOriginalFilename(), e);
      }
    }

    assignment.setCompletedDate(OffsetDateTime.now());
    return assignment;
  }

  @Override
  public void handleCompletion(TaskAssignmentImpl assignment) {
    if ("claimCouponButton".equals(assignment.getTask().getName())) {
      TaskImpl duolingoTestTask = taskService.getByName("duolingoTest");
      int daysToComplete = duolingoTestTask.getDaysToComplete() != null
          ? duolingoTestTask.getDaysToComplete() : 0;
      TaskAssignmentImpl newAssignment = new TaskAssignmentImpl();
      newAssignment.setTask(duolingoTestTask);
      newAssignment.setCandidate(assignment.getCandidate());
      newAssignment.setActivatedBy(assignment.getActivatedBy());
      newAssignment.setActivatedDate(OffsetDateTime.now());
      newAssignment.setStatus(Status.active);
      newAssignment.setDueDate(LocalDate.now().plusDays(daysToComplete));
      taskService.populateTransientFields(duolingoTestTask);
    }
  }

  private void saveNonOverlappingMetadata(
      Candidate candidate, UploadTask uploadTask, Map<String, String> fieldAnswers,
      List<MetadataFieldResponse> requiredMetadata, TaskAssignmentImpl assignment) {
    Set<String> overlappingFields = Set.of("firstName", "lastName", "dateOfBirth", "gender", "countryOfBirth");
    for (MetadataFieldResponse field : requiredMetadata) {
      String fieldName = field.getName();
      if (!overlappingFields.contains(fieldName)) {
        String value = fieldAnswers.get(fieldName);
        if (value != null && !value.trim().isEmpty()) {
          CandidateProperty prop = new CandidateProperty();
          prop.setCandidateId(candidate.getId());
          prop.setName(uploadTask.getName() + "_ta" + assignment.getId() + "_" + fieldName);
          prop.setValue(value);
          prop.setRelatedTaskAssignment(assignment);
          candidatePropertyRepository.save(prop);
        }
      }
    }
  }
}