package com.personal.financeapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DonutChart(
    data: List<Pair<Color, Double>>,
    modifier: Modifier = Modifier,
    centerLabel: String = ""
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val total = data.sumOf { it.second }.coerceAtLeast(0.001)
            var startAngle = -90f
            val strokeWidth = size.minDimension * 0.18f
            val radius = (size.minDimension - strokeWidth) / 2f
            val topLeft = Offset((size.width - radius * 2) / 2f, (size.height - radius * 2) / 2f)
            val arcSize = Size(radius * 2, radius * 2)

            data.forEach { (color, value) ->
                val sweep = (value / total * 360f).toFloat()
                drawArc(
                    color = color,
                    startAngle = startAngle,
                    sweepAngle = sweep - 1f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                )
                startAngle += sweep
            }
        }
        if (centerLabel.isNotBlank()) {
            Text(centerLabel, style = MaterialTheme.typography.labelMedium, fontSize = 11.sp)
        }
    }
}

@Composable
fun LineChart(
    data: List<Pair<Float, Float>>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    fillAlpha: Float = 0.12f
) {
    if (data.size < 2) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("No data", style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    val fill = lineColor.copy(alpha = fillAlpha)
    Canvas(modifier = modifier) {
        val xMin = data.minOf { it.first }
        val xMax = data.maxOf { it.first }
        val yMin = data.minOf { it.second }
        val yMax = data.maxOf { it.second }
        val xRange = (xMax - xMin).coerceAtLeast(1f)
        val yRange = (yMax - yMin).coerceAtLeast(1f)
        val pad = 8.dp.toPx()

        fun tx(x: Float) = pad + (x - xMin) / xRange * (size.width - pad * 2)
        fun ty(y: Float) = size.height - pad - (y - yMin) / yRange * (size.height - pad * 2)

        val linePath = Path().apply {
            moveTo(tx(data[0].first), ty(data[0].second))
            for (i in 1 until data.size) lineTo(tx(data[i].first), ty(data[i].second))
        }
        val fillPath = Path().apply {
            addPath(linePath)
            lineTo(tx(data.last().first), size.height)
            lineTo(tx(data.first().first), size.height)
            close()
        }
        drawPath(fillPath, color = fill)
        drawPath(linePath, color = lineColor, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
        data.forEach { (x, y) ->
            drawCircle(lineColor, radius = 4.dp.toPx(), center = Offset(tx(x), ty(y)))
        }
    }
}

@Composable
fun BarChart(
    data: List<MonthlyBarData>,
    modifier: Modifier = Modifier,
    incomeColor: Color = Color(0xFF4CAF50),
    expenseColor: Color = Color(0xFFF44336)
) {
    if (data.isEmpty()) return
    Canvas(modifier = modifier) {
        val maxVal = data.maxOf { maxOf(it.income, it.expense) }.coerceAtLeast(0.01)
        val slotWidth = size.width / data.size
        val barWidth = slotWidth * 0.35f
        val maxBarHeight = size.height * 0.88f

        data.forEachIndexed { i, item ->
            val slotX = i * slotWidth
            val incH = (item.income / maxVal * maxBarHeight).toFloat()
            val expH = (item.expense / maxVal * maxBarHeight).toFloat()
            // income bar (left of slot center)
            drawRect(
                color = incomeColor,
                topLeft = Offset(slotX + slotWidth * 0.05f, size.height - incH),
                size = Size(barWidth, incH)
            )
            // expense bar (right of slot center)
            drawRect(
                color = expenseColor,
                topLeft = Offset(slotX + slotWidth * 0.5f, size.height - expH),
                size = Size(barWidth, expH)
            )
        }
    }
}

data class MonthlyBarData(val label: String, val income: Double, val expense: Double)
