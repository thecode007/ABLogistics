package org.safieddine.ablogistics.ui.utils

import com.lowagie.text.*
import com.lowagie.text.pdf.PdfPCell
import com.lowagie.text.pdf.PdfPTable
import com.lowagie.text.pdf.PdfWriter
import org.safieddine.ablogistics.data.MaterialType
import org.safieddine.ablogistics.data.ReceiptResponse
import java.awt.Color
import java.io.File
import java.io.FileOutputStream
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

object PdfExporter {

    fun generateLoadInvoice(load: ReceiptResponse) {
        val fileChooser = JFileChooser()
        fileChooser.dialogTitle = "Save Invoice PDF"
        fileChooser.fileFilter = FileNameExtensionFilter("PDF Files", "pdf")
        fileChooser.selectedFile = File("Invoice_${load.receiptId ?: load.id}.pdf")

        val userSelection = fileChooser.showSaveDialog(null)
        if (userSelection != JFileChooser.APPROVE_OPTION) return

        var file = fileChooser.selectedFile
        if (!file.name.lowercase().endsWith(".pdf")) {
            file = File(file.absolutePath + ".pdf")
        }

        val document = Document(PageSize.A4)
        try {
            PdfWriter.getInstance(document, FileOutputStream(file))
            document.open()

            // Header Section
            val headerTable = PdfPTable(3)
            headerTable.widthPercentage = 100f
            headerTable.setWidths(floatArrayOf(1f, 2f, 1f))
            headerTable.defaultCell.border = Rectangle.NO_BORDER

            // Logo (Left)
            try {
                // Look for the logo in resources or specific path
                val logoPath = "composeApp/src/commonMain/composeResources/drawable/ab_logo.png"
                val logo = Image.getInstance(logoPath)
                logo.scaleToFit(80f, 80f)
                val logoCell = PdfPCell(logo)
                logoCell.border = Rectangle.NO_BORDER
                logoCell.horizontalAlignment = Element.ALIGN_LEFT
                headerTable.addCell(logoCell)
            } catch (e: Exception) {
                headerTable.addCell("") // Placeholder if logo fails
            }

            // Company Info (Middle)
            val companyInfo = PdfPCell()
            companyInfo.border = Rectangle.NO_BORDER
            companyInfo.horizontalAlignment = Element.ALIGN_CENTER
            val titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18f, Color.BLACK)
            val subTitleFont = FontFactory.getFont(FontFactory.HELVETICA, 10f, Color.BLACK)
            val invoiceLabelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14f, Color.BLACK)

            val pTitle = Paragraph("A. B. LOGISTICS LTD", titleFont)
            pTitle.alignment = Element.ALIGN_CENTER
            companyInfo.addElement(pTitle)

            val pAddr = Paragraph("156 CHARLOTTE STREET\n+232 88 200-004\nEmail: bazzyabdallah@gmail.com", subTitleFont)
            pAddr.alignment = Element.ALIGN_CENTER
            companyInfo.addElement(pAddr)

            val pInvoice = Paragraph("INVOICE", invoiceLabelFont)
            pInvoice.alignment = Element.ALIGN_CENTER
            pInvoice.setSpacingBefore(10f)
            companyInfo.addElement(pInvoice)

            headerTable.addCell(companyInfo)

            // NO and Date (Right)
            val rightInfo = PdfPCell()
            rightInfo.border = Rectangle.NO_BORDER
            rightInfo.horizontalAlignment = Element.ALIGN_RIGHT
            
            val noFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12f, Color.RED)
            val dateFont = FontFactory.getFont(FontFactory.HELVETICA, 12f, Color.BLACK)
            
            val pNo = Paragraph("NO: ${load.receiptId ?: load.id}", noFont)
            pNo.alignment = Element.ALIGN_RIGHT
            rightInfo.addElement(pNo)
            
            val dateStr = load.createdAt?.let {
                try {
                    val odt = OffsetDateTime.parse(it)
                    odt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                } catch (e: Exception) { it }
            } ?: ""
            val pDate = Paragraph("Date: $dateStr", dateFont)
            pDate.alignment = Element.ALIGN_RIGHT
            rightInfo.addElement(pDate)
            
            headerTable.addCell(rightInfo)

            document.add(headerTable)
            document.add(Paragraph("\n"))

            // Customer Info Section
            val detailsFont = FontFactory.getFont(FontFactory.HELVETICA, 12f, Color.BLACK)
            val boldDetailsFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12f, Color.BLACK)

            val customerTable = PdfPTable(1)
            customerTable.widthPercentage = 100f
            
            fun addDetailLine(label: String, value: String) {
                val p = Paragraph()
                p.add(Chunk("$label: ", boldDetailsFont))
                p.add(Chunk(value, detailsFont))
                p.setSpacingAfter(5f)
                val cell = PdfPCell(p)
                cell.border = Rectangle.NO_BORDER
                customerTable.addCell(cell)
            }

            addDetailLine("Company's Name", load.customerName ?: "N/A")
            addDetailLine("Receiver's Name", "")
            addDetailLine("Address", "")

            document.add(customerTable)
            document.add(Paragraph("\n"))

            // Main Table Section
            val table = PdfPTable(4)
            table.widthPercentage = 100f
            table.setWidths(floatArrayOf(1f, 3f, 1.5f, 1.5f))
            table.setSpacingBefore(10f)

            fun addHeaderCell(text: String) {
                val cell = PdfPCell(Phrase(text, boldDetailsFont))
                cell.horizontalAlignment = Element.ALIGN_CENTER
                cell.backgroundColor = Color.LIGHT_GRAY
                cell.setPadding(5f)
                table.addCell(cell)
            }

            addHeaderCell("QTY LITERS")
            addHeaderCell("DESCRIPTION")
            addHeaderCell("UNIT PRICE")
            addHeaderCell("AMOUNT")

            // Data Row
            val qty = load.dispatchedQuantity ?: load.loadedQuantity ?: BigDecimal.ZERO
            val unitPrice = load.sellingPrice ?: BigDecimal.ZERO
            val amount = load.amount

            fun addDataCell(text: String, align: Int = Element.ALIGN_CENTER) {
                val cell = PdfPCell(Phrase(text, detailsFont))
                cell.horizontalAlignment = align
                cell.setPadding(8f)
                cell.minimumHeight = 30f
                table.addCell(cell)
            }

            addDataCell(qty.setScale(2, java.math.RoundingMode.HALF_UP).toString())
            addDataCell(load.materialType?.name ?: "Logistics Services", Element.ALIGN_LEFT)
            addDataCell(unitPrice.setScale(2, java.math.RoundingMode.HALF_UP).toString())
            addDataCell(amount.setScale(2, java.math.RoundingMode.HALF_UP).toString())

            // Add some empty rows to mimic the image look
            for (i in 1..8) {
                addDataCell("")
                addDataCell("")
                addDataCell("")
                addDataCell("")
            }

            // Total Row
            val totalCellLabel = PdfPCell(Phrase("TOTAL", boldDetailsFont))
            totalCellLabel.colspan = 3
            totalCellLabel.horizontalAlignment = Element.ALIGN_RIGHT
            totalCellLabel.setPadding(5f)
            table.addCell(totalCellLabel)

            val totalCellValue = PdfPCell(Phrase(amount.setScale(2, java.math.RoundingMode.HALF_UP).toString(), boldDetailsFont))
            totalCellValue.horizontalAlignment = Element.ALIGN_CENTER
            totalCellValue.setPadding(5f)
            table.addCell(totalCellValue)

            document.add(table)

            // Footer Section
            document.add(Paragraph("\n"))
            val amountInWords = NumberToWords.convert(amount.toLong())
            val pWords = Paragraph()
            pWords.add(Chunk("Amount in words: ", boldDetailsFont))
            pWords.add(Chunk("$amountInWords Only", detailsFont))
            document.add(pWords)

            document.add(Paragraph("\n\n"))
            val signatureTable = PdfPTable(2)
            signatureTable.widthPercentage = 100f
            
            val sigCell1 = PdfPCell(Paragraph("..................................................", detailsFont))
            sigCell1.border = Rectangle.NO_BORDER
            signatureTable.addCell(sigCell1)

            val sigCell2 = PdfPCell(Paragraph("Signature: .................................", boldDetailsFont))
            sigCell2.border = Rectangle.NO_BORDER
            sigCell2.horizontalAlignment = Element.ALIGN_RIGHT
            signatureTable.addCell(sigCell2)

            document.add(signatureTable)

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            document.close()
        }
    }
}

object NumberToWords {
    private val units = arrayOf("", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine")
    private val teens = arrayOf("Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen")
    private val tens = arrayOf("", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety")

    fun convert(n: Long): String {
        if (n == 0L) return "Zero"
        if (n < 0) return "Minus " + convert(-n)

        var result = ""
        if (n >= 1_000_000_000) {
            result += convert(n / 1_000_000_000) + " Billion "
            return result + convert(n % 1_000_000_000)
        }
        if (n >= 1_000_000) {
            result += convert(n / 1_000_000) + " Million "
            return result + convert(n % 1_000_000)
        }
        if (n >= 1_000) {
            result += convert(n / 1_000) + " Thousand "
            return result + convert(n % 1_000)
        }
        if (n >= 100) {
            result += convert(n / 100) + " Hundred "
            return result + convert(n % 100)
        }
        if (n >= 20) {
            result += tens[(n / 10).toInt()] + " "
            return result + convert(n % 10)
        }
        if (n >= 10) {
            return teens[(n - 10).toInt()]
        }
        return units[n.toInt()]
    }
}
