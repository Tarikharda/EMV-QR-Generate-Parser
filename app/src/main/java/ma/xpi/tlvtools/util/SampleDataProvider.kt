package ma.xpi.tlvtools.util

/**
 * Utility class for providing sample EMV QR code data
 */
object SampleDataProvider {
    
    /**
     * Get sample EMV QR code data from Annex B of the EMVCo specification
     * @return The sample EMV QR code data
     */
    fun getAnnexBSampleData(): String {
        // This is a sample EMV QR code from Annex B of the EMVCo QR Code Specification
        return "00020101021229300012D156000000000510A93FO3230Q31280012D15600000001030812345678520441115802CN5914BEST TRANSPORT6007BEIJING64200002ZH0104最佳运输0202北京540523.7253031565502016233030412340603***0708A60086670902ME91320016A0112233449988770708123456786304A13A"
    }
    
    /**
     * Get a more complex sample EMV QR code with additional fields
     * @return The complex sample EMV QR code data
     */
    fun getComplexSampleData(): String {
        // This is a more complex sample with additional fields
        return "00020101021229300012D156000000000510A93FO3230Q31280012D15600000001030812345678520441115802CN5914BEST TRANSPORT6007BEIJING64200002ZH0104最佳运输0202北京540523.725303156550201623303041234060398765432106304A13A0708A6008667090211ME0102ID5204987605120454455253039586802SG7004ABCD63046CC3"
    }
    
    /**
     * Get a sample EMV QR code with language template
     * @return The sample EMV QR code data with language template
     */
    fun getSampleWithLanguageTemplate(): String {
        // This is a sample with language template
        return "00020101021229300012D156000000000510A93FO3230Q31280012D15600000001030812345678" +
               "5204ABCD5303156540523.725502015802CN5914BEST TRANSPORT6007BEIJING61071234567624" +
               "0001256304A13A64280002ZH0104最佳运输0202北京62800003JA0104ベスト輸送0202東京63044CDF"
    }
}
