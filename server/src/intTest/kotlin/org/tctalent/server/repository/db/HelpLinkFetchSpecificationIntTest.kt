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

package org.tctalent.server.repository.db

import kotlin.test.*
import org.springframework.beans.factory.annotation.Autowired
import org.tctalent.server.model.db.*
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest
import org.tctalent.server.repository.db.integrationhelp.getHelpLink
import org.tctalent.server.repository.db.integrationhelp.getSavedCountry
import org.tctalent.server.request.helplink.SearchHelpLinkRequest

class HelpLinkFetchSpecificationIntTest : BaseDBIntegrationTest() {
  @Autowired lateinit var repo: HelpLinkRepository
  @Autowired lateinit var countryRepo: CountryRepository
  lateinit var helpLink: HelpLink

  @BeforeTest
  fun setup() {
    assertTrue { isContainerInitialized() }
    helpLink = getHelpLink().apply { country = getSavedCountry(countryRepo) }
    repo.save(helpLink)
    assertTrue { helpLink.id > 0 }
  }

  @Test
  fun `test add country`() {
    repo.save(helpLink.apply { country = getSavedCountry(countryRepo) })

    val request = SearchHelpLinkRequest().apply { countryId = helpLink.country?.id ?: fail() }
    val spec = HelpLinkFetchSpecification.buildSearchQuery(request)
    val result = repo.findAll()
    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
    assertEquals(1, result.size)
    assertEquals(helpLink.id, result.first().id)
  }

  @Test
  fun `test add job stage`() {
    repo.save(helpLink.apply { jobStage = JobOpportunityStage.jobOffer })

    val request = SearchHelpLinkRequest().apply { jobStage = helpLink.jobStage }
    val spec = HelpLinkFetchSpecification.buildSearchQuery(request)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
    assertEquals(1, result.size)
    assertEquals(helpLink.id, result.first().id)
  }

  @Test
  fun `test add job stage fail`() {
    repo.save(helpLink.apply { jobStage = JobOpportunityStage.jobOffer })

    val request = SearchHelpLinkRequest().apply { jobStage = JobOpportunityStage.noJobOffer }
    val spec = HelpLinkFetchSpecification.buildSearchQuery(request)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue { result.isEmpty() }
  }

  @Test
  fun `test add case stage`() {
    repo.save(helpLink.apply { caseStage = CandidateOpportunityStage.testing })

    val request = SearchHelpLinkRequest().apply { caseStage = helpLink.caseStage }
    val spec = HelpLinkFetchSpecification.buildSearchQuery(request)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
    assertEquals(1, result.size)
    assertEquals(helpLink.id, result.first().id)
  }

  @Test
  fun `test add case stage fail`() {
    repo.save(helpLink.apply { caseStage = CandidateOpportunityStage.testing })

    val request =
      SearchHelpLinkRequest().apply { caseStage = CandidateOpportunityStage.cvPreparation }
    val spec = HelpLinkFetchSpecification.buildSearchQuery(request)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue { result.isEmpty() }
  }

  @Test
  fun `test add focus`() {
    repo.save(helpLink.apply { focus = HelpFocus.updateStage })

    val request = SearchHelpLinkRequest().apply { focus = helpLink.focus }
    val spec = HelpLinkFetchSpecification.buildSearchQuery(request)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
    assertEquals(1, result.size)
    assertEquals(helpLink.id, result.first().id)
  }

  @Test
  fun `test add focus fail`() {
    repo.save(helpLink.apply { focus = HelpFocus.updateStage })

    val request = SearchHelpLinkRequest().apply { focus = HelpFocus.updateNextStep }
    val spec = HelpLinkFetchSpecification.buildSearchQuery(request)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue { result.isEmpty() }
  }

  @Test
  fun `test add next step name`() {
    repo.save(helpLink.apply { nextStepInfo = NextStepInfo().apply { nextStepName = "STOP" } })

    val request =
      SearchHelpLinkRequest().apply { nextStepName = helpLink.nextStepInfo?.nextStepName }
    val spec = HelpLinkFetchSpecification.buildSearchQuery(request)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue { result.isNotEmpty() }
    assertEquals(1, result.size)
    assertEquals(helpLink.id, result.first().id)
  }

  @Test
  fun `test add next step name fail`() {
    repo.save(helpLink.apply { nextStepInfo = NextStepInfo().apply { nextStepName = "STOP" } })

    val request = SearchHelpLinkRequest().apply { nextStepName = "GO" }
    val spec = HelpLinkFetchSpecification.buildSearchQuery(request)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue { result.isEmpty() }
  }
}
