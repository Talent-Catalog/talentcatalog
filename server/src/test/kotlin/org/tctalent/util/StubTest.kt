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

package org.tctalent.util

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class StubTest {
    @Test
    fun testPrintJavaUpdate21() {
        val expect = "I expect this string with Java 21 in it."
        val twentyOne = 21
        val strToTest = "I expect this string with Java $twentyOne in it."
        assertEquals(expect, strToTest)
    }
}