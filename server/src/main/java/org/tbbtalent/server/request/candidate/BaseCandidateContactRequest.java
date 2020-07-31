package org.tbbtalent.server.request.candidate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaseCandidateContactRequest {
    private Long id;
    private String email;
    private String phone;
    private String whatsapp;
}
