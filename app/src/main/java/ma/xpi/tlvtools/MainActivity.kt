package ma.xpi.tlvtools

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import ma.xpi.tlvtools.model.EmvQrData
import ma.xpi.tlvtools.model.LanguageTemplate
import ma.xpi.tlvtools.model.MerchantAccount
import ma.xpi.tlvtools.parser.EmvQrParser
import ma.xpi.tlvtools.parser.EmvQrParser.Companion.ACQUIRER_MERCHANT_OUTLET_NAME
import ma.xpi.tlvtools.parser.EmvQrParser.Companion.ACQUIRER_MERCHANT_OUTLET_NUMBER
import ma.xpi.tlvtools.parser.EmvQrParser.Companion.ACQUIRER_MERCHANT_OUTLET_TERMINAL_ADDRESS
import ma.xpi.tlvtools.parser.EmvQrParser.Companion.ACQUIRER_MERCHANT_OUTLET_TERMINAL_NUMBER
import ma.xpi.tlvtools.parser.EmvQrParser.Companion.CARD_NUMBER_HIDE
import ma.xpi.tlvtools.parser.EmvQrParser.Companion.CARD_SEQUENCE
import ma.xpi.tlvtools.parser.EmvQrParser.Companion.REF_DATE_TIME_SUBSTRING_0_10
import ma.xpi.tlvtools.parser.EmvQrParser.Companion.REF_DATE_TIME_SUBSTRING_11_19
import ma.xpi.tlvtools.parser.EmvQrParser.Companion.REF_REFERENCE
import ma.xpi.tlvtools.parser.EmvQrParser.Companion.REF_TRANSACTION_TYPE_SCHEME
import ma.xpi.tlvtools.parser.EmvQrParser.Companion.REF_TRANSACTION_TYPE_WORDING
import ma.xpi.tlvtools.parser.EmvQrParser.Companion.REF_TRX_AMOUNT
import ma.xpi.tlvtools.parser.EmvQrParser.Companion.REF_CURRENCY_ISO_CODE_ALPHA
import ma.xpi.tlvtools.parser.EmvQrParser.Companion.REF_INTERCHANGE_FEE
import ma.xpi.tlvtools.parser.EmvQrParser.Companion.REF_AUTH_CODE
import ma.xpi.tlvtools.parser.EmvQrParser.Companion.FOOTER_TICKET
import ma.xpi.tlvtools.parser.EmvQrParser.Companion.SCRT_EMVDATA_APP_NAME
import ma.xpi.tlvtools.parser.EmvQrParser.Companion.BATCH_NUMBER
import ma.xpi.tlvtools.parser.EmvQrParser.Companion.RECEIPT_NUMBER
import ma.xpi.tlvtools.parser.EmvQrParser.Companion.SCRT_EMVDATA_TAG_84
import ma.xpi.tlvtools.parser.EmvQrParser.Companion.SCRT_EMVDATA_TAG_95
import ma.xpi.tlvtools.parser.EmvQrParser.Companion.SCRT_EMVDATA_TAG_9B
import ma.xpi.tlvtools.parser.EmvQrParser.Companion.SCRT_EMVDATA_TAG_9F10
import ma.xpi.tlvtools.parser.EmvQrParser.Companion.SCRT_EMVDATA_TAG_9F34_9F10
import ma.xpi.tlvtools.ui.theme.TLVtoolsTheme
import ma.xpi.tlvtools.util.CurrencyMap
import ma.xpi.tlvtools.util.MccMap
import ma.xpi.tlvtools.util.SampleDataProvider

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TLVtoolsTheme {
                EmvQrParserApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmvQrParserApp() {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    var qrData by remember { mutableStateOf("") }
    var parsedData by remember { mutableStateOf<EmvQrData?>(null) }
    var parseError by remember { mutableStateOf<String?>(null) }
    var showJsonDialog by remember { mutableStateOf(false) }
    var showQRCodeDialog by remember { mutableStateOf(false) }
    var jsonContent by remember { mutableStateOf("") }
    
    val parser = remember { EmvQrParser() }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("EMV QR Parser") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Input section
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Input EMV QR Data",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    OutlinedTextField(
                        value = qrData,
                        onValueChange = { qrData = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Raw EMV QR Data") },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        maxLines = 5
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (qrData.isBlank()) {
                                    parseError = "Please enter QR data"
                                    parsedData = null
                                    return@Button
                                }
                                
                                parser.parse(qrData)
                                    .onSuccess {
                                        parsedData = it
                                        parseError = null
                                    }
                                    .onFailure {
                                        parseError = "Error parsing QR data: ${it.message}"
                                        parsedData = null
                                    }
                                
                                focusManager.clearFocus()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Parse")
                        }
                        
                        Button(
                            onClick = {
                                //Here should generate QR Code
                                //qrData = SampleDataProvider.getAnnexBSampleData()
                                val transactionData = mapOf(
                                    ACQUIRER_MERCHANT_OUTLET_NAME to "Ticket System",
                                    ACQUIRER_MERCHANT_OUTLET_TERMINAL_ADDRESS to "JORDAN GATE GAS STATION - AMMAN JO",
                                    ACQUIRER_MERCHANT_OUTLET_NUMBER to "71002374",
                                    ACQUIRER_MERCHANT_OUTLET_TERMINAL_NUMBER to "71002374",
                                    REF_REFERENCE to "504217190001",
                                    REF_TRANSACTION_TYPE_WORDING to "PURCHASE",
                                    REF_DATE_TIME_SUBSTRING_0_10 to "250211", // Corrected format to YYMMDD
                                    REF_TRANSACTION_TYPE_SCHEME to "CSC",
                                    REF_DATE_TIME_SUBSTRING_11_19 to "172441", // Corrected format to HHMMSS
                                    CARD_NUMBER_HIDE to "402**0093",
                                    CARD_SEQUENCE to "01",
                                    REF_TRX_AMOUNT to "1000", // Converted 1 JOD to minor units (fils)
                                    REF_CURRENCY_ISO_CODE_ALPHA to "JOD",
                                    REF_INTERCHANGE_FEE to "0.00",
                                    REF_AUTH_CODE to "418448",
                                    FOOTER_TICKET to "Merchant",
                                    SCRT_EMVDATA_APP_NAME to "QR Scanner",
                                    SCRT_EMVDATA_TAG_84 to "01108000102A0200",
                                    SCRT_EMVDATA_TAG_95 to "0000000000",
                                    SCRT_EMVDATA_TAG_9B to "01108000102A020",
                                    SCRT_EMVDATA_TAG_9F10 to "01108000102A0200000000000000000000FF",
                                    SCRT_EMVDATA_TAG_9F34_9F10 to "1F0002",
                                    BATCH_NUMBER to "3",
                                    RECEIPT_NUMBER to "135"
                                )
                                qrData = parser.generate(transactionData);

                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Generate Qr Code")
                        }
                    }
                    
                    // Additional sample buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                qrData = SampleDataProvider.getComplexSampleData()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Complex Example")
                        }
                        
                        Button(
                            onClick = {
                                qrData = SampleDataProvider.getSampleWithLanguageTemplate()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Language Example")
                        }
                    }
                    
                    if (parseError != null) {
                        Text(
                            text = parseError!!,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            
            // Results section
            if (parsedData != null) {
                ResultsSection(
                    data = parsedData!!, 
                    context = context,
                    onShowJson = {
                        jsonContent = parsedData!!.toJson()
                        showJsonDialog = true
                    },
                    onShowQrCode = {
                        showQRCodeDialog = true
                    }
                )
                
                // JSON Dialog
                if (showJsonDialog) {
                    AlertDialog(
                        onDismissRequest = { showJsonDialog = false },
                        title = { Text("JSON Data") },
                        text = {
                            Box(
                                modifier = Modifier
                                    .verticalScroll(rememberScrollState())
                                    .fillMaxWidth()
                            ) {
                                Text(jsonContent)
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("JSON Data", jsonContent)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "JSON copied to clipboard", Toast.LENGTH_SHORT).show()
                                showJsonDialog = false
                            }) {
                                Text("Copy & Close")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showJsonDialog = false }) {
                                Text("Close")
                            }
                        }
                    )
                }

                // JSON Dialog
                if (showQRCodeDialog) {
                    AlertDialog(
                        onDismissRequest = { showQRCodeDialog = false },
                        title = { Text("QR Code") },
                        text = {
                            Box(
                                modifier = Modifier .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                val barcodeEncoder = BarcodeEncoder();
                                val bitmap = barcodeEncoder.encodeBitmap(qrData, BarcodeFormat.QR_CODE, 500, 500);
                                Image(
                                    bitmap = bitmap.asImageBitmap(),

                                    contentDescription = "QR Code"
                                )
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("JSON Data", jsonContent)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "JSON copied to clipboard", Toast.LENGTH_SHORT).show()
                                showJsonDialog = false
                            }) {
                                Text("Share")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showJsonDialog = false }) {
                                Text("Close")
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ResultsSection(data: EmvQrData, context: Context, onShowJson: () -> Unit = {} , onShowQrCode: () -> Unit = {}) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Parsed QR Data",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            // JSON Button
            Button(
                onClick = onShowJson,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("View as JSON")
            }

            Button(
                onClick = onShowQrCode,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("View Generated QR Code")
            }

            // CRC Validation Status
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (data.crcValid) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(8.dp)
            ) {
                Text(
                    text = if (data.crcValid) "✓ CRC Validation Passed" else "✗ CRC Validation Failed",
                    color = if (data.crcValid) Color(0xFF2E7D32) else Color(0xFFC62828),
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Basic Information
            ExpandableSection(title = "Basic Information") {
                DataField("Payload Format", data.payloadFormat)
                DataField("Initiation Method", 
                    when(data.initiationMethod) {
                        "11" -> "Static QR Code"
                        "12" -> "Dynamic QR Code"
                        else -> data.initiationMethod
                    }
                )
            }
            
            // Merchant Information
            ExpandableSection(title = "Merchant Information") {
                DataField("Merchant Name", data.merchantName)
                DataField("Merchant City", data.merchantCity)
                DataField("Country Code", data.countryCode)
            }
            
            // Transaction Details
            ExpandableSection(title = "Transaction Details") {
                DataField("Currency", data.currency)
                if (data.amount != null) {
                    val currencyCode = data.currency.split(" - ").firstOrNull()?.trim() ?: ""
                    val currencySymbol = CurrencyMap.getCurrencySymbol(currencyCode.takeLast(3))
                    DataField("Amount", "$currencySymbol ${data.amount}")
                } else {
                    DataField("Amount", "Not specified")
                }
            }
            
            // Merchant Account Information
            if (data.merchantAccountInfo.isNotEmpty()) {
                ExpandableSection(title = "Merchant Account Information") {
                    data.merchantAccountInfo.forEach { account ->
                        Text(
                            text = "${account.name} (ID: ${account.id})",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        
                        if (account.fields.isNotEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                account.fields.forEach { (key, value) ->
                                    DataField("Field $key", value)
                                }
                            }
                        } else {
                            Text(
                                text = "No fields",
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                        
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                }
            }
            
            // Additional Data
            if (data.additionalData.isNotEmpty()) {
                ExpandableSection(title = "Additional Data") {
                    data.additionalData.forEach { (key, value) ->
                        DataField(key, value)
                    }
                }
            }
            
            // Language Template
            if (data.languageTemplate != null) {
                ExpandableSection(title = "Language Template (${data.languageTemplate.languagePreference})") {
                    data.languageTemplate.merchantName?.let {
                        DataField("Merchant Name", it)
                    }
                    
                    data.languageTemplate.merchantCity?.let {
                        DataField("Merchant City", it)
                    }
                    
                    data.languageTemplate.additionalData?.forEach { (key, value) ->
                        DataField("Additional Field $key", value)
                    }
                }
            }
        }
    }
}

@Composable
fun ExpandableSection(
    title: String,
    initiallyExpanded: Boolean = true,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(initiallyExpanded) }
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = if (expanded) "Collapse" else "Expand"
            )
        }
        
        if (expanded) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    content()
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun DataField(label: String, value: String, copyable: Boolean = true) {
    val context = LocalContext.current
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "$label:",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(0.35f)
        )
        
        Row(
            modifier = Modifier.weight(0.65f),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = value,
                modifier = Modifier.weight(1f),
                overflow = TextOverflow.Ellipsis
            )
            
            if (copyable) {
                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText(label, value)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Copy to clipboard",
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EmvQrParserAppPreview() {
    TLVtoolsTheme {
        EmvQrParserApp()
    }
}

@Preview(showBackground = true)
@Composable
fun ResultsSectionPreview() {
    val sampleData = EmvQrData(
        payloadFormat = "01",
        initiationMethod = "11",
        merchantAccountInfo = listOf(
            MerchantAccount(
                id = "02",
                name = "Visa",
                fields = mapOf("01" to "12345", "02" to "67890")
            )
        ),
        currency = "USD - US Dollar ($)",
        amount = "10.99",
        merchantName = "Sample Merchant",
        merchantCity = "Sample City",
        countryCode = "US",
        additionalData = mapOf(
            "Bill Number" to "123456",
            "Reference Label" to "REF123"
        ),
        unreservedTemplates = mapOf(
            "01" to "Sample Template 1",
            "02" to "Sample Template 2"
        ),
        languageTemplate = LanguageTemplate(
            languagePreference = "ZH",
            merchantName = "示例商家",
            merchantCity = "示例城市",
            additionalData = null
        ),
        crcValid = true
    )
    
    TLVtoolsTheme {
        Surface {
            ResultsSection(sampleData, LocalContext.current)
        }
    }
}