package org.tbbtalent.server.request.country;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.tbbtalent.server.model.Status;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UpdateCountryRequest {
    @NotBlank
    private String name;
    @NotNull
    private Status status;
}
