package org.safieddine.ablogistics.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.material.Icon
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.composefluent.FluentTheme
import io.github.composefluent.component.*
import io.github.composefluent.icons.Icons
import io.github.composefluent.icons.regular.*
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.safieddine.ablogistics.data.CustomerResponse
import org.safieddine.ablogistics.data.MaterialType
import org.safieddine.ablogistics.data.ReceiptResponse
import org.safieddine.ablogistics.data.service.CustomerService
import org.safieddine.ablogistics.data.service.ReceiptService
import org.safieddine.ablogistics.data.session.SessionStore
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale

// ─── Palette ─────────────────────────────────────────────────────────────────
private val FuelBlue        = Color(0xFF0078D4)
private val FuelBluePale    = Color(0xFFDEECF9)
private val DieselAmber     = Color(0xFFC8922A)
private val DieselAmberPale = Color(0xFFFDF3DC)
private val ProfitGreen     = Color(0xFF0E7A0D)
private val ProfitGreenPale = Color(0xFFDFF6DD)
private val PendingOrange   = Color(0xFFCA5010)
private val PendingOrangePale = Color(0xFFFEF0E6)
private val RevenuePurple   = Color(0xFF744DA9)
private val RevenuePurplePale = Color(0xFFF0E6FF)

// ─── Data state ───────────────────────────────────────────────────────────────
private data class DashboardState(
    val isLoading: Boolean = true,
    val error: String? = null,
    // Liters
    val fuelDelivered: BigDecimal = BigDecimal.ZERO,
    val dieselDelivered: BigDecimal = BigDecimal.ZERO,
    val fuelPending: BigDecimal = BigDecimal.ZERO,
    val dieselPending: BigDecimal = BigDecimal.ZERO,
    // Revenue/profit from profit-analysis API
    val totalRevenue: BigDecimal = BigDecimal.ZERO,
    val totalProfit: BigDecimal = BigDecimal.ZERO,
    val totalShortagePenalty: BigDecimal = BigDecimal.ZERO,
    // Customer chart data
    val customerStats: List<CustomerStat> = emptyList()
)

private data class CustomerStat(
    val name: String,
    val fuelLiters: BigDecimal,
    val dieselLiters: BigDecimal
) {
    val totalLiters: BigDecimal get() = fuelLiters.add(dieselLiters)
}

// ─── DashboardScreen ─────────────────────────────────────────────────────────
@Composable
fun DashboardScreen() {
    val scope = rememberCoroutineScope()
    val selectedWarehouse by SessionStore.selectedWarehouse.collectAsState()
    var state by remember { mutableStateOf(DashboardState()) }

    fun reload(warehouseId: Long) {
        state = state.copy(isLoading = true, error = null)
        scope.launch {
            try {
                coroutineScope {
                    val profitDeferred  = async { ReceiptService.getProfitAnalysis() }
                    val receiptsDeferred = async {
                        ReceiptService.listWarehouseDetailed(warehouseId = warehouseId, size = 500)
                    }
                    val customersDeferred = async { CustomerService.list(warehouseId) }

                    val profitRes   = profitDeferred.await()
                    val receiptsRes = receiptsDeferred.await()
                    val customersRes = customersDeferred.await()

                    // ── Profit & revenue ────────────────────────────────────
                    val profitData = profitRes.getOrNull()?.data
                    val totalProfit   = profitData?.totalProfit ?: BigDecimal.ZERO
                    val totalRevenue  = profitData?.totalExpectedRevenue ?: BigDecimal.ZERO
                    val shortagePenalty = profitData?.totalShortagePenalty ?: BigDecimal.ZERO

                    // ── Load receipts (only OUTWARD BRV deliveries to CUSTOMER) ─────────────
                    val allLoads: List<ReceiptResponse> =
                        receiptsRes.getOrNull()?.data?.receipts
                            ?.filter {
                                it.brvId != null &&
                                it.receiptType == org.safieddine.ablogistics.data.ReceiptType.OUTWARD &&
                                it.entityType == org.safieddine.ablogistics.data.EntityType.CUSTOMER
                            } ?: emptyList()

                    // Finalized = dispatchedQuantity is non-null and > 0
                    val finalized = allLoads.filter {
                        it.dispatchedQuantity != null && it.dispatchedQuantity.compareTo(BigDecimal.ZERO) != 0
                    }
                    val pending = allLoads.filter {
                        it.dispatchedQuantity == null || it.dispatchedQuantity.compareTo(BigDecimal.ZERO) == 0
                    }

                    fun sumDispatched(list: List<ReceiptResponse>, type: MaterialType) =
                        list.filter { it.materialType == type }
                            .fold(BigDecimal.ZERO) { acc, r ->
                                acc.add(r.dispatchedQuantity ?: BigDecimal.ZERO)
                            }

                    fun sumLoaded(list: List<ReceiptResponse>, type: MaterialType) =
                        list.filter { it.materialType == type }
                            .fold(BigDecimal.ZERO) { acc, r ->
                                acc.add(r.loadedQuantity ?: BigDecimal.ZERO)
                            }

                    val fuelDelivered   = sumDispatched(finalized, MaterialType.FUEL)
                    val dieselDelivered = sumDispatched(finalized, MaterialType.DIESEL)
                    val fuelPending     = sumLoaded(pending, MaterialType.FUEL)
                    val dieselPending   = sumLoaded(pending, MaterialType.DIESEL)

                    // ── Customer stats from real API ──────────────────────
                    val customers: List<CustomerResponse> =
                        customersRes.getOrNull()?.data?.customers ?: emptyList()

                    val stats = customers
                        .map { c ->
                            CustomerStat(
                                name = c.name,
                                fuelLiters = c.totalFuelLiters,
                                dieselLiters = c.totalDieselLiters
                            )
                        }
                        .filter { it.totalLiters > BigDecimal.ZERO }
                        .sortedByDescending { it.totalLiters }

                    state = DashboardState(
                        isLoading = false,
                        fuelDelivered = fuelDelivered,
                        dieselDelivered = dieselDelivered,
                        fuelPending = fuelPending,
                        dieselPending = dieselPending,
                        totalRevenue = totalRevenue,
                        totalProfit = totalProfit,
                        totalShortagePenalty = shortagePenalty,
                        customerStats = stats
                    )
                }
            } catch (e: Exception) {
                state = state.copy(isLoading = false, error = e.message ?: "Unknown error")
            }
        }
    }

    LaunchedEffect(selectedWarehouse) {
        selectedWarehouse?.id?.let { reload(it) }
    }

    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(FluentTheme.colors.background.mica.base.copy(alpha = 0.05f))
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
        // ── Header ────────────────────────────────────────────────────────
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(
                    "Warehouse · ${selectedWarehouse?.name ?: "—"}",
                    style = FluentTheme.typography.caption,
                    color = FluentTheme.colors.background.smoke.default
                )
            }
            Spacer(Modifier.weight(1f))
            org.safieddine.ablogistics.ui.theme.ABLogisticsSubtleButton(
                onClick = { selectedWarehouse?.id?.let { reload(it) } },
                iconOnly = true
            ) {
                Icon(
                    imageVector = Icons.Regular.ArrowCounterclockwise,
                    contentDescription = "Refresh",
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // ── Loading / error ───────────────────────────────────────────────
        if (state.isLoading) {
            Box(Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    ProgressRing()
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Loading dashboard…",
                        style = FluentTheme.typography.caption,
                        color = FluentTheme.colors.background.smoke.default
                    )
                }
            }
            return@Column
        }

        if (state.error != null) {
            InfoBar(
                modifier = Modifier.fillMaxWidth(),
                title = { Text("Failed to load dashboard") },
                severity = InfoBarSeverity.Critical,
                message = { Text(state.error!!) }
            )
            return@Column
        }

        // ── Row 1: Delivered liters ────────────────────────────────────────
        SectionLabel("Delivered Volumes")
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Max), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            KpiCard(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                label = "Fuel Delivered",
                value = formatLiters(state.fuelDelivered),
                unit = "L",
                accent = FuelBlue,
                bg = FuelBluePale,
                icon = Icons.Regular.DocumentTableTruck
            )
            KpiCard(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                label = "Diesel Delivered",
                value = formatLiters(state.dieselDelivered),
                unit = "L",
                accent = DieselAmber,
                bg = DieselAmberPale,
                icon = Icons.Regular.DocumentTableTruck
            )
            // Delivered mix card
            val totalDelivered = state.fuelDelivered.add(state.dieselDelivered)
            FluentCard(Modifier.weight(2f).fillMaxHeight()) {
                Column(Modifier.fillMaxHeight().padding(16.dp)) {
                    Text(
                        "Delivered Mix",
                        style = FluentTheme.typography.caption,
                        color = FluentTheme.colors.background.smoke.default
                    )
                    Spacer(Modifier.weight(1f))
                    MaterialSplitBar(
                        fuelValue = state.fuelDelivered,
                        dieselValue = state.dieselDelivered,
                        fuelColor = FuelBlue,
                        dieselColor = DieselAmber
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Total: ${formatLiters(totalDelivered)} L",
                        style = FluentTheme.typography.bodyStrong,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Row 2: Pending liters ─────────────────────────────────────────
        SectionLabel("Pending Deliveries")
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Max), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            KpiCard(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                label = "Fuel Pending",
                value = formatLiters(state.fuelPending),
                unit = "L",
                accent = PendingOrange,
                bg = PendingOrangePale,
                icon = Icons.Regular.History
            )
            KpiCard(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                label = "Diesel Pending",
                value = formatLiters(state.dieselPending),
                unit = "L",
                accent = PendingOrange,
                bg = PendingOrangePale,
                icon = Icons.Regular.History
            )
            // Progress ring
            val totalDelivered2 = state.fuelDelivered.add(state.dieselDelivered)
            val grandTotal = totalDelivered2.add(state.fuelPending).add(state.dieselPending)
            val deliveryRatio = if (grandTotal > BigDecimal.ZERO)
                totalDelivered2.divide(grandTotal, 4, RoundingMode.HALF_UP).toFloat() else 0f
            DeliveryRatioCard(Modifier.weight(2f).fillMaxHeight(), deliveryRatio, grandTotal)
        }

        Spacer(Modifier.height(16.dp))

        // ── Row 3: Revenue & Profit ───────────────────────────────────────
        SectionLabel("Financial Summary")
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            KpiCard(
                modifier = Modifier.weight(1f),
                label = "Expected Revenue",
                value = formatMoney(state.totalRevenue),
                unit = "DZD",
                accent = RevenuePurple,
                bg = RevenuePurplePale,
                icon = Icons.Regular.Apps
            )
            KpiCard(
                modifier = Modifier.weight(1f),
                label = "Total Profit",
                value = formatMoney(state.totalProfit),
                unit = "DZD",
                accent = ProfitGreen,
                bg = ProfitGreenPale,
                icon = Icons.Regular.CheckmarkCircle
            )
            if (state.totalShortagePenalty > BigDecimal.ZERO) {
                KpiCard(
                    modifier = Modifier.weight(1f),
                    label = "Shortage Penalty",
                    value = formatMoney(state.totalShortagePenalty),
                    unit = "DZD",
                    accent = Color(0xFFD32F2F),
                    bg = Color(0xFFFFEBEE),
                    icon = Icons.Regular.Dismiss
                )
            } else {
                Spacer(Modifier.weight(1f))
            }
        }

        // ── Row 4: Customer Bar Chart ─────────────────────────────────────
        if (state.customerStats.isNotEmpty()) {
            Spacer(Modifier.height(24.dp))
            SectionLabel("Top Customers by Volume")
            Spacer(Modifier.height(8.dp))
            FluentCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Liters consumed per customer",
                            style = FluentTheme.typography.caption,
                            color = FluentTheme.colors.background.smoke.default
                        )
                        Spacer(Modifier.weight(1f))
                        LegendDot(FuelBlue, "Fuel")
                        Spacer(Modifier.width(14.dp))
                        LegendDot(DieselAmber, "Diesel")
                    }
                    Spacer(Modifier.height(20.dp))
                    CustomerBarChart(
                        stats = state.customerStats,
                        fuelColor = FuelBlue,
                        dieselColor = DieselAmber
                    )
                }
            }
        }

        }

        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            adapter = rememberScrollbarAdapter(scrollState)
        )
    }
}

// ─── Section label ────────────────────────────────────────────────────────────
@Composable
private fun SectionLabel(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .width(3.dp)
                .height(14.dp)
                .background(FluentTheme.colors.fillAccent.default, RoundedCornerShape(2.dp))
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text,
            style = FluentTheme.typography.bodyStrong,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1A1A1A)
        )
    }
}

// ─── KPI Card ─────────────────────────────────────────────────────────────────
@Composable
private fun KpiCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    unit: String,
    accent: Color,
    bg: Color,
    icon: ImageVector
) {
    Box(
        modifier
            .shadow(1.dp, RoundedCornerShape(8.dp))
            .background(Color.White, RoundedCornerShape(8.dp))
            .border(1.dp, accent.copy(alpha = 0.18f), RoundedCornerShape(8.dp))
    ) {
        Column(Modifier.fillMaxHeight().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(30.dp)
                        .background(bg, RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    label,
                    style = FluentTheme.typography.caption,
                    color = FluentTheme.colors.background.smoke.default,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.weight(1f))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    value,
                    style = FluentTheme.typography.subtitle,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    unit,
                    style = FluentTheme.typography.caption.copy(fontSize = 10.sp),
                    color = accent,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
            // Accent stripe at bottom
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(accent.copy(alpha = 0.15f))
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(0.65f)
                        .fillMaxHeight()
                        .background(Brush.horizontalGradient(listOf(accent, accent.copy(alpha = 0.3f))), RoundedCornerShape(2.dp))
                )
            }
        }
    }
}

// ─── Delivery Ratio Card ──────────────────────────────────────────────────────
@Composable
private fun DeliveryRatioCard(modifier: Modifier, ratio: Float, grandTotal: BigDecimal) {
    val animRatio by animateFloatAsState(
        targetValue = ratio,
        animationSpec = tween(900, easing = EaseOutCubic),
        label = "ratio_anim"
    )
    Box(
        modifier
            .shadow(1.dp, RoundedCornerShape(8.dp))
            .background(Color.White, RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
    ) {
        Column(Modifier.fillMaxHeight().padding(16.dp)) {
            Text(
                "Delivery Completion",
                style = FluentTheme.typography.caption,
                color = FluentTheme.colors.background.smoke.default
            )
            Spacer(Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                androidx.compose.foundation.Canvas(Modifier.size(64.dp)) {
                    val stroke = 9.dp.toPx()
                    drawArc(
                        color = Color(0xFFEEEEEE),
                        startAngle = 0f, sweepAngle = 360f,
                        useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(stroke, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = ProfitGreen,
                        startAngle = -90f, sweepAngle = 360f * animRatio,
                        useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(stroke, cap = StrokeCap.Round)
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        "${(animRatio * 100).toInt()}%",
                        style = FluentTheme.typography.subtitle,
                        fontWeight = FontWeight.Bold,
                        color = ProfitGreen
                    )
                    Text(
                        "of ${formatLiters(grandTotal)} L dispatched",
                        style = FluentTheme.typography.caption,
                        color = FluentTheme.colors.background.smoke.default
                    )
                    if (ratio < 1f) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "${formatLiters(grandTotal.multiply(BigDecimal(1 - ratio.toDouble())))} L pending",
                            style = FluentTheme.typography.caption.copy(fontSize = 11.sp),
                            color = PendingOrange
                        )
                    }
                }
            }
        }
    }
}

// ─── Customer Bar Chart ───────────────────────────────────────────────────────
@Composable
private fun CustomerBarChart(
    stats: List<CustomerStat>,
    fuelColor: Color,
    dieselColor: Color
) {
    val maxLiters = stats.maxOfOrNull { it.totalLiters.toFloat() }?.coerceAtLeast(1f) ?: 1f
    val chartHeight = 200.dp
    val barWidth = 18.dp
    val barGap = 4.dp

    Column {
        Row(
            Modifier.fillMaxWidth().height(chartHeight),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            stats.forEach { stat ->
                val fuelFrac   = (stat.fuelLiters.toFloat() / maxLiters).coerceIn(0f, 1f)
                val dieselFrac = (stat.dieselLiters.toFloat() / maxLiters).coerceIn(0f, 1f)

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    // Total label
                    Text(
                        formatLitersShort(stat.totalLiters),
                        style = FluentTheme.typography.caption.copy(fontSize = 9.sp),
                        color = FluentTheme.colors.background.smoke.default,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                    Spacer(Modifier.height(3.dp))
                    // Bars side-by-side
                    Row(
                        Modifier.height(chartHeight - 22.dp),
                        horizontalArrangement = Arrangement.spacedBy(barGap),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        AnimatedBar(fuelFrac, fuelColor, barWidth, chartHeight - 22.dp)
                        AnimatedBar(dieselFrac, dieselColor, barWidth, chartHeight - 22.dp)
                    }
                }
            }
        }

        Spacer(Modifier.height(6.dp))
        HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 1.dp)
        Spacer(Modifier.height(6.dp))

        // X-axis customer names
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            stats.forEach { stat ->
                Text(
                    stat.name,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = FluentTheme.typography.caption.copy(fontSize = 10.sp),
                    color = FluentTheme.colors.background.smoke.default,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun AnimatedBar(fraction: Float, color: Color, width: Dp, maxHeight: Dp) {
    val animFrac by animateFloatAsState(
        targetValue = fraction,
        animationSpec = tween(800, easing = EaseOutCubic),
        label = "bar_${color.value}"
    )
    val h = (maxHeight.value * animFrac).coerceAtLeast(0f).dp
    Box(
        Modifier
            .width(width)
            .height(h)
            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
            .background(Brush.verticalGradient(listOf(color, color.copy(alpha = 0.55f))))
    )
}

// ─── Material Split Bar ───────────────────────────────────────────────────────
@Composable
private fun MaterialSplitBar(
    fuelValue: BigDecimal,
    dieselValue: BigDecimal,
    fuelColor: Color,
    dieselColor: Color
) {
    val total = fuelValue.add(dieselValue)
    val fuelFrac = if (total > BigDecimal.ZERO)
        fuelValue.divide(total, 4, RoundingMode.HALF_UP).toFloat() else 0.5f
    val animFuel by animateFloatAsState(fuelFrac, tween(700, easing = EaseOutCubic), label = "split")

    Column {
        Row(
            Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
        ) {
            if (animFuel > 0f) {
                Box(Modifier.weight(animFuel).fillMaxHeight().background(fuelColor))
            }
            if (animFuel < 1f) {
                Box(Modifier.weight(1f - animFuel).fillMaxHeight().background(dieselColor))
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(7.dp).background(fuelColor, CircleShape))
                Spacer(Modifier.width(5.dp))
                Text(
                    "${formatLiters(fuelValue)} L Fuel",
                    style = FluentTheme.typography.caption,
                    color = fuelColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${formatLiters(dieselValue)} L Diesel",
                    style = FluentTheme.typography.caption,
                    color = dieselColor,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.width(5.dp))
                Box(Modifier.size(7.dp).background(dieselColor, CircleShape))
            }
        }
    }
}

// ─── Fluent Card ──────────────────────────────────────────────────────────────
@Composable
private fun FluentCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier
            .shadow(1.dp, RoundedCornerShape(8.dp))
            .background(Color.White, RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
    ) { content() }
}

// ─── Legend dot ───────────────────────────────────────────────────────────────
@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).background(color, CircleShape))
        Spacer(Modifier.width(4.dp))
        Text(label, style = FluentTheme.typography.caption, color = FluentTheme.colors.background.smoke.default)
    }
}

// ─── Number formatters ────────────────────────────────────────────────────────
private fun formatLiters(value: BigDecimal): String =
    String.format(Locale.getDefault(), "%,.2f", value)

private fun formatLitersShort(value: BigDecimal): String {
    val d = value.toDouble()
    return when {
        d >= 1_000_000 -> String.format(Locale.getDefault(), "%.1fM", d / 1_000_000)
        d >= 1_000     -> String.format(Locale.getDefault(), "%.1fK", d / 1_000)
        else           -> String.format(Locale.getDefault(), "%.0f", d)
    }
}

private fun formatMoney(value: BigDecimal): String =
    String.format(Locale.getDefault(), "%,.0f", value)