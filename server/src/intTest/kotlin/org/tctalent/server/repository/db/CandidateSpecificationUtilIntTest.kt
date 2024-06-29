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

import org.mockito.Mockito.mock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.JpaSort
import org.tctalent.server.model.db.Candidate
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest
import org.tctalent.server.request.PagedSearchRequest
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.Join
import javax.persistence.criteria.Root
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CandidateSpecificationUtilIntTest : BaseDBIntegrationTest() {
  @Autowired private lateinit var repo: CandidateRepository
  @Autowired private lateinit var userRepository: UserRepository
  private lateinit var candidate: Root<Candidate>
  private lateinit var request: PagedSearchRequest
  private lateinit var builder: CriteriaBuilder
  private lateinit var user: Join<Any, Any>
  private lateinit var partner: Join<Any, Any>
  private lateinit var nationality: Join<Any, Any>
  private lateinit var country: Join<Any, Any>
  private lateinit var educationLevel: Join<Any, Any>

  @BeforeTest
  fun setup() {
    assertTrue { isContainerInitialized() }
    request = PagedSearchRequest()
    candidate = mock(Root::class.java) as Root<Candidate>
    builder = mock(CriteriaBuilder::class.java)
    user = mock(Join::class.java) as Join<Any, Any>
    partner = mock(Join::class.java) as Join<Any, Any>
    nationality = mock(Join::class.java) as Join<Any, Any>
    country = mock(Join::class.java) as Join<Any, Any>
    educationLevel = mock(Join::class.java) as Join<Any, Any>
  }

  @Test
  fun testGetOrderByOrders_withUserPartnerSort() {
    request.sortFields = arrayOf<String>("user.partner.name")
    request.sortDirection = Sort.Direction.ASC
    val path: JpaSort.Path<Any> = mock(Path::class.java)

    //        when(partner.get("name")).thenReturn(path)
    //        `when`(builder.asc(path)).thenReturn(mock(Order::class.java))

    val orders =
      CandidateSpecificationUtil.getOrderByOrders(
        request,
        candidate,
        builder,
        user,
        partner,
        nationality,
        country,
        educationLevel,
      )

    //    verify(partner).get("name")
    //    verify(builder).asc(path)
    assertEquals(2, orders.size) // Includes the default sort by id desc
  }
  //
  //  @Test
  //  fun testGetOrderByOrders_withUserSort() {
  //    request.setSortFields(arrayOf<String>("user.email"))
  //    request.setSortDirection(Sort.Direction.DESC)
  //
  //    val path: Path<Any> = mock(Path::class.java)
  //    `when`(user.get("email")).thenReturn(path)
  //    `when`(builder.desc(path)).thenReturn(mock(Order::class.java))
  //
  //    val orders: List<Order> =
  //      CandidateSpecificationUtil.getOrderByOrders(
  //        request,
  //        candidate,
  //        builder,
  //        user,
  //        partner,
  //        nationality,
  //        country,
  //        educationLevel,
  //      )
  //
  //    verify(user).get("email")
  //    verify(builder).desc(path)
  //    assertEquals(2, orders.size) // Includes the default sort by id desc
  //  }
  //
  //  @Test
  //  fun testGetOrderByOrders_withNationalitySort() {
  //    request.setSortFields(arrayOf<String>("nationality.name"))
  //    request.setSortDirection(Sort.Direction.ASC)
  //
  //    val path: Path<Any> = mock(Path::class.java)
  //    `when`(nationality.get("name")).thenReturn(path)
  //    `when`(builder.asc(path)).thenReturn(mock(Order::class.java))
  //
  //    val orders: List<Order> =
  //      CandidateSpecificationUtil.getOrderByOrders(
  //        request,
  //        candidate,
  //        builder,
  //        user,
  //        partner,
  //        nationality,
  //        country,
  //        educationLevel,
  //      )
  //
  //    verify(nationality).get("name")
  //    verify(builder).asc(path)
  //    assertEquals(2, orders.size) // Includes the default sort by id desc
  //  }
  //
  //  @Test
  //  fun testGetOrderByOrders_withCountrySort() {
  //    request.setSortFields(arrayOf<String>("country.name"))
  //    request.setSortDirection(Sort.Direction.DESC)
  //
  //    val path: Path<Any> = mock(Path::class.java)
  //    `when`(country.get("name")).thenReturn(path)
  //    `when`(builder.desc(path)).thenReturn(mock(Order::class.java))
  //
  //    val orders: List<Order> =
  //      CandidateSpecificationUtil.getOrderByOrders(
  //        request,
  //        candidate,
  //        builder,
  //        user,
  //        partner,
  //        nationality,
  //        country,
  //        educationLevel,
  //      )
  //
  //    verify(country).get("name")
  //    verify(builder).desc(path)
  //    assertEquals(2, orders.size) // Includes the default sort by id desc
  //  }
  //
  //  @Test
  //  fun testGetOrderByOrders_withEducationLevelSort() {
  //    request.setSortFields(arrayOf<String>("maxEducationLevel.level"))
  //    request.setSortDirection(Sort.Direction.ASC)
  //
  //    val path: Path<Any> = mock(Path::class.java)
  //    `when`(educationLevel.get("level")).thenReturn(path)
  //    `when`(builder.asc(path)).thenReturn(mock(Order::class.java))
  //
  //    val orders: List<Order> =
  //      CandidateSpecificationUtil.getOrderByOrders(
  //        request,
  //        candidate,
  //        builder,
  //        user,
  //        partner,
  //        nationality,
  //        country,
  //        educationLevel,
  //      )
  //
  //    verify(educationLevel).get("level")
  //    verify(builder).asc(path)
  //    assertEquals(2, orders.size) // Includes the default sort by id desc
  //  }
  //
  //  @Test
  //  fun testGetOrderByOrders_withIdSort() {
  //    request.setSortFields(arrayOf<String>("id"))
  //    request.setSortDirection(Sort.Direction.ASC)
  //
  //    val path: Path<Any> = mock(Path::class.java)
  //    `when`(candidate.get("id")).thenReturn(path)
  //    `when`(builder.asc(path)).thenReturn(mock(Order::class.java))
  //
  //    val orders: List<Order> =
  //      CandidateSpecificationUtil.getOrderByOrders(
  //        request,
  //        candidate,
  //        builder,
  //        user,
  //        partner,
  //        nationality,
  //        country,
  //        educationLevel,
  //      )
  //
  //    verify(candidate).get("id")
  //    verify(builder).asc(path)
  //    assertEquals(1, orders.size) // No additional id sort required
  //  }
  //
  //  @Test
  //  fun testGetOrderByOrders_withoutIdSort() {
  //    request.setSortFields(arrayOf<String>("user.email"))
  //    request.setSortDirection(Sort.Direction.ASC)
  //
  //    val emailPath: Path<Any> = mock(Path::class.java)
  //    `when`(user.get("email")).thenReturn(emailPath)
  //    `when`(builder.asc(emailPath)).thenReturn(mock(Order::class.java))
  //
  //    val idPath: Path<Any> = mock(Path::class.java)
  //    `when`(candidate.get("id")).thenReturn(idPath)
  //    `when`(builder.desc(idPath)).thenReturn(mock(Order::class.java))
  //
  //    val orders: List<Order> =
  //      CandidateSpecificationUtil.getOrderByOrders(
  //        request,
  //        candidate,
  //        builder,
  //        user,
  //        partner,
  //        nationality,
  //        country,
  //        educationLevel,
  //      )
  //
  //    verify(user).get("email")
  //    verify(builder).asc(emailPath)
  //    verify(candidate).get("id")
  //    verify(builder).desc(idPath)
  //    assertEquals(2, orders.size) // Includes the default sort by id desc
  //  }
}
