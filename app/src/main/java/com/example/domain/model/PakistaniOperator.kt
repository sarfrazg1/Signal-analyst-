package com.example.domain.model

enum class PakistaniOperator(
    val brandName: String,
    val urduName: String,
    val legalName: String,
    val primaryMncList: List<String>,
    val primaryColorHex: Long,
    val secondaryColorHex: Long
) {
    JAZZ(
        brandName = "Jazz",
        urduName = "جاز",
        legalName = "PMCL (Mobilink & Warid)",
        primaryMncList = listOf("01", "07", "1", "7"),
        primaryColorHex = 0xFFD32F2F, // Crimson Red
        secondaryColorHex = 0xFFFFEBEE
    ),
    ZONG(
        brandName = "Zong 4G",
        urduName = "زونگ 4G",
        legalName = "CMPak (China Mobile Pakistan)",
        primaryMncList = listOf("04", "4"),
        primaryColorHex = 0xFF00897B, // Teal / Cyan Brand
        secondaryColorHex = 0xFFE0F2F1
    ),
    UFONE(
        brandName = "Ufone 4G",
        urduName = "یوفون 4G",
        legalName = "PTML (Pakistan Telecom Mobile Ltd)",
        primaryMncList = listOf("03", "3"),
        primaryColorHex = 0xFFFF6F00, // Vibrant Orange
        secondaryColorHex = 0xFFFFF3E0
    ),
    TELENOR(
        brandName = "Telenor",
        urduName = "ٹیلی نار",
        legalName = "Telenor Pakistan",
        primaryMncList = listOf("06", "6"),
        primaryColorHex = 0xFF0288D1, // Telecom Sky Blue
        secondaryColorHex = 0xFFE1F5FE
    ),
    SCOM(
        brandName = "SCOM",
        urduName = "ایس کام",
        legalName = "Special Communications Organization (AJK & GB)",
        primaryMncList = listOf("05", "5"),
        primaryColorHex = 0xFF2E7D32, // Forest Green
        secondaryColorHex = 0xFFE8F5E9
    ),
    OTHER(
        brandName = "Other / Global Operator",
        urduName = "دیگر آپریٹر",
        legalName = "Non-PK / Unidentified Carrier",
        primaryMncList = emptyList(),
        primaryColorHex = 0xFF546E7A, // Steel Slate
        secondaryColorHex = 0xFFECEFF1
    );

    companion object {
        const val PAKISTAN_MCC = "410"

        fun fromMccMnc(mcc: String?, mnc: String?, carrierName: String? = null): PakistaniOperator {
            val cleanMcc = mcc?.trim()
            val cleanMnc = mnc?.trim()

            // If MCC is Pakistan (410) or empty and matches MNC list
            if (cleanMcc == PAKISTAN_MCC || cleanMcc == null) {
                if (cleanMnc != null) {
                    values().firstOrNull { op -> op.primaryMncList.contains(cleanMnc) }?.let {
                        return it
                    }
                }
            }

            // Fallback by carrier name analysis (SPN / Network Operator Name)
            if (!carrierName.isNullOrBlank()) {
                val lower = carrierName.lowercase()
                when {
                    lower.contains("jazz") || lower.contains("mobilink") || lower.contains("warid") || lower.contains("pmcl") -> return JAZZ
                    lower.contains("zong") || lower.contains("cmpak") || lower.contains("china mobile") -> return ZONG
                    lower.contains("ufone") || lower.contains("ptml") -> return UFONE
                    lower.contains("telenor") -> return TELENOR
                    lower.contains("scom") || lower.contains("sco") -> return SCOM
                }
            }

            return OTHER
        }
    }
}
