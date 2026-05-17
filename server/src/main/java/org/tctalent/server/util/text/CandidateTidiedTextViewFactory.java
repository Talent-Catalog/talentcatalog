/*
 * Copyright (c) 2026 Talent Catalog.
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

package org.tctalent.server.util.text;

import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.stereotype.Component;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateJobExperience;

/**
 * This creates proxies for a Candidate and relevant related classes.
 * The proxies intercept methods that get text fields used in generating candidate CV's,
 * replacing them to return {@link TextParts} tidied text if present.
 * <p>
 * The purpose of this is to allow a candidate's CV to be generated using tidied text where it
 * exists.
 *
 * @author John Cameron
 */
@Component
@RequiredArgsConstructor
public class CandidateTidiedTextViewFactory {

    private final TextPartsTidiedTextService textPartsTidiedTextService;

    public Candidate create(Candidate candidate) {

        ProxyFactory factory = new ProxyFactory(candidate);
        factory.setProxyTargetClass(true);

        //Replace job experiences with a
        factory.addAdvice((MethodInterceptor) invocation -> {

            if (invocation.getMethod().getName().equals("getCandidateJobExperiences")
                && invocation.getArguments().length == 0) {

                Collection<CandidateJobExperience> experiences =
                    (Collection<CandidateJobExperience>) invocation.proceed();

                if (experiences == null) {
                    return null;
                } else {
                    return experiences.stream()
                        .map(this::wrapExperience)
                        .toList();
                }
            }

            return invocation.proceed();
        });

        return (Candidate) factory.getProxy();
    }

    private CandidateJobExperience wrapExperience(
        CandidateJobExperience experience) {

        ProxyFactory factory = new ProxyFactory(experience);
        factory.setProxyTargetClass(true);

        factory.addAdvice((MethodInterceptor) invocation -> {

            if (invocation.getMethod().getName().equals("getDescription")
                && invocation.getArguments().length == 0) {

                String encoded = (String) invocation.proceed();

                return textPartsTidiedTextService
                    .getTidiedText(encoded);
            }

            return invocation.proceed();
        });

        return (CandidateJobExperience) factory.getProxy();
    }
}
