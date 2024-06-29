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

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import javax.persistence.criteria.*
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import org.springframework.data.domain.Sort
import org.tctalent.server.model.db.Candidate
import org.tctalent.server.repository.db.CandidateSpecificationUtil.getOrderByOrders
import org.tctalent.server.request.PagedSearchRequest

class CandidateSpecificationUtilIntTest {
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
    request = mockk()
    candidate = mockk()
    builder = mockk()
    user = mockk()
    partner = mockk()
    nationality = mockk()
    country = mockk()
    educationLevel = mockk()
  }

  @Test
  fun `sorts by user partner in ascending order`() {
    val pathAsc = mockk<Path<String>>()
    val pathId = mockk<Path<String>>()
    val ascOrder = mockk<Order>()
    val descOrder = mockk<Order>()

    every { request.sortFields } returns arrayOf("user.partner.name")
    every { request.sortDirection } returns Sort.Direction.ASC
    every { partner.get<String>("name") } returns pathAsc
    every { builder.asc(pathAsc) } returns ascOrder
    every { candidate.get<String>("id") } returns pathId
    every { builder.asc(any()) } answers { ascOrder }
    every { builder.desc(any()) } answers { descOrder }

    val orders =
      getOrderByOrders(
        request,
        candidate,
        builder,
        user,
        partner,
        nationality,
        country,
        educationLevel,
      )

    verify {
      builder.asc(any())
      partner.get<String>("name")
      request.sortFields
      request.sortDirection
      candidate.get<String>("id")
    }
    assertEquals(2, orders.size)
  }

  @Test
  fun `sorts by user email order desc`() {
    val pathAsc = mockk<Path<String>>()
    val pathId = mockk<Path<String>>()
    val ascOrder = mockk<Order>()
    val descOrder = mockk<Order>()

    every { request.sortFields } returns arrayOf("user.email")
    every { request.sortDirection } returns Sort.Direction.DESC
    every { user.get<String>("email") } returns pathAsc
    every { builder.asc(pathAsc) } returns ascOrder
    every { candidate.get<String>("id") } returns pathId
    every { builder.asc(any()) } answers { ascOrder }
    every { builder.desc(any()) } answers { descOrder }

    val orders =
      getOrderByOrders(
        request,
        candidate,
        builder,
        user,
        partner,
        nationality,
        country,
        educationLevel,
      )

    verify {
      user.get<String>("email")
      request.sortFields
      request.sortDirection
      candidate.get<String>("id")
      builder.desc(any())
    }
    assertEquals(2, orders.size)
  }

  @Test
  fun `sorts by user in ascending order`() {
    val pathAsc = mockk<Path<String>>()
    val pathId = mockk<Path<String>>()
    val ascOrder = mockk<Order>()
    val descOrder = mockk<Order>()

    every { request.sortFields } returns arrayOf("user.name")
    every { request.sortDirection } returns Sort.Direction.ASC
    every { user.get<String>("name") } returns pathAsc
    every { builder.asc(pathAsc) } returns ascOrder
    every { candidate.get<String>("id") } returns pathId
    every { builder.asc(any()) } answers { ascOrder }
    every { builder.desc(any()) } answers { descOrder }

    val orders =
      getOrderByOrders(
        request,
        candidate,
        builder,
        user,
        partner,
        nationality,
        country,
        educationLevel,
      )

    verify {
      builder.asc(any())
      user.get<String>("name")
      request.sortFields
      request.sortDirection
      candidate.get<String>("id")
    }
    assertEquals(2, orders.size)
  }

  @Test
  fun `sorts by user nationality property in ascending order`() {
    val pathAsc = mockk<Path<String>>()
    val pathId = mockk<Path<String>>()
    val ascOrder = mockk<Order>()
    val descOrder = mockk<Order>()

    every { request.sortFields } returns arrayOf("nationality.name")
    every { request.sortDirection } returns Sort.Direction.ASC
    every { nationality.get<String>("name") } returns pathAsc
    every { builder.asc(pathAsc) } returns ascOrder
    every { candidate.get<String>("id") } returns pathId
    every { builder.asc(any()) } answers { ascOrder }
    every { builder.desc(any()) } answers { descOrder }

    val orders =
      getOrderByOrders(
        request,
        candidate,
        builder,
        user,
        partner,
        nationality,
        country,
        educationLevel,
      )

    verify {
      builder.asc(any())
      nationality.get<String>("name")
      request.sortFields
      request.sortDirection
      candidate.get<String>("id")
    }
    assertEquals(2, orders.size)
  }

  @Test
  fun `sorts by country in ascending order`() {
    val pathAsc = mockk<Path<String>>()
    val pathId = mockk<Path<String>>()
    val ascOrder = mockk<Order>()
    val descOrder = mockk<Order>()

    every { request.sortFields } returns arrayOf("country.name")
    every { request.sortDirection } returns Sort.Direction.ASC
    every { country.get<String>("name") } returns pathAsc
    every { builder.asc(pathAsc) } returns ascOrder
    every { candidate.get<String>("id") } returns pathId
    every { builder.asc(any()) } answers { ascOrder }
    every { builder.desc(any()) } answers { descOrder }

    val orders =
      getOrderByOrders(
        request,
        candidate,
        builder,
        user,
        partner,
        nationality,
        country,
        educationLevel,
      )

    verify {
      builder.asc(any())
      country.get<String>("name")
      request.sortFields
      request.sortDirection
      candidate.get<String>("id")
    }
    assertEquals(2, orders.size)
  }

  @Test
  fun `sorts by user education level in ascending order`() {
    val pathAsc = mockk<Path<String>>()
    val pathId = mockk<Path<String>>()
    val ascOrder = mockk<Order>()
    val descOrder = mockk<Order>()

    every { request.sortFields } returns arrayOf("maxEducationLevel.name")
    every { request.sortDirection } returns Sort.Direction.ASC
    every { educationLevel.get<String>("name") } returns pathAsc
    every { candidate.get<String>("id") } returns pathId
    every { builder.asc(any()) } answers { ascOrder }
    every { builder.desc(any()) } answers { descOrder }

    val orders =
      getOrderByOrders(
        request,
        candidate,
        builder,
        user,
        partner,
        nationality,
        country,
        educationLevel,
      )

    verify {
      builder.asc(any())
      educationLevel.get<String>("name")
      request.sortFields
      request.sortDirection
      candidate.get<String>("id")
    }
    assertEquals(2, orders.size)
  }

  @Test
  fun `sorts by user id in ascending order`() {
    val pathId = mockk<Path<String>>()
    val ascOrder = mockk<Order>()

    every { request.sortFields } returns arrayOf("id")
    every { request.sortDirection } returns Sort.Direction.ASC

    every { candidate.get<String>("id") } returns pathId
    every { builder.asc(any()) } answers { ascOrder }

    val orders =
      getOrderByOrders(
        request,
        candidate,
        builder,
        user,
        partner,
        nationality,
        country,
        educationLevel,
      )

    verify {
      builder.asc(any())
      request.sortFields
      request.sortDirection
      candidate.get<String>("id")
    }
    assertEquals(1, orders.size)
  }
}
