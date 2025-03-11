package ma.xpi.tlvtools.util

/**
 * Utility class for mapping ISO 4217 currency codes to currency names and symbols
 */
object CurrencyMap {
    private val currencyMap = mapOf(
        "156" to CurrencyInfo("CNY", "Chinese Yuan", "¥"),
        "344" to CurrencyInfo("HKD", "Hong Kong Dollar", "HK$"),
        "360" to CurrencyInfo("IDR", "Indonesian Rupiah", "Rp"),
        "392" to CurrencyInfo("JPY", "Japanese Yen", "¥"),
        "410" to CurrencyInfo("KRW", "South Korean Won", "₩"),
        "458" to CurrencyInfo("MYR", "Malaysian Ringgit", "RM"),
        "608" to CurrencyInfo("PHP", "Philippine Peso", "₱"),
        "702" to CurrencyInfo("SGD", "Singapore Dollar", "S$"),
        "764" to CurrencyInfo("THB", "Thai Baht", "฿"),
        "901" to CurrencyInfo("TWD", "New Taiwan Dollar", "NT$"),
        "704" to CurrencyInfo("VND", "Vietnamese Dong", "₫"),
        "036" to CurrencyInfo("AUD", "Australian Dollar", "A$"),
        "124" to CurrencyInfo("CAD", "Canadian Dollar", "C$"),
        "978" to CurrencyInfo("EUR", "Euro", "€"),
        "826" to CurrencyInfo("GBP", "British Pound", "£"),
        "356" to CurrencyInfo("INR", "Indian Rupee", "₹"),
        "840" to CurrencyInfo("USD", "US Dollar", "$"),
        "784" to CurrencyInfo("AED", "UAE Dirham", "د.إ"),
        "682" to CurrencyInfo("SAR", "Saudi Riyal", "﷼"),
        "643" to CurrencyInfo("RUB", "Russian Ruble", "₽"),
        "710" to CurrencyInfo("ZAR", "South African Rand", "R"),
        "404" to CurrencyInfo("KES", "Kenyan Shilling", "KSh"),
        "566" to CurrencyInfo("NGN", "Nigerian Naira", "₦"),
        "504" to CurrencyInfo("MAD", "Moroccan Dirham", "د.م."),
        "818" to CurrencyInfo("EGP", "Egyptian Pound", "E£"),
        "985" to CurrencyInfo("PLN", "Polish Złoty", "zł"),
        "756" to CurrencyInfo("CHF", "Swiss Franc", "CHF"),
        "578" to CurrencyInfo("NOK", "Norwegian Krone", "kr"),
        "752" to CurrencyInfo("SEK", "Swedish Krona", "kr"),
        "208" to CurrencyInfo("DKK", "Danish Krone", "kr"),
        "949" to CurrencyInfo("TRY", "Turkish Lira", "₺"),
        "986" to CurrencyInfo("BRL", "Brazilian Real", "R$"),
        "484" to CurrencyInfo("MXN", "Mexican Peso", "Mex$"),
        "032" to CurrencyInfo("ARS", "Argentine Peso", "AR$"),
        "152" to CurrencyInfo("CLP", "Chilean Peso", "CLP$"),
        "604" to CurrencyInfo("PEN", "Peruvian Sol", "S/")
    )

    /**
     * Get the currency name and symbol for the given ISO 4217 currency code
     * @param code The ISO 4217 currency code
     * @return The currency name and symbol
     */
    fun getCurrencyName(code: String): String {
        val currencyInfo = currencyMap[code]
        return if (currencyInfo != null) {
            "${currencyInfo.code} - ${currencyInfo.name} (${currencyInfo.symbol})"
        } else {
            "Unknown Currency ($code)"
        }
    }

    /**
     * Get the currency symbol for the given ISO 4217 currency code
     * @param code The ISO 4217 currency code
     * @return The currency symbol
     */
    fun getCurrencySymbol(code: String): String {
        return currencyMap[code]?.symbol ?: code
    }

    /**
     * Data class for currency information
     */
    data class CurrencyInfo(
        val code: String,
        val name: String,
        val symbol: String
    )
}
