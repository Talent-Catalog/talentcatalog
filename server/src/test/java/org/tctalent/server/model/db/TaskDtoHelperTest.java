/*
 * Copyright (c) 2024 Talent Catalog.
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

package org.tctalent.server.model.db;

import static org.junit.jupiter.api.Assertions.*;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.apache.commons.beanutils.PropertyUtils;
import org.junit.jupiter.api.Test;
import org.tctalent.server.model.db.task.TaskType;

/**
 * Test which fails when using default interface implementation
 *
 * @author John Cameron
 */
class TaskDtoHelperTest {

    @Test
    void testPropertyUtilsOnDefaultProperty()
        throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, IntrospectionException {
        UploadTaskImpl ut = new UploadTaskImpl();

        TaskType tt = ut.getTaskType();

        assertEquals(TaskType.Upload, tt);

        Class<UploadTaskImpl> clazz = UploadTaskImpl.class;
        final Method[] methods = clazz.getMethods();

        final BeanInfo beanInfo = Introspector.getBeanInfo(clazz);

        final Method m1 = clazz.getMethod("getTaskType");

        //This test fails when getTaskType is only done by the default implementation in the
        //UploadTask interface
        final Object value = PropertyUtils.getProperty(ut, "taskType");
        assertEquals(TaskType.Upload, value);
    }
}
