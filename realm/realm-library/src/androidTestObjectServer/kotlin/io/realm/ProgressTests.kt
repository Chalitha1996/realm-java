/*
 * Copyright 2017 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.realm

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@Ignore("FIXME: RealmApp refactor")
@RunWith(AndroidJUnit4::class)
class ProgressTests {
    @get:Test
    val fractionTransferred: Unit
        get() {
            val testData = arrayOf(arrayOf<Any>(0L, 0L, 1.0), arrayOf<Any>(0L, 1L, 0.0), arrayOf<Any>(1L, 1L, 1.0), arrayOf<Any>(1L, 2L, 0.5))
            for (test in testData) {
                val transferredBytes = test[0] as Long
                val transferableBytes = test[1] as Long
                val fraction = test[2] as Double
                val progress = Progress(transferredBytes, transferableBytes)
                val errorMessage = String.format(Locale.US, "Failed with: (%d, %d)", transferredBytes, transferableBytes)
                Assert.assertEquals(errorMessage, fraction, progress.fractionTransferred, 0.0)
            }
        }

    @get:Test
    val transferredBytes: Unit
        get() {
            val testData = longArrayOf(0, Long.MAX_VALUE)
            for (transferredBytes in testData) {
                val errorMessage = String.format(Locale.US, "Failed with: %d", transferredBytes)
                val progress = Progress(transferredBytes, Long.MAX_VALUE)
                Assert.assertEquals(errorMessage, transferredBytes, progress.transferredBytes)
            }
        }

    @get:Test
    val transferableBytes: Unit
        get() {
            val testData = longArrayOf(0, Long.MAX_VALUE)
            for (transferableBytes in testData) {
                val errorMessage = String.format(Locale.US, "Failed with: %d", transferableBytes)
                val progress = Progress(0, transferableBytes)
                Assert.assertEquals(errorMessage, transferableBytes, progress.transferableBytes)
            }
        }
}
