/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.index.engine;

import org.apache.lucene.util.BytesRefBuilder;
import org.apache.lucene.util.RamUsageTester;
import org.apache.lucene.util.TestUtil;
import org.elasticsearch.bootstrap.JavaVersion;
import org.elasticsearch.test.ESTestCase;

public class LiveVersionMapTests extends ESTestCase {

    public void testRamBytesUsed() throws Exception {
        assumeTrue("Test disabled for JDK 9", JavaVersion.current().compareTo(JavaVersion.parse("9")) < 0);
        LiveVersionMap map = new LiveVersionMap();
        for (int i = 0; i < 100000; ++i) {
            BytesRefBuilder uid = new BytesRefBuilder();
            uid.copyChars(TestUtil.randomSimpleString(random(), 10, 20));
            VersionValue version = new VersionValue(randomLong(), randomLong(), randomLong());
            map.putUnderLock(uid.toBytesRef(), version);
        }
        long actualRamBytesUsed = RamUsageTester.sizeOf(map);
        long estimatedRamBytesUsed = map.ramBytesUsed();
        // less than 50% off
        assertEquals(actualRamBytesUsed, estimatedRamBytesUsed, actualRamBytesUsed / 2);

        // now refresh
        map.beforeRefresh();
        map.afterRefresh(true);

        for (int i = 0; i < 100000; ++i) {
            BytesRefBuilder uid = new BytesRefBuilder();
            uid.copyChars(TestUtil.randomSimpleString(random(), 10, 20));
            VersionValue version = new VersionValue(randomLong(), randomLong(), randomLong());
            map.putUnderLock(uid.toBytesRef(), version);
        }
        actualRamBytesUsed = RamUsageTester.sizeOf(map);
        estimatedRamBytesUsed = map.ramBytesUsed();
        // less than 25% off
        assertEquals(actualRamBytesUsed, estimatedRamBytesUsed, actualRamBytesUsed / 4);
    }

}
