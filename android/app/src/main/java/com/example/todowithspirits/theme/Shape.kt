package com.example.todowithspirits.theme

import androidx.compose.foundation.shape.GenericShape
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import kotlin.math.*

/**
 * Flat-top hexagon (horizontal orientation, flat edges at top/bottom) with rounded corners.
 * Corner rounding is computed geometrically so arcs are tangent to each edge.
 */
val HexagonShape = GenericShape { size, _ ->
    val w = size.width
    val h = size.height
    // Corner radius as a fraction of the shorter dimension
    val r = minOf(w, h) * 0.20f

    // Flat-top hexagon vertices, clockwise in screen coords (y-down)
    // Proportions match Figma: W=360, H=389
    val pts = arrayOf(
        Offset(w * 0.25f, 0f),      // top-left
        Offset(w * 0.75f, 0f),      // top-right
        Offset(w,         h * 0.5f), // right
        Offset(w * 0.75f, h),        // bottom-right
        Offset(w * 0.25f, h),        // bottom-left
        Offset(0f,        h * 0.5f)  // left
    )
    val n = pts.size

    for (i in 0 until n) {
        val prev = pts[(i - 1 + n) % n]
        val curr = pts[i]
        val next = pts[(i + 1) % n]

        // Incoming unit vector (direction arriving at curr)
        val ax = curr.x - prev.x;  val ay = curr.y - prev.y
        val al = sqrt(ax * ax + ay * ay)
        val ux = ax / al;           val uy = ay / al

        // Outgoing unit vector (direction leaving curr)
        val bx = next.x - curr.x;  val by = next.y - curr.y
        val bl = sqrt(bx * bx + by * by)
        val vx = bx / bl;           val vy = by / bl

        // Interior angle via dot product of reversed-incoming and outgoing
        val dot = -ux * vx - uy * vy   // cos(interior angle)

        // tan(half interior angle) = sqrt((1-cos)/(1+cos))
        // Tangent length from vertex to tangent point = r / tan(α/2)
        val halfTan = sqrt((1f - dot) / (1f + dot).coerceAtLeast(1e-6f))
        val tLen = (r / halfTan).coerceAtMost(minOf(al, bl) * 0.45f)
        val effectiveR = tLen * halfTan

        // Tangent points on the two edges
        val t1x = curr.x - ux * tLen;  val t1y = curr.y - uy * tLen
        val t2x = curr.x + vx * tLen;  val t2y = curr.y + vy * tLen

        // Arc center: t1 offset by effectiveR in the CCW-rotated incoming direction
        // CCW rotation of (ux, uy) = (-uy, ux)
        val cx = t1x - uy * effectiveR
        val cy = t1y + ux * effectiveR

        // Compute arc start and sweep angles
        val startAngle = atan2(t1y - cy, t1x - cx) * (180f / PI.toFloat())
        val endAngle   = atan2(t2y - cy, t2x - cx) * (180f / PI.toFloat())
        var sweep = endAngle - startAngle
        // Normalise to (-180, 180]; for a convex CW polygon this is always positive
        if (sweep < -180f) sweep += 360f
        if (sweep >  180f) sweep -= 360f

        if (i == 0) moveTo(t1x, t1y) else lineTo(t1x, t1y)
        arcTo(
            rect = Rect(cx - effectiveR, cy - effectiveR, cx + effectiveR, cy + effectiveR),
            startAngleDegrees = startAngle,
            sweepAngleDegrees = sweep,
            forceMoveTo = false
        )
    }
    close()
}
