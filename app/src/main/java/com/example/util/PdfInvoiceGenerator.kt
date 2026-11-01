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
    fun createPdfDocument(context: Context, sale: Sale, shopSettings: ShopSettings): PdfDocument {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        drawInvoiceCanvas(context, canvas, sale, shopSettings)

        document.finishPage(page)
        return document
    }

    private fun drawInvoiceCanvas(context: Context, canvas: Canvas, sale: Sale, shopSettings: ShopSettings) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val dateFormat = SimpleDateFormat("dd MMMM, yyyy - hh:mm a", if (shopSettings.appLanguage == "EN") Locale.US else Locale("bn", "BD"))
        val dateString = dateFormat.format(Date(sale.soldAt))

        // Background canvas fill (pure white)
        canvas.drawColor(Color.WHITE)

        // Top decorative accent header band
        paint.color = Color.parseColor("#0284C7")
        paint.style = Paint.Style.FILL
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 10f, paint)

        // Draw Shop Logo prominently at the top of the invoice / warranty card
        try {
            val logoDrawable = androidx.core.content.ContextCompat.getDrawable(context, com.example.R.drawable.shop_logo)
            if (logoDrawable != null) {
                val logoW = 84
                val logoH = 46
                val left = (PAGE_WIDTH - logoW) / 2
                val top = 18
                logoDrawable.setBounds(left, top, left + logoW, top + logoH)
                logoDrawable.draw(canvas)
            }
        } catch (_: Exception) {}

        // Shop Branding Header
        paint.color = Color.parseColor("#0F172A")
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 21f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(shopSettings.shopName, (PAGE_WIDTH / 2).toFloat(), 82f, paint)

        // Tagline
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 10f
        paint.color = Color.parseColor("#475569")
        canvas.drawText(shopSettings.shopTagline, (PAGE_WIDTH / 2).toFloat(), 98f, paint)

        // Shop Contact info
        paint.textSize = 9f
        paint.color = Color.parseColor("#64748B")
        val contactLine = "${shopSettings.shopAddress}  |  মোবাইল: ${shopSettings.shopPhone}"
        canvas.drawText(contactLine, (PAGE_WIDTH / 2).toFloat(), 112f, paint)

        // Title Badge
        paint.color = Color.parseColor("#E0F2FE")
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(170f, 122f, 425f, 146f, 6f, 6f, paint)

        paint.color = Color.parseColor("#0369A1")
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 11f
        canvas.drawText("ক্যাশ মেমো ও ওয়ারেন্টি কার্ড", (PAGE_WIDTH / 2).toFloat(), 138f, paint)

        // Invoice Meta Info
        paint.textAlign = Paint.Align.LEFT
        paint.color = Color.parseColor("#1E293B")
        paint.textSize = 10.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("ইনভয়েস নং: #${sale.invoiceNumber}", 40f, 164f, paint)

        paint.textAlign = Paint.Align.RIGHT
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.parseColor("#475569")
        canvas.drawText("তারিখ: $dateString", (PAGE_WIDTH - 40).toFloat(), 164f, paint)

        // Horizontal Separator
        paint.strokeWidth = 1f
        paint.color = Color.parseColor("#CBD5E1")
        canvas.drawLine(40f, 172f, (PAGE_WIDTH - 40).toFloat(), 172f, paint)

        // Customer Info Card Box
        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#F8FAFC")
        canvas.drawRoundRect(40f, 182f, (PAGE_WIDTH - 40).toFloat(), 250f, 10f, 10f, paint)

        paint.style = Paint.Style.STROKE
        paint.color = Color.parseColor("#E2E8F0")
        paint.strokeWidth = 1f
        canvas.drawRoundRect(40f, 182f, (PAGE_WIDTH - 40).toFloat(), 250f, 10f, 10f, paint)

        paint.style = Paint.Style.FILL
        paint.textAlign = Paint.Align.LEFT
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.parseColor("#0284C7")
        paint.textSize = 10.5f
        canvas.drawText("[ ক্রেতার তথ্য / Customer Info ]", 54f, 200f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.parseColor("#1E293B")
        paint.textSize = 10.5f
        canvas.drawText("নাম: ${sale.customerName}", 54f, 218f, paint)
        canvas.drawText("মোবাইল: ${sale.customerPhone}", 54f, 234f, paint)
        if (sale.customerAddress.isNotBlank()) {
            canvas.drawText("ঠিকানা: ${sale.customerAddress}", 320f, 218f, paint)
        }

        // Product Details Section Box
        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#F8FAFC")
        canvas.drawRoundRect(40f, 260f, (PAGE_WIDTH - 40).toFloat(), 355f, 10f, 10f, paint)

        paint.style = Paint.Style.STROKE
        paint.color = Color.parseColor("#E2E8F0")
        canvas.drawRoundRect(40f, 260f, (PAGE_WIDTH - 40).toFloat(), 355f, 10f, 10f, paint)

        paint.style = Paint.Style.FILL
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.parseColor("#0284C7")
        paint.textSize = 10.5f
        canvas.drawText("[ পণ্যের বিবরণ / Device Details ]", 54f, 278f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.parseColor("#0F172A")
        paint.textSize = 12.5f
        canvas.drawText(sale.productDetails, 54f, 300f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.parseColor("#334155")
        paint.textSize = 10.5f
        canvas.drawText("ডিভাইস (IMEI): ${sale.imei}", 54f, 320f, paint)

        val extraSpecs = StringBuilder()
        if (sale.batteryHealth.isNotBlank()) extraSpecs.append("ব্যাটারি: ${sale.batteryHealth}   ")
        if (sale.boxCondition.isNotBlank()) extraSpecs.append("বক্স: ${sale.boxCondition}")
        if (extraSpecs.isNotEmpty()) {
            canvas.drawText(extraSpecs.toString(), 54f, 338f, paint)
        }

        // Pricing & Payment Table Box
        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#F1F5F9")
        canvas.drawRoundRect(40f, 368f, (PAGE_WIDTH - 40).toFloat(), 472f, 10f, 10f, paint)

        paint.style = Paint.Style.STROKE
        paint.color = Color.parseColor("#CBD5E1")
        canvas.drawRoundRect(40f, 368f, (PAGE_WIDTH - 40).toFloat(), 472f, 10f, 10f, paint)

        paint.style = Paint.Style.FILL
        paint.textSize = 11.5f

        // Row 1: Sale Price
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.parseColor("#1E293B")
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("মোট বিক্রয় মূল্য:", 54f, 392f, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("৳  ${InventoryViewModel.formatBdt(sale.salePrice)}", (PAGE_WIDTH - 54).toFloat(), 392f, paint)

        // Row 2: Paid Amount
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("পরিশোধিত টাকা:", 54f, 414f, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.parseColor("#16A34A")
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("৳  ${InventoryViewModel.formatBdt(sale.paidAmount)}", (PAGE_WIDTH - 54).toFloat(), 414f, paint)

        // Row 3: Due Amount
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = if (sale.dueAmount > 0) Color.parseColor("#DC2626") else Color.parseColor("#16A34A")
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("বকেয়া / বাকি:", 54f, 436f, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("৳  ${InventoryViewModel.formatBdt(sale.dueAmount)}", (PAGE_WIDTH - 54).toFloat(), 436f, paint)

        // Row 4: Payment Method
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.parseColor("#64748B")
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("পেমেন্ট মেথড: ${sale.paymentMethod}", 54f, 458f, paint)

        // Warranty Policy Box (User specified terms)
        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#FEFCE8")
        canvas.drawRoundRect(40f, 484f, (PAGE_WIDTH - 40).toFloat(), 624f, 10f, 10f, paint)

        paint.style = Paint.Style.STROKE
        paint.color = Color.parseColor("#FEF08A")
        canvas.drawRoundRect(40f, 484f, (PAGE_WIDTH - 40).toFloat(), 624f, 10f, 10f, paint)

        paint.style = Paint.Style.FILL
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.parseColor("#A16207")
        paint.textSize = 10.5f
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("[ অফিসিয়াল ওয়ারেন্টি ও গ্যারান্টি নীতিমালা / Warranty Policy ]", 54f, 504f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.parseColor("#451A03")
        paint.textSize = 9.5f
        canvas.drawText("1. ${shopSettings.warrantyReplacement}", 54f, 526f, paint)
        canvas.drawText("2. ${shopSettings.warrantyService}", 54f, 547f, paint)
        canvas.drawText("3. ${shopSettings.warrantyExchangeDeduction}", 54f, 568f, paint)

        if (sale.remarks.isNotBlank()) {
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            paint.color = Color.parseColor("#78350F")
            canvas.drawText("বিশেষ নোট: ${sale.remarks}", 54f, 592f, paint)
        }

        // Bottom Disclaimers & Signatures
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.parseColor("#64748B")
        paint.textSize = 8.5f
        canvas.drawText("* বিক্রিত মাল ফেরত নেওয়া হয় না। ওয়ারেন্টি দাবির জন্য মূল ক্যাশমেমো সংরক্ষণ বাধ্যতামূলক।", 40f, 650f, paint)
        canvas.drawText("* ${shopSettings.shopName} এর সাথে থাকার জন্য ধন্যবাদ!", 40f, 664f, paint)

        // Signature lines
        paint.strokeWidth = 1f
        paint.color = Color.parseColor("#94A3B8")
        canvas.drawLine(54f, 730f, 180f, 730f, paint)
        canvas.drawLine((PAGE_WIDTH - 180).toFloat(), 730f, (PAGE_WIDTH - 54).toFloat(), 730f, paint)

        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 9.5f
        paint.color = Color.parseColor("#334155")
        canvas.drawText("ক্রেতার স্বাক্ষর", 117f, 746f, paint)
        canvas.drawText("কর্তৃপক্ষের স্বাক্ষর ও সিল", (PAGE_WIDTH - 117).toFloat(), 746f, paint)

        // Footer brand tagline
        paint.textSize = 8.5f
        paint.color = Color.parseColor("#94A3B8")
        canvas.drawText("${shopSettings.shopName} • ${shopSettings.shopTagline}", (PAGE_WIDTH / 2).toFloat(), 795f, paint)
    }
    /**
     * Saves the PDF to user's device Downloads directory and offers immediate viewing.
     */
    fun savePdfToDownloads(context: Context, sale: Sale, shopSettings: ShopSettings): Uri? {
        val pdfDocument = createPdfDocument(context, sale, shopSettings)
        val fileName = "${shopSettings.shopName.replace(" ", "_")}_Invoice_${sale.invoiceNumber}.pdf"
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
                    val pdf = createPdfDocument(context, sale, shopSettings)
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
