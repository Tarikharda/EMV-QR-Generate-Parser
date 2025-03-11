package ma.xpi.tlvtools.parser

import android.util.Log
import ma.xpi.tlvtools.model.EmvQrData
import ma.xpi.tlvtools.model.LanguageTemplate
import ma.xpi.tlvtools.model.MerchantAccount
import ma.xpi.tlvtools.model.TlvObject
import ma.xpi.tlvtools.util.CrcCalculator
import ma.xpi.tlvtools.util.CurrencyMap
import ma.xpi.tlvtools.util.MccMap

/**
 * Parser for EMV QR codes
 */
class EmvQrParser {

    companion object {
        // EMV QR code field IDs
        const val ID_PAYLOAD_FORMAT = "00"
        const val ID_INITIATION_METHOD = "01"
        const val ID_MERCHANT_ACCOUNT_INFO_START = "02"
        const val ID_MERCHANT_ACCOUNT_INFO_END = "51"
        const val ID_MCC = "52"
        const val ID_CURRENCY = "53"
        const val ID_AMOUNT = "54"
        const val ID_TIP_INDICATOR = "55"
        const val ID_VALUE_OF_CONVENIENCE_FEE_FIXED = "56"
        const val ID_VALUE_OF_CONVENIENCE_FEE_PERCENTAGE = "57"
        const val ID_COUNTRY_CODE = "58"
        const val ID_MERCHANT_NAME = "59"
        const val ID_MERCHANT_CITY = "60"
        const val ID_POSTAL_CODE = "61"
        const val ID_ADDITIONAL_DATA = "62"
        const val ID_LANGUAGE_TEMPLATE = "64"
        const val ID_RFU_START = "65"
        const val ID_RFU_END = "79"
        const val ID_UNRESERVED_TEMPLATES_START = "80"
        const val ID_UNRESERVED_TEMPLATES_END = "99"
        const val ID_CRC = "63"

        // Additional Data field IDs
        const val ID_ADDITIONAL_BILL_NUMBER = "01"
        const val ID_ADDITIONAL_MOBILE_NUMBER = "02"
        const val ID_ADDITIONAL_STORE_LABEL = "03"
        const val ID_ADDITIONAL_LOYALTY_NUMBER = "04"
        const val ID_ADDITIONAL_REFERENCE_LABEL = "05"
        const val ID_ADDITIONAL_CUSTOMER_LABEL = "06"
        const val ID_ADDITIONAL_TERMINAL_LABEL = "07"
        const val ID_ADDITIONAL_PURPOSE_TRANSACTION = "08"
        const val ID_ADDITIONAL_CONSUMER_DATA_REQUEST = "09"
        const val ID_ADDITIONAL_RFU_START = "10"
        const val ID_ADDITIONAL_RFU_END = "99"

        // Language Template field IDs
        const val ID_LANGUAGE_PREFERENCE = "00"
        const val ID_LANGUAGE_MERCHANT_NAME = "01"
        const val ID_LANGUAGE_MERCHANT_CITY = "02"
        const val ID_LANGUAGE_RFU_START = "03"
        const val ID_LANGUAGE_RFU_END = "99"

        // Unreserved Templates (Custom) field IDs
        const val ID_FOOTER_TICKET = "80"
        const val ID_INTERCHANGE_FEE = "81"
        const val ID_APPLICATION_NAME = "82"
        const val ID_BATCH_NUMBER = "83"
        const val ID_RECEIPT_NUMBER = "84"

        const val ID_SCRT_EMVDATA_TAG_84 = "85"
        const val ID_SCRT_EMVDATA_TAG_95 = "86"
        const val ID_SCRT_EMVDATA_TAG_9F10 = "87"
        const val ID_SCRT_EMVDATA_TAG_9B = "88"
        const val ID_SCRT_EMVDATA_TAG_9F34_9F10 = "89"

        // Transaction data field names as constants
        const val ACQUIRER_MERCHANT_OUTLET_NAME = "Acquirer.Merchant.Outlet.Name"
        const val ACQUIRER_MERCHANT_OUTLET_TERMINAL_ADDRESS = "Acquirer.Merchant.Outlet.Terminal.Address"
        const val ACQUIRER_MERCHANT_OUTLET_NUMBER = "Acquirer.Merchant.Outlet.Number"
        const val ACQUIRER_MERCHANT_OUTLET_TERMINAL_NUMBER = "Acquirer.Merchant.Outlet.Terminal.Number"
        const val REF_REFERENCE = "REF.reference"
        const val REF_TRANSACTION_TYPE_WORDING = "REF.transactionType{getTransactionWording::}"
        const val REF_DATE_TIME_SUBSTRING_0_10 = "REF.dateTime{subString:0:10}"
        const val REF_TRANSACTION_TYPE_SCHEME = "REF.transactionType{getTransactionScheme::}"
        const val REF_DATE_TIME_SUBSTRING_11_19 = "REF.dateTime{subString:11:19}"
        const val CARD_NUMBER_HIDE = "Card.Number{hideCardNumber::}"
        const val CARD_SEQUENCE = "Card.Sequence"
        const val REF_TRX_AMOUNT = "REF.trxAmount"
        const val REF_CURRENCY_ISO_CODE_ALPHA = "REF.currencyIsoCode{currencyAlpha::}"
        const val REF_INTERCHANGE_FEE = "REF.interchangeFee"
        const val REF_AUTH_CODE = "REF.autCode"
        const val FOOTER_TICKET = "FOOTER_TICKET"
        const val SCRT_EMVDATA_APP_NAME = "SCRT.EMVDATA{getApplicationName:0:}"

        const val SCRT_EMVDATA_TAG_84 = "SCRT.EMVDATA{getEMVTag:84:}"
        const val SCRT_EMVDATA_TAG_95 = "SCRT.EMVDATA{getEMVTag:95:}"
        const val SCRT_EMVDATA_TAG_9F10 = "SCRT.EMVDATA{getEMVTag:9F10:}"
        const val SCRT_EMVDATA_TAG_9B = "SCRT.EMVDATA{getEMVTag:9B:}"
        const val SCRT_EMVDATA_TAG_9F34_9F10 = "SCRT.EMVDATA{requestSignature:9F34:9F10}"

        const val BATCH_NUMBER = "BatchNumber"
        const val RECEIPT_NUMBER = "ReceiptNumber"
    }

    /**
     * Parse an EMV QR code string
     * @param qrData The EMV QR code string to parse
     * @return The parsed EMV QR data
     */
    fun parse(qrData: String): Result<EmvQrData> {
        return runCatching {
            // Validate CRC
            val crcValid = validateCrc(qrData)

            Log.d("EmvQrParser", "crcValid $crcValid")
            // Parse TLV objects
            val tlvObjects = parseTlvObjects(qrData.substring(0, qrData.length - 4))
            
            // Extract root fields
            val payloadFormat = tlvObjects.find { it.id == ID_PAYLOAD_FORMAT }?.value ?: ""
            val initiationMethod = tlvObjects.find { it.id == ID_INITIATION_METHOD }?.value ?: ""
            
            // Extract merchant account info
            val merchantAccountInfoList = tlvObjects
                .filter { it.id.toInt() in ID_MERCHANT_ACCOUNT_INFO_START.toInt()..ID_MERCHANT_ACCOUNT_INFO_END.toInt() }
                .map { tlv ->
                    val subTlvs = parseTlvObjects(tlv.value)
                    MerchantAccount(
                        id = tlv.id,
                        name = getMerchantAccountName(tlv.id),
                        fields = subTlvs.associate { it.id to it.value }
                    )
                }
            
            // Extract currency and format it
            val currencyCode = tlvObjects.find { it.id == ID_CURRENCY }?.value ?: ""
            val currency = CurrencyMap.getCurrencyName(currencyCode)
            
            // Extract amount
            val amount = tlvObjects.find { it.id == ID_AMOUNT }?.value
            
            // Extract merchant details
            val merchantName = tlvObjects.find { it.id == ID_MERCHANT_NAME }?.value ?: ""
            val merchantCity = tlvObjects.find { it.id == ID_MERCHANT_CITY }?.value ?: ""
            val countryCode = tlvObjects.find { it.id == ID_COUNTRY_CODE }?.value ?: ""
            
            // Extract additional data
            val additionalDataTlv = tlvObjects.find { it.id == ID_ADDITIONAL_DATA }
            val additionalData = if (additionalDataTlv != null) {
                val additionalDataObjects = parseTlvObjects(additionalDataTlv.value)
                additionalDataObjects.associate { 
                    val fieldName = getAdditionalDataFieldName(it.id)
                    fieldName to it.value 
                }
            } else {
                emptyMap()
            }

            // Extract Unreserved Templates data
            val unrevetedTemplatesTlv = tlvObjects.find { it.id == FOOTER_TICKET }
            val unrevetedTemplatesData = if (unrevetedTemplatesTlv != null) {
                val unrevetedTemplatesObjects = parseTlvObjects(unrevetedTemplatesTlv.value)
                unrevetedTemplatesObjects.associate {
                    val fieldName = getUnreservedTemplatesDataFieldName(it.id)
                    fieldName to it.value
                }
            } else {
                emptyMap()
            }
            
            // Extract language template
            val languageTemplateTlv = tlvObjects.find { it.id == ID_LANGUAGE_TEMPLATE }
            val languageTemplate = if (languageTemplateTlv != null) {
                val languageTemplateObjects = parseTlvObjects(languageTemplateTlv.value)
                val languagePreference = languageTemplateObjects.find { it.id == ID_LANGUAGE_PREFERENCE }?.value ?: ""
                val langMerchantName = languageTemplateObjects.find { it.id == ID_LANGUAGE_MERCHANT_NAME }?.value
                val langMerchantCity = languageTemplateObjects.find { it.id == ID_LANGUAGE_MERCHANT_CITY }?.value
                
                val langAdditionalData = languageTemplateObjects
                    .filter { it.id.toInt() >= ID_LANGUAGE_RFU_START.toInt() && it.id.toInt() <= ID_LANGUAGE_RFU_END.toInt() }
                    .associate { it.id to it.value }
                
                LanguageTemplate(
                    languagePreference = languagePreference,
                    merchantName = langMerchantName,
                    merchantCity = langMerchantCity,
                    additionalData = if (langAdditionalData.isEmpty()) null else langAdditionalData
                )
            } else {
                null
            }
            
            // Create and return the EMV QR data object
            EmvQrData(
                payloadFormat = payloadFormat,
                initiationMethod = initiationMethod,
                merchantAccountInfo = merchantAccountInfoList,
                currency = currency,
                amount = amount,
                merchantName = merchantName,
                merchantCity = merchantCity,
                countryCode = countryCode,
                additionalData = additionalData,
                unreservedTemplates = unrevetedTemplatesData,
                languageTemplate = languageTemplate,
                crcValid = crcValid
            )
        }
    }

    /**
     * Parse TLV objects from a string
     * @param data The string to parse
     * @return A list of TLV objects
     */
    private fun parseTlvObjects(data: String): List<TlvObject> {
        val tlvObjects = mutableListOf<TlvObject>()
        var index = 0

        while (index < data.length) {
            // Parse ID (2 characters)
            if (index + 2 > data.length) break
            val id = data.substring(index, index + 2)
            Log.d("parseTlvObjects", "id $id")
            index += 2

            // Parse length (2 characters)
            if (index + 2 > data.length) break
            val length = data.substring(index, index + 2).toInt()
            Log.d("parseTlvObjects", "length $length")
            index += 2

            // Parse value (length characters)
            if (index + length > data.length) break
            val value = data.substring(index, index + length)
            Log.d("parseTlvObjects", "value $value")
            index += length

            // Add TLV object to list
            tlvObjects.add(TlvObject(id, length, value))
        }
        
        return tlvObjects
    }

    /**
     * Validate the CRC of an EMV QR code
     * @param qrData The EMV QR code string to validate
     * @return True if the CRC is valid, false otherwise
     */
    private fun validateCrc(qrData: String): Boolean {

        Log.e("validateCrc", "qrData.length ${qrData.length}")

        if (qrData.length < 4) return false

        val dataWithoutCrc = qrData.substring(0, qrData.length - 4)
        val providedCrc = qrData.substring(qrData.length - 4)

        Log.e("validateCrc", "dataWithoutCrc $dataWithoutCrc")
        Log.e("validateCrc", "providedCrc $providedCrc")


        val calculatedCrc = CrcCalculator.calculateCrc(dataWithoutCrc)

        Log.e("validateCrc", "calculatedCrc $calculatedCrc")

        return calculatedCrc.equals(providedCrc, ignoreCase = true)
    }

    /**
     * Get the name of a merchant account based on its ID
     * @param id The ID of the merchant account
     * @return The name of the merchant account
     */
    private fun getMerchantAccountName(id: String): String {
        return when (id) {
            "02" -> "Visa"
            "03" -> "Mastercard"
            "04" -> "Amex"
            "05" -> "JCB"
            "06" -> "UnionPay"
            "07" -> "Discover"
            "08" -> "Diners"
            "09" -> "Interac"
            "10" -> "Rupay"
            "11" -> "JCB"
            "12" -> "Mir"
            "13" -> "eftpos"
            "14" -> "Elcart"
            "15" -> "Girogo"
            "16" -> "Maestro UK"
            "17" -> "Maestro"
            "18" -> "Maestro International"
            "19" -> "Maestro Domestic"
            "20" -> "Mastercard Debit"
            "21" -> "Mastercard Credit"
            "22" -> "Visa Electron"
            "23" -> "Visa Debit"
            "24" -> "Visa Credit"
            "25" -> "Visa Dankort"
            "26" -> "Dankort"
            "27" -> "Bancontact"
            "28" -> "Girocard"
            "29" -> "GIM UEMOA"
            "30" -> "Meeza"
            "31" -> "Troy"
            else -> "Unknown Merchant Account ($id)"
        }
    }



    /**
     * Get the name of an additional data field based on its ID
     * @param id The ID of the additional data field
     * @return The name of the additional data field
     */
    private fun getAdditionalDataFieldName(id: String): String {
        return when (id) {
            ID_ADDITIONAL_BILL_NUMBER -> "Bill Number"
            ID_ADDITIONAL_MOBILE_NUMBER -> "Mobile Number"
            ID_ADDITIONAL_STORE_LABEL -> "Store Label"
            ID_ADDITIONAL_LOYALTY_NUMBER -> "Loyalty Number"
            ID_ADDITIONAL_REFERENCE_LABEL -> "Reference Label"
            ID_ADDITIONAL_CUSTOMER_LABEL -> "Customer Label"
            ID_ADDITIONAL_TERMINAL_LABEL -> "Terminal Label"
            ID_ADDITIONAL_PURPOSE_TRANSACTION -> "Purpose of Transaction"
            ID_ADDITIONAL_CONSUMER_DATA_REQUEST -> "Consumer Data Request"
            ID_SCRT_EMVDATA_TAG_84 -> "AID"
            else -> "Additional Field ($id)"
        }
    }

    private fun getUnreservedTemplatesDataFieldName(id: String): String {
        return when (id) {
            ID_SCRT_EMVDATA_TAG_84 -> "AID"
            ID_SCRT_EMVDATA_TAG_95 -> "TVR"
            else -> "Unreserved Templates ($id)"
        }
    }
    /**
     * Generate an EMV QR code string based on transaction data
     * @param transactionData Map of transaction data
     * @return The EMV QR code string
     */
    fun generate(transactionData: Map<String, String>): String {
        val qrCodeBuilder = StringBuilder()

        addField(qrCodeBuilder, ID_PAYLOAD_FORMAT, "01")
        addField(qrCodeBuilder, ID_INITIATION_METHOD, "11")

        addField(qrCodeBuilder, ID_MCC, transactionData[ACQUIRER_MERCHANT_OUTLET_NUMBER] ?: "5541")
        val currencyAlpha = transactionData[REF_CURRENCY_ISO_CODE_ALPHA] ?: "JOD"
        val currencyNumeric = convertCurrencyAlphaToNumeric(currencyAlpha)
        addField(qrCodeBuilder, ID_CURRENCY, currencyNumeric)

        val amount = transactionData[REF_TRX_AMOUNT] ?: "1"
        addField(qrCodeBuilder, ID_AMOUNT, formatAmount(amount, currencyAlpha))

        addField(qrCodeBuilder, ID_COUNTRY_CODE, "400")

        val merchantName = transactionData[ACQUIRER_MERCHANT_OUTLET_NAME] ?: "JORDAN GATE GAS STATION"
        addField(qrCodeBuilder, ID_MERCHANT_NAME, merchantName.substring(0, minOf(merchantName.length, 25)))

        val merchantAddress = transactionData[ACQUIRER_MERCHANT_OUTLET_TERMINAL_ADDRESS] ?: ""
        val city = if (merchantAddress.contains("-")) {
            merchantAddress.split("-").last().split(" ").first()
        } else {
            "AMMAN"
        }
        addField(qrCodeBuilder, ID_MERCHANT_CITY, city)

        val additionalDataBuilder = StringBuilder()

        val transactionDate = transactionData[REF_DATE_TIME_SUBSTRING_0_10] ?: ""
        if (transactionDate.isNotEmpty()) {
            val formattedDate = formatDate(transactionDate)
            addField(additionalDataBuilder, ID_ADDITIONAL_BILL_NUMBER, formattedDate)
        }

        val transactionTime = transactionData[REF_DATE_TIME_SUBSTRING_11_19] ?: ""
        if (transactionTime.isNotEmpty()) {
            val formattedTime = formatTime(transactionTime)
            addField(additionalDataBuilder, ID_ADDITIONAL_MOBILE_NUMBER, formattedTime)
        }

        val terminalNumber = transactionData[ACQUIRER_MERCHANT_OUTLET_TERMINAL_NUMBER] ?: ""
        if (terminalNumber.isNotEmpty()) {
            addField(additionalDataBuilder, ID_ADDITIONAL_STORE_LABEL, terminalNumber)
        }

        val referenceNumber = transactionData[REF_REFERENCE] ?: ""
        if (referenceNumber.isNotEmpty()) {
            addField(additionalDataBuilder, ID_ADDITIONAL_REFERENCE_LABEL, referenceNumber)
        }

        val authCode = transactionData[REF_AUTH_CODE] ?: ""
        if (authCode.isNotEmpty()) {
            addField(additionalDataBuilder, ID_ADDITIONAL_PURPOSE_TRANSACTION, authCode)
        }

        if (additionalDataBuilder.isNotEmpty()) {
            addField(qrCodeBuilder, ID_ADDITIONAL_DATA, additionalDataBuilder.toString())
        }

        val footerTicket = transactionData[FOOTER_TICKET] ?: ""
        if (footerTicket.isNotEmpty()) {
            addField(qrCodeBuilder, ID_FOOTER_TICKET, footerTicket)
        }

        val interchangeFee = transactionData[REF_INTERCHANGE_FEE] ?: ""
        if (interchangeFee.isNotEmpty()) {
            addField(qrCodeBuilder, ID_INTERCHANGE_FEE, interchangeFee)
        }

        val appName = transactionData[SCRT_EMVDATA_APP_NAME] ?: ""
        if (appName.isNotEmpty()) {
            addField(qrCodeBuilder, ID_APPLICATION_NAME, appName)
        }

        val emv_84 = transactionData[SCRT_EMVDATA_TAG_84] ?: ""
        if (emv_84.isNotEmpty()) {
            addField(qrCodeBuilder, ID_SCRT_EMVDATA_TAG_84, emv_84)
        }

        val emv_95 = transactionData[SCRT_EMVDATA_TAG_95] ?: ""
        if (emv_95.isNotEmpty()) {
            addField(qrCodeBuilder, ID_SCRT_EMVDATA_TAG_95, emv_95)
        }

        val emv_9B = transactionData[SCRT_EMVDATA_TAG_9B] ?: ""
        if (emv_9B.isNotEmpty()) {
            addField(qrCodeBuilder, ID_SCRT_EMVDATA_TAG_9B, emv_9B)
        }

        val emv_9F10 = transactionData[SCRT_EMVDATA_TAG_9F10] ?: ""
        if (emv_9F10.isNotEmpty()) {
            addField(qrCodeBuilder, ID_SCRT_EMVDATA_TAG_9F10, emv_9F10)
        }

        val emv_9F34_9F10 = transactionData[SCRT_EMVDATA_TAG_9F34_9F10] ?: ""
        if (emv_9F34_9F10.isNotEmpty()) {
            addField(qrCodeBuilder, ID_SCRT_EMVDATA_TAG_9F34_9F10, emv_9F34_9F10)
        }

        val batchNumber = transactionData[BATCH_NUMBER] ?: ""
        if (batchNumber.isNotEmpty()) {
            addField(qrCodeBuilder, ID_BATCH_NUMBER, batchNumber)
        }

        val receiptNumber = transactionData[RECEIPT_NUMBER] ?: ""
        if (receiptNumber.isNotEmpty()) {
            addField(qrCodeBuilder, ID_RECEIPT_NUMBER, receiptNumber)
        }

        val crc = calculateCrc(qrCodeBuilder.toString())
        addField(qrCodeBuilder, ID_CRC, crc)

        return qrCodeBuilder.toString()
    }
    /**
     * Add a field to the QR code string
     * @param builder The builder to add the field to
     * @param id The ID of the field
     * @param value The value of the field
     */
    private fun addField(builder: StringBuilder, id: String, value: String) {
        if (value.isEmpty()) return

        val length = String.format("%02d", value.length)
        builder.append(id).append(length).append(value)
    }

    /**
     * Format a date string from MM/DD/YYYY to YYMMDD
     * @param dateString The date string to format (MM/DD/YYYY)
     * @return The formatted date string (YYMMDD)
     */
    private fun formatDate(dateString: String): String {
        // Assuming input format is MM/DD/YYYY or similar
        val parts = dateString.split("/")
        if (parts.size >= 3) {
            val year = parts[2].substring(2, 4) // Get last 2 digits of year
            val month = parts[0].padStart(2, '0')
            val day = parts[1].padStart(2, '0')
            return year + month + day
        }
        return ""
    }

    /**
     * Format a time string from HH:MM:SS to HHMMSS
     * @param timeString The time string to format (HH:MM:SS)
     * @return The formatted time string (HHMMSS)
     */
    private fun formatTime(timeString: String): String {
        // Remove colons from time string
        return timeString.replace(":", "")
    }

    /**
     * Format an amount for the QR code
     * @param amount The amount to format
     * @param currencyCode The currency code (JOD, USD, etc.)
     * @return The formatted amount
     */
    private fun formatAmount(amount: String, currencyCode: String): String {
        // Convert to minor units based on currency
        val amountValue = amount.toDoubleOrNull() ?: 0.0

        val minorUnits = when (currencyCode) {
            "JOD" -> 1000 // 1 JOD = 1000 fils
            "USD", "EUR" -> 100 // 1 USD/EUR = 100 cents
            else -> 100 // Default to 2 decimal places
        }

        val amountInMinorUnits = (amountValue * minorUnits).toLong()
        return amountInMinorUnits.toString()
    }

    /**
     * Convert currency alpha code to numeric ISO 4217 code
     * @param alpha The alpha currency code (e.g., JOD)
     * @return The numeric currency code (e.g., 400)
     */
    private fun convertCurrencyAlphaToNumeric(alpha: String): String {
        return when (alpha) {
            "JOD" -> "400"
            "USD" -> "840"
            "EUR" -> "978"
            "SAR" -> "682"
            "AED" -> "784"
            "GBP" -> "826"
            else -> "400" // Default to JOD if unknown
        }
    }

    /**
     * Calculate CRC-16 checksum for EMV QR code
     * @param data The data to calculate the CRC for
     * @return The calculated CRC as a 4-character hexadecimal string
     */
    private fun calculateCrc(data: String): String {
        // Create the data with CRC field ID and length placeholder
        val dataWithCrcField = data + ID_CRC + "04"

        // CRC-16 CCITT (0x1021) calculation
        val polynomial = 0x1021
        var crc = 0xFFFF

        for (character in dataWithCrcField) {
            crc = crc xor (character.code shl 8)

            for (i in 0 until 8) {
                crc = if (crc and 0x8000 != 0) {
                    (crc shl 1) xor polynomial
                } else {
                    crc shl 1
                }
                crc = crc and 0xFFFF
            }
        }

        // Return CRC as 4-character uppercase hex string
        return String.format("%04X", crc)
    }
}
