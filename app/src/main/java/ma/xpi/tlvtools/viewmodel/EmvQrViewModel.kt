package ma.xpi.tlvtools.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ma.xpi.tlvtools.model.EmvQrData
import ma.xpi.tlvtools.parser.EmvQrParser
import ma.xpi.tlvtools.util.SampleDataProvider

/**
 * ViewModel for the EMV QR Parser screen
 */
class EmvQrViewModel : ViewModel() {
    private val parser = EmvQrParser()
    
    // UI state
    private val _qrData = MutableStateFlow("")
    val qrData: StateFlow<String> = _qrData.asStateFlow()
    
    private val _parsedData = MutableStateFlow<EmvQrData?>(null)
    val parsedData: StateFlow<EmvQrData?> = _parsedData.asStateFlow()
    
    private val _parseError = MutableStateFlow<String?>(null)
    val parseError: StateFlow<String?> = _parseError.asStateFlow()
    
    private val _showJsonDialog = MutableStateFlow(false)
    val showJsonDialog: StateFlow<Boolean> = _showJsonDialog.asStateFlow()
    
    private val _jsonContent = MutableStateFlow("")
    val jsonContent: StateFlow<String> = _jsonContent.asStateFlow()
    
    /**
     * Update the QR data input
     */
    fun updateQrData(data: String) {
        _qrData.value = data
    }
    
    /**
     * Parse the QR data
     */
    fun parseQrData() {
        viewModelScope.launch {
            if (_qrData.value.isBlank()) {
                _parseError.value = "Please enter QR data"
                _parsedData.value = null
                return@launch
            }
            
            parser.parse(_qrData.value)
                .onSuccess {
                    _parsedData.value = it
                    _parseError.value = null
                }
                .onFailure {
                    _parseError.value = "Error parsing QR data: ${it.message}"
                    _parsedData.value = null
                }
        }
    }
    
    /**
     * Load sample data from Annex B
     */
    fun loadAnnexBSample() {
        _qrData.value = SampleDataProvider.getAnnexBSampleData()
    }
    
    /**
     * Load complex sample data
     */
    fun loadComplexSample() {
        _qrData.value = SampleDataProvider.getComplexSampleData()
    }
    
    /**
     * Load sample with language template
     */
    fun loadLanguageTemplateSample() {
        _qrData.value = SampleDataProvider.getSampleWithLanguageTemplate()
    }
    
    /**
     * Show JSON dialog with the parsed data
     */
    fun showJsonData() {
        _parsedData.value?.let {
            _jsonContent.value = it.toJson()
            _showJsonDialog.value = true
        }
    }
    
    /**
     * Dismiss the JSON dialog
     */
    fun dismissJsonDialog() {
        _showJsonDialog.value = false
    }
}
