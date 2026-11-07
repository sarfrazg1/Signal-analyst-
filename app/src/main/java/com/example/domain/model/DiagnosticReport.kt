package com.example.domain.model

enum class DiagnosticCategory(val label: String) {
    EXCELLENT_LINK("Optimal Network Performance"),
    WEAK_COVERAGE("Weak Signal Coverage / Distance"),
    HIGH_INTERFERENCE("RF Multipath / Co-Channel Interference"),
    TOWER_CONGESTION("Cell Sector Congestion / Capacity Limit"),
    LEGACY_FALLBACK("Legacy Network Fallback (2G/3G)"),
    HARDWARE_OR_SIM("SIM / Baseband Hardware State"),
    NO_SIGNAL_OUTAGE("Out of Base Station Coverage"),
    ROAMING_MODE("National / International Roaming")
}

enum class DiagnosticSeverity(val colorHex: Long) {
    OPTIMAL(0xFF2E7D32),  // Green
    MODERATE(0xFFF57C00), // Orange
    CRITICAL(0xFFD32F2F)  // Red
}

data class DiagnosticReport(
    val category: DiagnosticCategory,
    val severity: DiagnosticSeverity,
    val titleEn: String,
    val titleUr: String,
    val explanationEn: String,
    val explanationUr: String,
    val recommendationsEn: List<String>,
    val recommendationsUr: List<String>,
    val evidenceMetrics: Map<String, String>,
    val timestamp: Long = System.currentTimeMillis()
)
