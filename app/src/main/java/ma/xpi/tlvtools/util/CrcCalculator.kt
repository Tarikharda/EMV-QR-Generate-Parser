package ma.xpi.tlvtools.util

/**
 * Utility class for calculating CRC-16 checksums according to ISO/IEC 13239 (polynomial 0x1021)
 */
object CrcCalculator {
    // CRC-16 CCITT polynomial: x^16 + x^12 + x^5 + 1 (0x1021)
    private const val POLYNOMIAL = 0x1021
    
    /**
     * Calculate the CRC-16 checksum for the given data
     * @param data The data to calculate the CRC for
     * @return The CRC-16 checksum as a 4-character hexadecimal string
     */
    fun calculateCrc(data: String): String {
        val bytes = data.toByteArray(Charsets.UTF_8)
        var crc = 0xFFFF // Initial value
        
        for (byte in bytes) {
            crc = crc xor (byte.toInt() and 0xFF shl 8)
            
            for (i in 0 until 8) {
                crc = if (crc and 0x8000 != 0) {
                    (crc shl 1) xor POLYNOMIAL
                } else {
                    crc shl 1
                }
                crc = crc and 0xFFFF // Keep only 16 bits
            }
        }
        
        return String.format("%04X", crc)
    }
}
