package com.hexocraft.lib.utilities.comparator;

/*

 Copyright 2018 hexosse

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 */
import com.hexocraft.lib.utilities.comparator.NumberAwareStringComparator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class NumberAwareStringComparatorTest {

    public NumberAwareStringComparator comparator = NumberAwareStringComparator.INSTANCE;

    @Test
    void compare() {

        assertEquals(0, comparator.compare("", ""));
        assertEquals(0, comparator.compare("test1", "test1"));
        assertEquals(0, comparator.compare("test.1", "test.1"));
        assertEquals(0, comparator.compare("test01", "test1"));
        assertEquals(0, comparator.compare("test1", "test01"));

        assertTrue(comparator.compare("", "a") < 0);
        assertTrue(comparator.compare("0", "a") < 0);
        assertTrue(comparator.compare("a", "b") < 0);
        assertTrue(comparator.compare("1", "2") < 0);
        assertTrue(comparator.compare("test", "test1") < 0);
        assertTrue(comparator.compare("test1", "test2") < 0);
        assertTrue(comparator.compare("test1-1", "test2-1") < 0);
        assertTrue(comparator.compare("test01-01", "test02-01") < 0);
        assertTrue(comparator.compare("test1", "test10") < 0);
        assertTrue(comparator.compare("test2", "test10") < 0);

        assertTrue(comparator.compare("a", "0") > 0);
        assertTrue(comparator.compare("b", "a") > 0);
        assertTrue(comparator.compare("2", "1") > 0);
        assertTrue(comparator.compare("test1", "test") > 0);
        assertTrue(comparator.compare("test2", "test1") > 0);
        assertTrue(comparator.compare("test2-1", "test1-1") > 0);
        assertTrue(comparator.compare("test02-01", "test01-01") > 0);
        assertTrue(comparator.compare("test10", "test1") > 0);
        assertTrue(comparator.compare("test10", "test2") > 0);
    }
}
