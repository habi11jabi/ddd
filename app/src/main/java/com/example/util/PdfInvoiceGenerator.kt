package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.provider.MediaStore
import android.widget.Toast
import com.example.data.model.Sale
import com.example.data.model.ShopSettings
import com.example.viewmodel.InventoryViewModel
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfInvoiceGenerator {

    private const val PAGE_WIDTH = 595 // A4 standard width in points
    private const val PAGE_HEIGHT = 842 // A4 standard height in points

    /**
     * Builds an in-memory Android PdfDocument for the given Sale invoice
     */
    fun createPdfDocument(sale: Sale, shopSettings: ShopSettings): PdfDocument {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        drawInvoiceCanvas(canvas, sale, shopSettings)

        document.finishPage(page)
        return document
    }

    private fun drawInvoiceCanvas(canvas: Canvas, sale: Sale, shopSettings: ShopSettings) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val dateFormat = SimpleDateFormat("dd MMMM, yyyy - hh:mm a", if (shopSettings.appLanguage == "EN") Locale.US else Locale("bn", "BD"))
        val dateString = dateFormat.format(Date(sale.soldAt))

        // Background canvas fill (pure white)
        canvas.drawColor(Color.WHITE)

        // Top decorative accent header band
        paint.color = Color.parseColor("#0284C7")
        paint.style = Paint.Style.FILL
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 12f, paint)

        // Shop Branding Header
        paint.color = Color.parseColor("#0F172A")
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 24f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(shopSettings.shopName, (PAGE_WIDTH / 2).toFloat(), 48f, paint)

        // Tagline
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 12f
        paint.color = Color.parseColor("#475569")
        canvas.drawText(shopSettings.shopTagline, (PAGE_WIDTH / 2).toFloat(), 66f, paint)

        // Shop Contact info
        paint.textSize = 10f
        paint.color = Color.parseColor("#64748B")
        val contactLine = "${shopSettings.shopAddress}  |  মোবাইল: ${shopSettings.shopPhone}"
        canvas.drawText(contactLine, (PAGE_WIDTH / 2).toFloat(), 82f, paint)

        // Title Badge
        paint.color = Color.parseColor("#E0F2FE")
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(190f, 96f, 405f, 122f, 8f, 8f, paint)

        paint.color = Color.parseColor("#0369A1")
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 12f
        canvas.drawText("ক্যাশ মেমো ও ওয়ারেন্টি কার্ড", (PAGE_WIDTH / 2).toFloat(), 113f, paint)

        // Invoice Meta Info
        paint.textAlign = Paint.Align.LEFT
        paint.color = Color.parseColor("#1E293B")
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("ইনভয়েস নং: #${sale.invoiceNumber}", 40f, 145f, paint)

        paint.textAlign = Paint.Align.RIGHT
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.parseColor("#475569")
        canvas.drawText("তারিখ: $dateString", (PAGE_WIDTH - 40).toFloat(), 145f, paint)

        // Horizontal Separator
        paint.strokeWidth = 1.2f
        paint.color = Color.parseColor("#CBD5E1")
        canvas.drawLine(40f, 155f, (PAGE_WIDTH - 40).toFloat(), 155f, paint)

        // Customer Info Card Box
        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#F8FAFC")
        canvas.drawRoundRect(40f, 168f, (PAGE_WIDTH - 40).toFloat(), 242f, 10f, 10f, paint)

        paint.style = Paint.Style.STROKE
        paint.color = Color.parseColor("#E2E8F0")
        paint.strokeWidth = 1f
        canvas.drawRoundRect(40f, 168f, (PAGE_WIDTH - 40).toFloat(), 242f, 10f, 10f, paint)

        paint.style = Paint.Style.FILL
        paint.textAlign = Paint.Align.LEFT
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.parseColor("#0284C7")
        paint.textSize = 11f
        canvas.drawText("[ ক্রেতার তথ্য / Customer Info ]", 54f, 188f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.parseColor("#1E293B")
        paint.textSize = 11f
        canvas.drawText("নাম: ${sale.customerName}", 54f, 208f, paint)
        canvas.drawText("মোবাইল: ${sale.customerPhone}", 54f, 226f, paint)
        if (sale.customerAddress.isNotBlank()) {
            canvas.drawText("ঠিকানা: ${sale.customerAddress}", 320f, 208f, paint)
        }

        // Product Details Section Box
        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#F8FAFC")
        canvas.drawRoundRect(40f, 255f, (PAGE_WIDTH - 40).toFloat(), 355f, 10f, 10f, paint)

        paint.style = Paint.Style.STROKE
        paint.color = Color.parseColor("#E2E8F0")
        canvas.drawRoundRect(40f, 255f, (PAGE_WIDTH - 40).toFloat(), 355f, 10f, 10f, paint)

        paint.style = Paint.Style.FILL
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.parseColor("#0284C7")
        paint.textSize = 11f
        canvas.drawText("[ পণ্যের বিবরণ / Device Details ]", 54f, 275f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.parseColor("#0F172A")
        paint.textSize = 13f
        canvas.drawText(sale.productDetails, 54f, 298f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.parseColor("#334155")
        paint.textSize = 11f
        canvas.drawText("আইএমইআই (IMEI): ${sale.imei}", 54f, 320f, paint)

        val extraSpecs = StringBuilder()
        if (sale.batteryHealth.isNotBlank()) extraSpecs.append("ব্যাটারি হেল্থ: ${sale.batteryHealth}   ")
        if (sale.boxCondition.isNotBlank()) extraSpecs.append("বক্স: ${sale.boxCondition}")
        if (extraSpecs.isNotEmpty()) {
            canvas.drawText(extraSpecs.toString(), 54f, 340f, paint)
        }

        // Pricing & Payment Table Box
        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#F1F5F9")
        canvas.drawRoundRect(40f, 370f, (PAGE_WIDTH - 40).toFloat(), 480f, 10f, 10f, paint)

        paint.style = Paint.Style.STROKE
        paint.color = Color.parseColor("#CBD5E1")
        canvas.drawRoundRect(40f, 370f, (PAGE_WIDTH - 40).toFloat(), 480f, 10f, 10f, paint)

        paint.style = Paint.Style.FILL
        paint.textSize = 12f

        // Row 1: Sale Price
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.parseColor("#1E293B")
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("মোট বিক্রয় মূল্য:", 54f, 396f, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("৳ ${InventoryViewModel.formatBdt(sale.salePrice)}", (PAGE_WIDTH - 54).toFloat(), 396f, paint)

        // Row 2: Paid Amount
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("পরিশোধিত টাকা:", 54f, 420f, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.parseColor("#16A34A")
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("৳ ${InventoryViewModel.formatBdt(sale.paidAmount)}", (PAGE_WIDTH - 54).toFloat(), 420f, paint)

        // Row 3: Due Amount
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = if (sale.dueAmount > 0) Color.parseColor("#DC2626") else Color.parseColor("#16A34A")
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("বকেয়া / বাকি:", 54f, 444f, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("৳ ${InventoryViewModel.formatBdt(sale.dueAmount)}", (PAGE_WIDTH - 54).toFloat(), 444f, paint)

        // Row 4: Payment Method
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.parseColor("#64748B")
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("পেমেন্ট মেথড: ${sale.paymentMethod}", 54f, 468f, paint)

        // Warranty Policy Box (User specified terms)
        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#FEFCE8")
        canvas.drawRoundRect(40f, 498f, (PAGE_WIDTH - 40).toFloat(), 640f, 10f, 10f, paint)

        paint.style = Paint.Style.STROKE
        paint.color = Color.parseColor("#FEF08A")
        canvas.drawRoundRect(40f, 498f, (PAGE_WIDTH - 40).toFloat(), 640f, 10f, 10f, paint)

        paint.style = Paint.Style.FILL
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.parseColor("#A16207")
        paint.textSize = 11f
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("[ অফিসিয়াল ওয়ারেন্টি ও এক্সচেঞ্জ পলিসি / Warranty Policy ]", 54f, 520f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.parseColor("#451A03")
        paint.textSize = 10f
        canvas.drawText("1. 7 Days Replacement Guarantee Without physical Damage & Touch Display.", 54f, 545f, paint)
        canvas.drawText("2. 2 Years Service Warranty Without Parts & update issue.", 54f, 568f, paint)
        canvas.drawText("3. 30% - 35% Deducted From Sell Amount and Exchange.", 54f, 591f, paint)

        if (sale.remarks.isNotBlank()) {
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            paint.color = Color.parseColor("#78350F")
            canvas.drawText("বিশেষ নোট: ${sale.remarks}", 54f, 616f, paint)
        }

        // Bottom Disclaimers & Signatures
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.parseColor("#64748B")
        paint.textSize = 9f
        canvas.drawText("* বিক্রিত পণ্য অফেরতযোগ্য। ওয়ারেন্টির জন্য মেমো প্রদর্শন আবশ্যক।", 40f, 665f, paint)
        canvas.drawText("* APPLE AREA BD থেকে পণ্য ক্রয় করার জন্য আপনাকে ধন্যবাদ!", 40f, 680f, paint)

        // Signature lines
        paint.strokeWidth = 1f
        paint.color = Color.parseColor("#94A3B8")
        canvas.drawLine(54f, 750f, 180f, 750f, paint)
        canvas.drawLine((PAGE_WIDTH - 180).toFloat(), 750f, (PAGE_WIDTH - 54).toFloat(), 750f, paint)

        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 10f
        paint.color = Color.parseColor("#334155")
        canvas.drawText("ক্রেতার স্বাক্ষর", 117f, 768f, paint)
        canvas.drawText("কর্তৃপক্ষের সিল ও স্বাক্ষর", (PAGE_WIDTH - 117).toFloat(), 768f, paint)

        // Footer brand tagline
        paint.textSize = 9f
        paint.color = Color.parseColor("#94A3B8")
        canvas.drawText("APPLE AREA BD • নির্ভরযোগ্য স্মার্টফোন পার্টনার • www.appleareabd.com", (PAGE_WIDTH / 2).toFloat(), 815f, paint)
    }

    /**
     * Saves the PDF to user's device Downloads directory and offers immediate viewing.
     */
    fun savePdfToDownloads(context: Context, sale: Sale, shopSettings: ShopSettings): Uri? {
        val pdfDocument = createPdfDocument(sale, shopSettings)
        val fileName = "AppleAreaBD_Invoice_${sale.invoiceNumber}.pdf"
        var savedUri: Uri? = null

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    val outputStream: OutputStream? = resolver.openOutputStream(uri)
                    outputStream?.use { stream ->
                        pdfDocument.writeTo(stream)
                    }
                    savedUri = uri
                }
            } else {
                @Suppress("DEPRECATION")
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val targetFile = File(downloadsDir, fileName)
                FileOutputStream(targetFile).use { stream ->
                    pdfDocument.writeTo(stream)
                }
                savedUri = Uri.fromFile(targetFile)
            }

            Toast.makeText(context, "ইনভয়েস PDF সফলভাবে ডাউনলোড হয়েছে: $fileName", Toast.LENGTH_LONG).show()

            // Offer viewing intent if valid URI
            savedUri?.let { uri ->
                val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
                }
                try {
                    context.startActivity(Intent.createChooser(viewIntent, "PDF ওপেন করুন"))
                } catch (_: Exception) {}
            }
        } catch (e: Exception) {
            Toast.makeText(context, "PDF সেভ করতে সমস্যা হয়েছে: ${e.message}", Toast.LENGTH_LONG).show()
        } finally {
            pdfDocument.close()
        }

        return savedUri
    }

    /**
     * Opens Android's standard system Print dialog to print directly to Wi-Fi/Bluetooth printer or Save as PDF.
     */
    fun printInvoice(context: Context, sale: Sale, shopSettings: ShopSettings) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
        if (printManager == null) {
            Toast.makeText(context, "প্রিন্ট সার্ভিস পাওয়া যায়নি।", Toast.LENGTH_SHORT).show()
            return
        }

        val printAdapter = object : PrintDocumentAdapter() {
            private var printedPdf: PdfDocument? = null

            override fun onLayout(
                oldAttributes: PrintAttributes?,
                newAttributes: PrintAttributes?,
                cancellationSignal: CancellationSignal?,
                callback: LayoutResultCallback?,
                extras: Bundle?
            ) {
                if (cancellationSignal?.isCanceled == true) {
                    callback?.onLayoutCancelled()
                    return
                }

                val info = PrintDocumentInfo.Builder("Invoice_${sale.invoiceNumber}.pdf")
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(1)
                    .build()

                callback?.onLayoutFinished(info, true)
            }

            override fun onWrite(
                pages: Array<out PageRange>?,
                destination: ParcelFileDescriptor?,
                cancellationSignal: CancellationSignal?,
                callback: WriteResultCallback?
            ) {
                if (cancellationSignal?.isCanceled == true) {
                    callback?.onWriteCancelled()
                    return
                }

                try {
                    val pdf = createPdfDocument(sale, shopSettings)
                    printedPdf = pdf
                    val outputStream = FileOutputStream(destination?.fileDescriptor)
                    pdf.writeTo(outputStream)
                    outputStream.close()
                    callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                } catch (e: Exception) {
                    callback?.onWriteFailed(e.message)
                }
            }

            override fun onFinish() {
                super.onFinish()
                printedPdf?.close()
                printedPdf = null
            }
        }

        val printJobName = "${shopSettings.shopName.replace(" ", "_")}_Memo_${sale.invoiceNumber}"
        val printAttributes = PrintAttributes.Builder()
            .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
            .setColorMode(PrintAttributes.COLOR_MODE_COLOR)
            .build()

        printManager.print(printJobName, printAdapter, printAttributes)
    }
}
