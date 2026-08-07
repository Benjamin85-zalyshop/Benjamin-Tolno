package com.example.ui

import android.content.Context
import android.widget.Toast
import com.example.data.models.Payment
import com.example.data.models.Student
import com.sr.SrPrinter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReceiptPrinter {
    fun printReceipt(
        context: Context,
        student: Student,
        payment: Payment,
        schoolName: String,
        classFee: Long
    ) {
        try {
            val printer = SrPrinter.getInstance(context.applicationContext)
            printer.setAlignment(1) // 1 = Center
            printer.setTextBold(true)
            printer.setTextSize(24f)
            printer.printText(schoolName + "\n")
            
            printer.setTextSize(18f)
            printer.setTextBold(false)
            printer.printText("RECU DE PAIEMENT\n\n")
            
            printer.setAlignment(0) // 0 = Left
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val dateStr = dateFormat.format(Date(payment.date))
            
            printer.printText("Date: $dateStr\n")
            val matricule = if (student.remoteId.length >= 5) student.remoteId.take(5).uppercase() else student.id.toString()
            printer.printText("Matricule: #$matricule\n")
            printer.printText("Eleve: ${student.firstName} ${student.lastName}\n")
            printer.printText("Classe: ${student.grade}\n\n")
            
            val fmt = java.text.NumberFormat.getInstance(java.util.Locale("fr", "GN"))
            printer.printText("Montant: ${fmt.format(payment.amount)} GNF\n")
            printer.printText("Motif: ${payment.reason}\n")
            printer.printText("Mode: ${payment.paymentMethod}\n\n")
            
            printer.setAlignment(1)
            printer.printText("Merci de votre confiance.\n")
            printer.nextLine(3)
            
            Toast.makeText(context, "Impression en cours...", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Erreur d'impression: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun printSummaryTicket(
        context: Context,
        schoolName: String,
        matricule: String,
        studentName: String,
        studentGrade: String,
        totalPaid: Long,
        remaining: Long
    ) {
        try {
            val printer = SrPrinter.getInstance(context.applicationContext)
            printer.setAlignment(1)
            printer.setTextBold(true)
            printer.setTextSize(24f)
            printer.printText(schoolName + "\n")
            
            printer.setTextSize(18f)
            printer.setTextBold(false)
            printer.printText("RECU DE PAIEMENT\n\n")
            
            printer.setAlignment(0)
            val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale("fr", "GN"))
            val dateStr = sdf.format(java.util.Date())
            
            printer.printText("Date: $dateStr\n")
            printer.printText("Matricule: #$matricule\n")
            printer.printText("Eleve: $studentName\n")
            printer.printText("Classe: $studentGrade\n\n")
            
            val fmt = java.text.NumberFormat.getInstance(java.util.Locale("fr", "GN"))
            printer.printText("TOTAL PAYE: ${fmt.format(totalPaid)} GNF\n")
            printer.printText("RESTE: ${fmt.format(remaining)} GNF\n\n")
            
            printer.setAlignment(1)
            printer.printText("Merci de votre confiance.\n")
            printer.nextLine(3)
            
            Toast.makeText(context, "Impression en cours...", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Erreur d'impression: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
