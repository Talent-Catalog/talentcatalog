/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.util;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test EnumHelper
 *
 * @author John Cameron
 */
public class TestEnumHelper {
    List<Fred> fredList;
    
    @BeforeEach
    void init() {
        fredList = new ArrayList<>();
        fredList.add(Fred.A);
        fredList.add(Fred.C);
    }
    
    @Test
    void testEnumArray() {
        String s = EnumHelper.toString(fredList);
        assertNotNull(s);
        
        List<Fred> x = EnumHelper.fromString(Fred.class, s);
        assertNotNull(x);
        
    }
    
    enum Fred {
        A,B,C
    }
}
