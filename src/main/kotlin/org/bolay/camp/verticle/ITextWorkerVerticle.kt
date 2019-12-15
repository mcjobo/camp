package org.bolay.camp.verticle

import com.itextpdf.kernel.colors.DeviceCmyk
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.property.UnitValue
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class ITextWorkerVerticle : CoroutineVerticle() {
    override suspend fun start() {
        super.start()

        vertx.eventBus().consumer<String>("org.bolay.camp.createPdfTable") { message ->
            println("received getGroups Message")
            GlobalScope.launch(vertx.dispatcher()) {
                createPdfTable()
                message.reply("webroot/listen/camp.pdf")
            }
        }

    }

    suspend fun createPdfTable() {
        val writer = PdfWriter("webroot/listen/camp.pdf")
        val pdf = PdfDocument(writer)
        val document = Document(pdf)

        document.add(Paragraph("Hello World!"))

        var table = Table(floatArrayOf(2f, 2f, 2f))
        table.width = UnitValue.createPercentValue(100f)

        val lightGrey = DeviceCmyk(0, 0, 0, 17)

        table.addHeaderCell(Cell().add(Paragraph("Header1")).setBackgroundColor(lightGrey))
        table.addHeaderCell(Cell().add(Paragraph("Header2")).setBackgroundColor(lightGrey))
        table.addHeaderCell(Cell().add(Paragraph("Header3")).setBackgroundColor(lightGrey))


        table.addCell(Cell().add(Paragraph("cell 1")))
        table.addCell(Cell().add(Paragraph("cell 2")))
        table.addCell(Cell().add(Paragraph("cell 3")))


        document.add(table)
        document.close()
    }
}
