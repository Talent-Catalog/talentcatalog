package org.tbbtalent.server.request.search;

import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UpdateSharingRequest {
    @NotNull
    private Long userId;
}
