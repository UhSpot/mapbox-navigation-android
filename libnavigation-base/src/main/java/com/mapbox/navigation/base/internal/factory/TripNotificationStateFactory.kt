package com.mapbox.navigation.base.internal.factory

import com.mapbox.api.directions.v5.models.BannerInstructions
import com.mapbox.navigation.base.trip.model.TripNotificationState

object TripNotificationStateFactory {

    fun buildTripNotificationState(
        bannerInstructions: BannerInstructions?,
        distanceRemaining: Double?,
        durationRemaining: Double?,
        drivingSide: String?
    ): TripNotificationState {
        return TripNotificationState(
            bannerInstructions,
            distanceRemaining,
            durationRemaining,
            drivingSide
        )
    }
}
