package org.safieddine.ablogistics.ui.utils

import com.lowagie.text.*
import com.lowagie.text.pdf.PdfPCell
import com.lowagie.text.pdf.PdfPTable
import com.lowagie.text.pdf.PdfWriter
import org.safieddine.ablogistics.data.MaterialType
import org.safieddine.ablogistics.data.ReceiptResponse
import org.safieddine.ablogistics.data.BrvPaymentTodoResponse
import java.awt.Color
import java.awt.Desktop
import java.io.File
import java.io.FileOutputStream
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

object PdfExporter {

    fun generateLoadInvoice(load: ReceiptResponse, partner: ReceiptResponse? = null) {
        val fileChooser = JFileChooser()
        fileChooser.dialogTitle = "Save Invoice PDF"
        fileChooser.fileFilter = FileNameExtensionFilter("PDF Files", "pdf")
        val baseId = load.receiptId?.removeSuffix("-D") ?: load.id.toString()
        fileChooser.selectedFile = File("Invoice_${baseId}.pdf")

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
                val logoPath = "composeApp/src/commonMain/composeResources/drawable/ab_logo.png"
                val logo = Image.getInstance(logoPath)
                logo.scaleToFit(80f, 80f)
                val logoCell = PdfPCell(logo)
                logoCell.border = Rectangle.NO_BORDER
                logoCell.horizontalAlignment = Element.ALIGN_LEFT
                headerTable.addCell(logoCell)
            } catch (e: Exception) {
                headerTable.addCell("")
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
            
            val pNo = Paragraph("NO: $baseId", noFont)
            pNo.alignment = Element.ALIGN_RIGHT
            rightInfo.addElement(pNo)
            
            val dateStr = load.createdAt?.let { s ->
                try {
                    val odt = if (s.contains("Z") || s.contains("+") || (s.length > 10 && s.substring(10).contains("-"))) {
                        OffsetDateTime.parse(s)
                    } else {
                        java.time.LocalDateTime.parse(s).atOffset(java.time.ZoneOffset.UTC)
                    }
                    odt.atZoneSameInstant(java.time.ZoneId.systemDefault())
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                } catch (e: Exception) {
                    s.take(16).replace("T", " ")
                }
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

            fun addDataCell(text: String, align: Int = Element.ALIGN_CENTER) {
                val cell = PdfPCell(Phrase(text, detailsFont))
                cell.horizontalAlignment = align
                cell.setPadding(8f)
                cell.minimumHeight = 30f
                table.addCell(cell)
            }

            var lineCount = 0
            var runningTotal = BigDecimal.ZERO

            if (load.material == "MIXED") {
                // Unified mixed load receipt
                val fuelQty = load.fuelDispatchedQuantity ?: load.fuelQuantity ?: BigDecimal.ZERO
                val fuelSp = load.fuelSellingPrice ?: BigDecimal.ZERO
                val fuelAmt = fuelQty.multiply(fuelSp)
                if (fuelQty > BigDecimal.ZERO) {
                    addDataCell(fuelQty.setScale(2, java.math.RoundingMode.HALF_UP).toString())
                    addDataCell("FUEL", Element.ALIGN_LEFT)
                    addDataCell(fuelSp.setScale(2, java.math.RoundingMode.HALF_UP).toString())
                    addDataCell(fuelAmt.setScale(2, java.math.RoundingMode.HALF_UP).toString())
                    runningTotal = runningTotal.add(fuelAmt)
                    lineCount++
                }

                val dieselQty = load.dieselDispatchedQuantity ?: load.dieselQuantity ?: BigDecimal.ZERO
                val dieselSp = load.dieselSellingPrice ?: BigDecimal.ZERO
                val dieselAmt = dieselQty.multiply(dieselSp)
                if (dieselQty > BigDecimal.ZERO) {
                    addDataCell(dieselQty.setScale(2, java.math.RoundingMode.HALF_UP).toString())
                    addDataCell("DIESEL", Element.ALIGN_LEFT)
                    addDataCell(dieselSp.setScale(2, java.math.RoundingMode.HALF_UP).toString())
                    addDataCell(dieselAmt.setScale(2, java.math.RoundingMode.HALF_UP).toString())
                    runningTotal = runningTotal.add(dieselAmt)
                    lineCount++
                }

                val brvCost = load.brvCost ?: BigDecimal.ZERO
                if (brvCost > BigDecimal.ZERO) {
                    addDataCell("1")
                    addDataCell("DELIVERY COST", Element.ALIGN_LEFT)
                    addDataCell(brvCost.setScale(2, java.math.RoundingMode.HALF_UP).toString())
                    addDataCell(brvCost.setScale(2, java.math.RoundingMode.HALF_UP).toString())
                    runningTotal = runningTotal.add(brvCost)
                    lineCount++
                }
            } else {
                // Standard single material load
                val qty1 = load.dispatchedQuantity ?: load.loadedQuantity ?: BigDecimal.ZERO
                val unitPrice1 = load.sellingPrice ?: BigDecimal.ZERO
                val amt1 = qty1.multiply(unitPrice1)
                addDataCell(qty1.setScale(2, java.math.RoundingMode.HALF_UP).toString())
                addDataCell(load.materialType?.name ?: "Logistics Services", Element.ALIGN_LEFT)
                addDataCell(unitPrice1.setScale(2, java.math.RoundingMode.HALF_UP).toString())
                addDataCell(amt1.setScale(2, java.math.RoundingMode.HALF_UP).toString())
                runningTotal = runningTotal.add(amt1)
                lineCount++

                // Partner load (only if legacy mixed dual receipt exists)
                if (partner != null) {
                    val qty2 = partner.dispatchedQuantity ?: partner.loadedQuantity ?: BigDecimal.ZERO
                    val unitPrice2 = partner.sellingPrice ?: BigDecimal.ZERO
                    val amt2 = qty2.multiply(unitPrice2)
                    addDataCell(qty2.setScale(2, java.math.RoundingMode.HALF_UP).toString())
                    addDataCell(partner.materialType?.name ?: "Logistics Services", Element.ALIGN_LEFT)
                    addDataCell(unitPrice2.setScale(2, java.math.RoundingMode.HALF_UP).toString())
                    addDataCell(amt2.setScale(2, java.math.RoundingMode.HALF_UP).toString())
                    runningTotal = runningTotal.add(amt2)
                    lineCount++
                }

                val totalDelivery = (load.brvCost ?: BigDecimal.ZERO).add(partner?.brvCost ?: BigDecimal.ZERO)
                if (totalDelivery > BigDecimal.ZERO) {
                    addDataCell("1")
                    addDataCell("DELIVERY COST", Element.ALIGN_LEFT)
                    addDataCell(totalDelivery.setScale(2, java.math.RoundingMode.HALF_UP).toString())
                    addDataCell(totalDelivery.setScale(2, java.math.RoundingMode.HALF_UP).toString())
                    runningTotal = runningTotal.add(totalDelivery)
                    lineCount++
                }
            }

            // Fill blank lines
            val targetLines = 9
            for (i in lineCount until targetLines) {
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

            val totalCellValue = PdfPCell(Phrase(runningTotal.setScale(2, java.math.RoundingMode.HALF_UP).toString(), boldDetailsFont))
            totalCellValue.horizontalAlignment = Element.ALIGN_CENTER
            totalCellValue.setPadding(5f)
            table.addCell(totalCellValue)

            document.add(table)

            // Footer Section
            document.add(Paragraph("\n"))
            val amountInWords = NumberToWords.convert(runningTotal.toLong())
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
        
        // Auto-open PDF
        openFile(file)
    }

    private fun openFile(file: File) {
        try {
            if (Desktop.isDesktopSupported()) {
                val desktop = Desktop.getDesktop()
                if (desktop.isSupported(Desktop.Action.OPEN)) {
                    desktop.open(file)
                    return
                }
            }
            // Fallback for different OS environments
            val os = System.getProperty("os.name").lowercase()
            if (os.contains("win")) {
                Runtime.getRuntime().exec(arrayOf("cmd.exe", "/c", "start", "\"\"", file.absolutePath))
            } else if (os.contains("mac")) {
                Runtime.getRuntime().exec(arrayOf("open", file.absolutePath))
            } else {
                Runtime.getRuntime().exec(arrayOf("xdg-open", file.absolutePath))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun generateBrvPaymentsPdf(payments: List<BrvPaymentTodoResponse>) {
        val fileChooser = JFileChooser()
        fileChooser.dialogTitle = "Save BRV Payments PDF"
        fileChooser.fileFilter = FileNameExtensionFilter("PDF Files", "pdf")
        fileChooser.selectedFile = File("BRV_Payments_Todo.pdf")

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

            // Header Table (Logo on left, Title & Issue Date on right)
            val headerTable = PdfPTable(2)
            headerTable.widthPercentage = 100f
            headerTable.setWidths(floatArrayOf(1f, 4f))
            headerTable.defaultCell.border = Rectangle.NO_BORDER

            // Logo Cell
            val logoCell = PdfPCell()
            logoCell.border = Rectangle.NO_BORDER
            logoCell.verticalAlignment = Element.ALIGN_MIDDLE
            try {
                val resourceStream = PdfExporter::class.java.classLoader.getResourceAsStream("drawable/ab_logo.png")
                    ?: PdfExporter::class.java.classLoader.getResourceAsStream("ab_logo.png")
                val logo = if (resourceStream != null) {
                    Image.getInstance(resourceStream.readBytes())
                } else {
                    var logoPath = "composeApp/src/commonMain/composeResources/drawable/ab_logo.png"
                    if (!File(logoPath).exists()) {
                        logoPath = "src/commonMain/composeResources/drawable/ab_logo.png"
                    }
                    Image.getInstance(logoPath)
                }
                logo.scaleToFit(80f, 80f)
                logoCell.addElement(logo)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            headerTable.addCell(logoCell)

            // Title & Date Cell
            val titleCell = PdfPCell()
            titleCell.border = Rectangle.NO_BORDER
            titleCell.verticalAlignment = Element.ALIGN_MIDDLE
            titleCell.setPaddingLeft(12f)

            val titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16f, Color.BLACK)
            val dateFont = FontFactory.getFont(FontFactory.HELVETICA, 10f, Color.DARK_GRAY)

            val pTitle = Paragraph("BRV PAYMENTS TODO LIST", titleFont)
            pTitle.alignment = Element.ALIGN_LEFT
            titleCell.addElement(pTitle)

            val todayStr = java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            val pDate = Paragraph("Issue Date: $todayStr", dateFont)
            pDate.alignment = Element.ALIGN_LEFT
            pDate.setSpacingBefore(4f)
            titleCell.addElement(pDate)

            headerTable.addCell(titleCell)

            document.add(headerTable)
            document.add(Paragraph("\n"))

            // Main Checklist Table (Tick, BRV Plate, Customer, Amount)
            val table = PdfPTable(4)
            table.widthPercentage = 100f
            table.setWidths(floatArrayOf(0.6f, 1.5f, 3.5f, 1.5f))
            table.setSpacingBefore(10f)

            val detailsFont = FontFactory.getFont(FontFactory.HELVETICA, 11f, Color.BLACK)

            fun addCheckboxCell(checked: Boolean) {
                val outerCell = PdfPCell()
                outerCell.horizontalAlignment = Element.ALIGN_CENTER
                outerCell.verticalAlignment = Element.ALIGN_MIDDLE
                outerCell.border = Rectangle.BOTTOM
                outerCell.borderWidth = 0.5f
                outerCell.borderColor = Color(0xFFE5E5E5.toInt())
                outerCell.setPadding(4f)
                
                val innerTable = PdfPTable(1)
                innerTable.totalWidth = 10f
                innerTable.isLockedWidth = true
                
                val innerCell = PdfPCell()
                innerCell.minimumHeight = 10f
                innerCell.borderWidth = 1f
                innerCell.borderColor = Color.GRAY
                innerCell.setPadding(0f)
                
                if (checked) {
                    innerCell.backgroundColor = Color(0xFF0078D4.toInt()) // Fluent blue Accent
                    val checkFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8f, Color.WHITE)
                    val p = Paragraph("x", checkFont)
                    p.alignment = Element.ALIGN_CENTER
                    p.leading = 6f
                    innerCell.addElement(p)
                } else {
                    innerCell.backgroundColor = Color.WHITE
                }
                
                innerTable.addCell(innerCell)
                outerCell.addElement(innerTable)
                table.addCell(outerCell)
            }

            fun addDataCell(text: String, align: Int = Element.ALIGN_CENTER) {
                val cell = PdfPCell(Phrase(text, detailsFont))
                cell.horizontalAlignment = align
                cell.border = Rectangle.BOTTOM
                cell.borderWidth = 0.5f
                cell.borderColor = Color(0xFFE5E5E5.toInt())
                cell.setPadding(8f)
                table.addCell(cell)
            }

            for (payment in payments) {
                addCheckboxCell(false) // Always empty so the employee can tick it manually on the printed sheet
                addDataCell(payment.plateNumber)
                addDataCell(payment.customerName, Element.ALIGN_LEFT)
                
                val amountStr = java.text.DecimalFormat("#,##0.00").format(payment.amount)
                addDataCell(amountStr + " DZD", Element.ALIGN_RIGHT)
            }

            document.add(table)

            // Auto-open document
            document.close()
            openFile(file)

        } catch (e: Exception) {
            e.printStackTrace()
            if (document.isOpen) {
                document.close()
            }
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
