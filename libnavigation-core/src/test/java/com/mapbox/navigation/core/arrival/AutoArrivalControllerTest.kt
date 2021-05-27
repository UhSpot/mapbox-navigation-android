package com.mapbox.navigation.core.arrival

import android.os.SystemClock
import com.mapbox.api.directions.v5.models.RouteLeg
import com.mapbox.navigation.base.trip.model.RouteLegProgress
import com.mapbox.navigation.core.arrival.AutoArrivalController.Companion.AUTO_ARRIVAL_SECONDS
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkObject
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

class AutoArrivalControllerTest {

    private val arrivalController = AutoArrivalController()

    @Before
    fun setup() {
        mockkStatic(SystemClock::class)
    }

    @After
    fun teardown() {
        unmockkObject(SystemClock.elapsedRealtimeNanos())
    }

    @Test
    fun `should navigate next at predicted arrival`() {
        val routeLeg = mockk<RouteLeg>()

        mockSecond(100)
        assertFalse(arrivalController.navigateNextRouteLeg(mockProgress(routeLeg)))
        mockSecond(100 + AUTO_ARRIVAL_SECONDS - 1)
        assertFalse(arrivalController.navigateNextRouteLeg(mockProgress(routeLeg)))
        mockSecond(100 + AUTO_ARRIVAL_SECONDS)
        assertTrue(arrivalController.navigateNextRouteLeg(mockProgress(routeLeg)))
    }

    @Test
    fun `should restart timer if rerouted`() {
        val routeLeg = mockk<RouteLeg>()

        mockSecond(100)
        assertFalse(arrivalController.navigateNextRouteLeg(mockProgress(routeLeg)))
        mockSecond(100L + AutoArrivalController.AUTO_ARRIVAL_SECONDS)
        val reroutedRouteLeg = mockk<RouteLeg>()
        assertFalse(arrivalController.navigateNextRouteLeg(mockProgress(reroutedRouteLeg)))
        mockSecond(100 + AUTO_ARRIVAL_SECONDS + AUTO_ARRIVAL_SECONDS - 1)
        assertFalse(arrivalController.navigateNextRouteLeg(mockProgress(reroutedRouteLeg)))

        mockSecond(100 + AUTO_ARRIVAL_SECONDS + AUTO_ARRIVAL_SECONDS)
        assertTrue(arrivalController.navigateNextRouteLeg(mockProgress(reroutedRouteLeg)))
    }

    private fun mockSecond(second: Long) = every {
        SystemClock.elapsedRealtimeNanos()
    } returns TimeUnit.SECONDS.toNanos(second)

    private fun mockProgress(mockedRouteLeg: RouteLeg) = mockk<RouteLegProgress> {
        every { routeLeg } returns mockedRouteLeg
    }
}
