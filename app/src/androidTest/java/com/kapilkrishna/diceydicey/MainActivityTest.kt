package com.kapilkrishna.diceydicey

import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Smoke test: the app launches into DieView (SceneView + the die.v2.glb asset)
 * and stays up. Tapping the die is not automated here — SceneView's native
 * surface handles taps outside Compose, so that path stays a manual check
 * (see ARCHITECTURE.md § Testing Strategy).
 */
@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @Test
    fun launchesAndStaysResumed() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            // Give SceneView time to create its engine and load the model asset.
            Thread.sleep(2_000)
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            assertEquals(Lifecycle.State.RESUMED, scenario.state)
        }
    }
}
