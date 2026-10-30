@file:Suppress("MagicNumber")

package com.hitchy.feature.debug.ui.gallery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.hitchy.core.model.GeoPoint
import com.hitchy.core.ui.map.HitchyMap
import com.hitchy.core.ui.map.MapDefaults
import com.hitchy.core.ui.map.MapPin
import com.hitchy.core.ui.theme.LocalHitchyColors
import com.hitchy.core.ui.theme.LocalHitchySpacing
import com.hitchy.core.ui.theme.LocalHitchyTypography

// Sample points near the default centre, for this gallery only. They are not pickup points.
private val SamplePoints = listOf(
    GeoPoint(lat = MapDefaults.center.lat, lng = MapDefaults.center.lng),
    GeoPoint(lat = MapDefaults.center.lat + 0.012, lng = MapDefaults.center.lng + 0.015),
    GeoPoint(lat = MapDefaults.center.lat + 0.030, lng = MapDefaults.center.lng + 0.020),
)

@Composable
fun GalleryMapSection(modifier: Modifier = Modifier) {
    val colors = LocalHitchyColors.current
    val typography = LocalHitchyTypography.current
    val spacing = LocalHitchySpacing.current

    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val pins = SamplePoints.mapIndexed { index, point ->
        MapPin(id = index.toString(), point = point, selected = index == selectedIndex)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.white),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(spacing.space16),
            verticalArrangement = Arrangement.spacedBy(spacing.space12),
        ) {
            Text(
                text = "Map (needs design review)",
                style = typography.h3,
                color = colors.textPrimary,
            )
            Text(
                text = "HitchyMap: fixed point pins and a straight route line. Tap a pin to select it. " +
                    "Without MAPS_API_KEY in android/local.properties the map stays grey.",
                style = typography.caption,
                color = colors.textSecondary,
            )
            HitchyMap(
                pins = pins,
                route = SamplePoints,
                onPinClick = { id -> selectedIndex = id.toInt() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(12.dp)),
            )
        }
    }
}
