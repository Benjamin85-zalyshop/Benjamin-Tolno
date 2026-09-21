package com.example.ui.printer

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.example.data.models.Payment
import com.example.data.models.Student
import com.example.ui.util.QrCodeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Type de terminal et format de papier :
 * - POS_58MM : Terminal Mobile POS de poche (ex: Sunmi V2, Z90, imprimantes thermiques 58mm). 32 colonnes.
 * - DESKTOP_80MM : Caisse de Comptoir / Bureau (ex: Senraise, caisses tactiles 80mm). 48 colonnes.
 */
enum class PrinterTerminalType(val label: String, val paperWidthMm: Int, val maxCharsPerLine: Int, val maxImageWidth: Int) {
    POS_58MM("Terminal Mobile POS (58 mm)", 58, 32, 384),
    DESKTOP_80MM("Caisse Comptoir / Bureau (80 mm)", 80, 48, 576)
}

object EscPosTicketBuilder {

    private const val CHARSET = "UTF-8"

    fun buildPaymentReceipt(
        terminalType: PrinterTerminalType,
        schoolName: String,
        student: Student,
        payment: Payment,
        classFee: Long,
        currency: String = "GNF",
        schoolLogo: Bitmap? = null
    ): ByteArray {
        val bos = ByteArrayOutputStream()
        val cols = terminalType.maxCharsPerLine

        fun write(bytes: ByteArray) = bos.write(bytes)
        fun writeText(text: String) = bos.write(text.toByteArray(charset(CHARSET)))
        fun feed(n: Int = 1) {
            repeat(n) { bos.write(EscPosPrinterUtils.printLineFeed()) }
        }

        fun drawDivider(char: Char = '-') {
            writeText(char.toString().repeat(cols) + "\n")
        }

        fun drawTwoColumns(left: String, right: String) {
            val spaceNeeded = cols - left.length - right.length
            if (spaceNeeded > 0) {
                writeText(left + " ".repeat(spaceNeeded) + right + "\n")
            } else {
                writeText("$left $right\n")
            }
        }

        try {
            // Initialisation de l'imprimante
            write(EscPosPrinterUtils.initPrinter())

            // 1. Logo si présent
            if (schoolLogo != null) {
                write(EscPosPrinterUtils.alignCenter())
                val logoBytes = EscPosPrinterUtils.decodeBitmap(schoolLogo, if (terminalType == PrinterTerminalType.POS_58MM) 200 else 300)
                write(logoBytes)
                feed(1)
            }

            // 2. En-tête : Nom de l'établissement
            write(EscPosPrinterUtils.alignCenter())
            write(EscPosPrinterUtils.emphasizedOn())
            write(EscPosPrinterUtils.fontSizeSetBig(1))
            writeText(schoolName.ifBlank { "ScolaPay" }.uppercase() + "\n")
            write(EscPosPrinterUtils.fontSizeSetBig(0))
            write(EscPosPrinterUtils.emphasizedOff())

            writeText("REÇU OFFICIEL DE PAIEMENT\n")
            drawDivider('=')

            // 3. Infos de base
            write(EscPosPrinterUtils.alignLeft())
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val dateStr = sdf.format(Date(payment.date))
            val matricule = if (student.remoteId.length >= 5) student.remoteId.take(5).uppercase() else student.id.toString()

            drawTwoColumns("Date :", dateStr)
            drawTwoColumns("Reçu N° :", "#${payment.id}")
            drawTwoColumns("Matricule :", "#$matricule")
            drawTwoColumns("Élève :", "${student.firstName} ${student.lastName}".take(cols - 8))
            drawTwoColumns("Classe :", student.grade)
            if (student.section.isNotBlank()) {
                drawTwoColumns("Section :", student.section)
            }

            drawDivider('-')

            // 4. Détails financiers
            val fmt = NumberFormat.getInstance(Locale("fr", "GN"))
            write(EscPosPrinterUtils.emphasizedOn())
            drawTwoColumns("MONTANT VERSÉ :", "${fmt.format(payment.amount)} $currency")
            write(EscPosPrinterUtils.emphasizedOff())
            drawTwoColumns("Motif :", payment.reason)
            drawTwoColumns("Mode de règlement :", payment.paymentMethod)

            if (classFee > 0L) {
                drawDivider('-')
                drawTwoColumns("Scolarité totale :", "${fmt.format(classFee)} $currency")
            }

            drawDivider('=')

            // 5. QR Code pour consultation du dossier par les parents
            val qrData = QrCodeUtils.buildStudentQrData(
                studentId = student.id,
                remoteId = student.remoteId,
                name = "${student.firstName} ${student.lastName}",
                grade = student.grade,
                section = student.section,
                paidFee = fmt.format(payment.amount),
                schoolName = schoolName
            )
            val qrSize = if (terminalType == PrinterTerminalType.POS_58MM) 200 else 280
            val qrBitmap = QrCodeUtils.generateQrBitmap(qrData, qrSize)
            if (qrBitmap != null) {
                write(EscPosPrinterUtils.alignCenter())
                val qrBytes = EscPosPrinterUtils.decodeBitmap(qrBitmap, qrSize)
                write(qrBytes)
                feed(1)
                writeText("Scannez pour vérifier l'authenticité\n")
            }

            // 6. Bas de ticket
            write(EscPosPrinterUtils.alignCenter())
            writeText("Merci pour votre confiance.\n")
            writeText("Généré par ScolaPay\n")

            // Fin de papier et découpe
            write(EscPosPrinterUtils.printAndFeedLines(if (terminalType == PrinterTerminalType.POS_58MM) 3 else 4))
            write(EscPosPrinterUtils.feedPaperCutPartial())

        } catch (e: Exception) {
            Log.e("EscPosTicketBuilder", "Erreur lors de la construction du ticket", e)
        }

        return bos.toByteArray()
    }
}

/**
 * Service de communication Bluetooth directe pour imprimantes ESC/POS.
 */
object BluetoothPrinterManager {

    private const val PREFS_NAME = "scolapay_printer_prefs"
    private const val KEY_TERMINAL_TYPE = "pref_printer_terminal_type"
    private const val KEY_BT_DEVICE_ADDRESS = "pref_printer_bt_address"
    private const val KEY_BT_DEVICE_NAME = "pref_printer_bt_name"

    // UUID standard SPP (Serial Port Profile) pour toutes les imprimantes Bluetooth ESC/POS
    private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    fun getTerminalType(context: Context): PrinterTerminalType {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val name = prefs.getString(KEY_TERMINAL_TYPE, PrinterTerminalType.POS_58MM.name)
        return try {
            PrinterTerminalType.valueOf(name ?: PrinterTerminalType.POS_58MM.name)
        } catch (e: Exception) {
            PrinterTerminalType.POS_58MM
        }
    }

    fun setTerminalType(context: Context, type: PrinterTerminalType) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_TERMINAL_TYPE, type.name).apply()
    }

    fun getSavedPrinter(context: Context): Pair<String?, String?> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val addr = prefs.getString(KEY_BT_DEVICE_ADDRESS, null)
        val name = prefs.getString(KEY_BT_DEVICE_NAME, null)
        return Pair(addr, name)
    }

    fun setSavedPrinter(context: Context, address: String?, name: String?) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_BT_DEVICE_ADDRESS, address)
            .putString(KEY_BT_DEVICE_NAME, name)
            .apply()
    }

    /**
     * Retourne la liste des appareils Bluetooth appairés
     */
    fun getPairedPrinters(): List<Pair<String, String>> {
        val adapter = BluetoothAdapter.getDefaultAdapter() ?: return emptyList()
        if (!adapter.isEnabled) return emptyList()

        return try {
            adapter.bondedDevices.map { device ->
                Pair(device.name ?: "Imprimante inconnue", device.address)
            }
        } catch (e: SecurityException) {
            emptyList()
        }
    }

    /**
     * Envoie des données brutes ESC/POS à l'imprimante Bluetooth cible.
     */
    suspend fun printDataViaBluetooth(macAddress: String, data: ByteArray): Result<Unit> = withContext(Dispatchers.IO) {
        val adapter = BluetoothAdapter.getDefaultAdapter()
            ?: return@withContext Result.failure(Exception("Bluetooth non disponible sur cet appareil"))

        if (!adapter.isEnabled) {
            return@withContext Result.failure(Exception("Veuillez activer le Bluetooth"))
        }

        var socket: BluetoothSocket? = null
        var outputStream: OutputStream? = null

        try {
            val device: BluetoothDevice = adapter.getRemoteDevice(macAddress)
            socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
            adapter.cancelDiscovery()
            socket.connect()

            outputStream = socket.outputStream
            outputStream.write(data)
            outputStream.flush()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("BluetoothPrinter", "Échec d'impression Bluetooth", e)
            Result.failure(Exception("Échec de connexion à l'imprimante ($macAddress) : ${e.localizedMessage ?: e.message}"))
        } finally {
            try {
                outputStream?.close()
                socket?.close()
            } catch (_: Exception) {}
        }
    }
}
