package ma.xpi.tlvtools.model

import org.json.JSONArray
import org.json.JSONObject

/**
 * Data class representing the parsed EMV QR code data
 */
data class EmvQrData(
    val payloadFormat: String,
    val initiationMethod: String,
    val merchantAccountInfo: List<MerchantAccount>,
    val currency: String,
    val amount: String?,
    val merchantName: String,
    val merchantCity: String,
    val countryCode: String,
    val additionalData: Map<String, String>,
    val unreservedTemplates : Map<String, String>,
    val languageTemplate: LanguageTemplate? = null,
    val crcValid: Boolean
) {
    /**
     * Convert the EmvQrData object to a JSON string
     * @return JSON string representation of the EmvQrData object
     */
    fun toJson(): String {
        val json = JSONObject()
        
        // Add basic fields
        json.put("payloadFormat", payloadFormat)
        json.put("initiationMethod", initiationMethod)
        json.put("currency", currency)
        json.put("amount", amount ?: JSONObject.NULL)
        json.put("merchantName", merchantName)
        json.put("merchantCity", merchantCity)
        json.put("countryCode", countryCode)
        json.put("crcValid", crcValid)
        
        // Add merchant account info
        val merchantAccountsJson = JSONArray()
        for (account in merchantAccountInfo) {
            val accountJson = JSONObject()
            accountJson.put("id", account.id)
            accountJson.put("name", account.name ?: JSONObject.NULL)
            
            // Add account fields
            val fieldsJson = JSONObject()
            for ((key, value) in account.fields) {
                fieldsJson.put(key, value)
            }
            accountJson.put("fields", fieldsJson)
            
            merchantAccountsJson.put(accountJson)
        }
        json.put("merchantAccountInfo", merchantAccountsJson)
        
        // Add additional data
        val additionalDataJson = JSONObject()
        for ((key, value) in additionalData) {
            additionalDataJson.put(key, value)
        }
        json.put("additionalData", additionalDataJson)
        
        // Add language template if present
        if (languageTemplate != null) {
            val languageTemplateJson = JSONObject()
            languageTemplateJson.put("languagePreference", languageTemplate.languagePreference)
            languageTemplateJson.put("merchantName", languageTemplate.merchantName ?: JSONObject.NULL)
            languageTemplateJson.put("merchantCity", languageTemplate.merchantCity ?: JSONObject.NULL)
            
            // Add language template additional data if present
            if (languageTemplate.additionalData != null) {
                val langAdditionalDataJson = JSONObject()
                for ((key, value) in languageTemplate.additionalData) {
                    langAdditionalDataJson.put(key, value)
                }
                languageTemplateJson.put("additionalData", langAdditionalDataJson)
            } else {
                languageTemplateJson.put("additionalData", JSONObject.NULL)
            }
            
            json.put("languageTemplate", languageTemplateJson)
        } else {
            json.put("languageTemplate", JSONObject.NULL)
        }
        
        return json.toString(2) // Pretty print with 2-space indentation
    }
}

/**
 * Data class representing merchant account information
 */
data class MerchantAccount(
    val id: String,
    val name: String?,
    val fields: Map<String, String>
) {
    /**
     * Convert the MerchantAccount object to a JSON string
     * @return JSON string representation of the MerchantAccount object
     */
    fun toJson(): String {
        val json = JSONObject()
        json.put("id", id)
        json.put("name", name ?: JSONObject.NULL)
        
        val fieldsJson = JSONObject()
        for ((key, value) in fields) {
            fieldsJson.put(key, value)
        }
        json.put("fields", fieldsJson)
        
        return json.toString(2)
    }
}

/**
 * Data class representing language template information
 */
data class LanguageTemplate(
    val languagePreference: String,
    val merchantName: String?,
    val merchantCity: String?,
    val additionalData: Map<String, String>?
) {
    /**
     * Convert the LanguageTemplate object to a JSON string
     * @return JSON string representation of the LanguageTemplate object
     */
    fun toJson(): String {
        val json = JSONObject()
        json.put("languagePreference", languagePreference)
        json.put("merchantName", merchantName ?: JSONObject.NULL)
        json.put("merchantCity", merchantCity ?: JSONObject.NULL)
        
        if (additionalData != null) {
            val additionalDataJson = JSONObject()
            for ((key, value) in additionalData) {
                additionalDataJson.put(key, value)
            }
            json.put("additionalData", additionalDataJson)
        } else {
            json.put("additionalData", JSONObject.NULL)
        }
        
        return json.toString(2)
    }
}

/**
 * Data class representing a Tag-Length-Value (TLV) object
 */
data class TlvObject(
    val id: String,
    val length: Int,
    val value: String,
    val children: List<TlvObject> = emptyList()
)
