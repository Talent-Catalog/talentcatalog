 /*
 * Copyright (c) 2024 Talent Catalog.
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

package org.tctalent.server.api.admin;

 import java.io.IOException;
 import java.util.Map;
 import jakarta.validation.Valid;
 import lombok.RequiredArgsConstructor;
 import org.springframework.lang.Nullable;
 import org.springframework.web.bind.annotation.PostMapping;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RestController;
 import org.tctalent.server.exception.EntityReferencedException;
 import org.tctalent.server.exception.InvalidRequestException;
 import org.tctalent.server.model.db.LinkPreview;
 import org.tctalent.server.request.chat.link_preview.BuildLinkPreviewRequest;
 import org.tctalent.server.service.db.LinkPreviewService;
 import org.tctalent.server.util.dto.DtoBuilder;

 @RestController()
 @RequestMapping("/api/admin/link-preview")
 @RequiredArgsConstructor
 public class LinkPreviewAdminApi
     implements IJoinedTableApi<BuildLinkPreviewRequest, BuildLinkPreviewRequest,
     BuildLinkPreviewRequest> {

     private final LinkPreviewService linkPreviewService;

   /**
    * @param id ID of record to be deleted
    * @return boolean confirming successful deletion
    * @throws EntityReferencedException if the object cannot be deleted because
    * it is referenced by another object.
    * @throws InvalidRequestException if not authorized to delete this object.
    */
     @Override
     public boolean delete(long id) throws EntityReferencedException, InvalidRequestException {
       return linkPreviewService.deleteLinkPreview(id);
     }

   /**
    * Receives a URL in String form and returns a LinkPreview for display in Job Chats if successful.
    * @param request URL String of link to be previewed
    * @return Map of LinkPreview or null if unsuccessful
    */
   @PostMapping("/build-link-preview")
   public @Nullable Map<String, Object> buildLinkPreview(
       @Valid @RequestBody BuildLinkPreviewRequest request) throws IOException {
     LinkPreview linkPreview = linkPreviewService.buildLinkPreview(request.getUrl());
     return this.linkPreviewDto().build(linkPreview);
   }

     private DtoBuilder linkPreviewDto() {
         return new DtoBuilder()
                 .add("id")
                 .add("url")
                 .add("title")
                 .add("description")
                 .add("imageUrl")
                 .add("domain")
                 .add("faviconUrl")
                 ;
     }

 }
