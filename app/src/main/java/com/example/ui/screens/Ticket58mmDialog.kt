package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.util.QrCodeUtils
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun Ticket58mmDialog(
    schoolName: String,
    matricule: String,
    studentName: String,
    studentGrade: String,
    totalPaid: Long,
    remaining: Long,
    onDismiss: () -> Unit,
    onPrint: () -> Unit = {}
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = Color.White,
            modifier = Modifier
                .padding(16.dp)
                // 58mm printer receipt is typically very narrow, around 384 pixels width max.
                // We emulate this visually using a specific width in dp
                .width(250.dp) 
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // TICKET DESIGN
                Text(
                    text = schoolName.ifBlank { "ScolaPay" }.uppercase(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    fontFamily = FontFamily.Monospace,
                    color = Color.Black
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "REÇU DE PAIEMENT",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color.Black
                )
                
                Divider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = Color.Black,
                    thickness = 1.dp
                )
                
                val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("fr", "GN"))
                val dateStr = sdf.format(Date())
                
                Text(
                    text = "Date: $dateStr",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start,
                    color = Color.Black
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "Matricule: #$matricule",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start,
                    color = Color.Black
                )
                
                Text(
                    text = "Elève: $studentName",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start,
                    color = Color.Black
                )
                
                Text(
                    text = "Classe: $studentGrade",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start,
                    color = Color.Black
                )
                
                Divider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = Color.Black,
                    thickness = 1.dp
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "TOTAL PAYÉ",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Color.Black
                    )
                    Text(
                        text = "${java.text.NumberFormat.getInstance(Locale("fr", "GN")).format(totalPaid)} GNF",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Color.Black
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "RESTE À PAYER",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color.Black
                    )
                    Text(
                        text = "${java.text.NumberFormat.getInstance(Locale("fr", "GN")).format(remaining)} GNF",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color.Black
                    )
                }
                
                Divider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = Color.Black,
                    thickness = 1.dp
                )
                
                // QR CODE GENERATION
                val qrData = remember(matricule, studentName) {
                    "SCOLA_STUDENT:matricule=$matricule;name=$studentName"
                }
                val qrBitmap = remember(qrData) {
                    QrCodeUtils.generateQrBitmap(qrData, 260)
                }

                if (qrBitmap != null) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "Code QR Reçu",
                        modifier = Modifier
                            .size(110.dp)
                            .padding(vertical = 4.dp)
                    )
                    Text(
                        text = "#$matricule",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                
                Text(
                    text = "Merci pour votre confiance.",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center,
                    color = Color.Black
                )
                Text(
                    text = "Généré par ScolaPay",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center,
                    color = Color.Black
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = onPrint,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F56E3))
                ) {
                    Text("Imprimer Ticket", fontSize = 12.sp, color = Color.White)
                }
                
                TextButton(onClick = onDismiss) {
                    Text("Fermer", color = Color.Gray)
                }
            }
        }
    }
}
