 /*
 * Copyright (c) 2024 Talent Beyond Boundaries.
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

 import java.util.Map;
 import javax.validation.Valid;
 import javax.validation.constraints.NotNull;
 import lombok.RequiredArgsConstructor;
 import org.springframework.web.bind.annotation.PostMapping;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RestController;
 import org.tctalent.server.exception.EntityReferencedException;
 import org.tctalent.server.exception.InvalidRequestException;
 import org.tctalent.server.exception.NoSuchObjectException;
 import org.tctalent.server.model.db.LinkPreview;
 import org.tctalent.server.request.chat.link_preview.CreateLinkPreviewRequest;
 import org.tctalent.server.service.db.LinkPreviewService;
 import org.tctalent.server.util.dto.DtoBuilder;

 @RestController()
 @RequestMapping("/api/admin/link-preview")
 @RequiredArgsConstructor
 public class LinkPreviewAdminApi
     implements IJoinedTableApi<CreateLinkPreviewRequest, CreateLinkPreviewRequest,
     CreateLinkPreviewRequest> {

     private final LinkPreviewService linkPreviewService;

     /**
      * TODO doc
      */
     @PostMapping("/build-link-preview")
     public @NotNull Map<String, Object> buildLinkPreview(@NotNull String url) {
       LinkPreview linkPreview = this.linkPreviewService.buildLinkPreview(url);
       return this.linkPreviewDto().build(linkPreview);
     }

   /**
    * TODO doc
    * @param chatPostId ID of parent record
    * @param request Request containing details from which the record is created.
    * @return
    * @throws NoSuchObjectException
    */
     @Override
     public @NotNull Map<String, Object> create(
         long chatPostId, @Valid CreateLinkPreviewRequest request
     ) throws NoSuchObjectException {
       LinkPreview linkPreview = linkPreviewService.createLinkPreview(chatPostId, request);
       return linkPreviewDto().build(linkPreview);
     }

     @Override
     public boolean delete(long id) throws EntityReferencedException, InvalidRequestException {
       return linkPreviewService.deleteLinkPreview(id);
     }

     private DtoBuilder linkPreviewDto() {
         return new DtoBuilder()
                 .add("id")
                 .add("url")
                 .add("title")
                 .add("description")
                 .add("imageUrl")
                 ;
     }

 }
