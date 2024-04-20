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

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.tctalent.server.model.sf.Contact
import org.tctalent.server.utils.BeanHelper
import java.beans.PropertyDescriptor

class BeanHelperTest {
//    @Test
//    fun `getPropertyDescriptor Java returns correct PropertyDescriptor`() {
//        val clazz = Contact::class.java
//        val propertyName = "accountId"
//        val expectedPropertyDescriptor = PropertyDescriptor(propertyName, clazz)
//        val actualPropertyDescriptor = BeanHelper.getPropertyDescriptor(clazz, propertyName)
//        assertEquals(expectedPropertyDescriptor, actualPropertyDescriptor)
//    }
//
//    @Test
//    fun `getPropertyDescriptor Java returns null for non-existent property`() {
//        val clazz = Contact::class.java
//        val propertyName = "nonExistentProperty"
//        val actualPropertyDescriptor = BeanHelper.getPropertyDescriptor(clazz, propertyName)
//        assertEquals(null, actualPropertyDescriptor)
//    }

    @Test
    fun `getPropertyDescriptor Kotlin returns correct PropertyDescriptor`() {
        val clazz = Contact::class.java
        val propertyName = "accountId"
        val expectedPropertyDescriptor = PropertyDescriptor(propertyName, clazz)
        val actualPropertyDescriptor = BeanHelper.getPropertyDescriptor(clazz, propertyName)
        assertEquals(expectedPropertyDescriptor, actualPropertyDescriptor)
    }

    @Test
    fun `getPropertyDescriptor Kotlin returns null for non-existent property`() {
        val clazz = Contact::class.java
        val propertyName = "nonExistentProperty"
        val actualPropertyDescriptor = BeanHelper.getPropertyDescriptor(clazz, propertyName)
        assertEquals(null, actualPropertyDescriptor)
    }
}
