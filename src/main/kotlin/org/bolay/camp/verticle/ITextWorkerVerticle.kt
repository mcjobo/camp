package org.bolay.camp.verticle

import com.itextpdf.kernel.colors.DeviceCmyk
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.property.UnitValue
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.eventbus.requestAwait
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class ITextWorkerVerticle : CoroutineVerticle() {
    override suspend fun start() {
        super.start()

        vertx.eventBus().consumer<String>("org.bolay.camp.createPdfTable") { message ->
            GlobalScope.launch(vertx.dispatcher()) {
                createPdfTable()
                message.reply("/listen/camp.pdf")
            }
        }
        vertx.eventBus().consumer<String>("org.bolay.camp.createPersonTable") { message ->
            GlobalScope.launch(vertx.dispatcher()) {
                var data = vertx.eventBus().requestAwait<String>("org.bolay.camp.getPersonTableData", "").body()
                createPersonTable(JsonObject(data))
//                message.reply("/listen/personTable.pdf")
                message.reply(data)
            }
        }

    }

    suspend fun createPersonTable(pData: JsonObject) {
        val writer = PdfWriter("webroot/listen/personTable.pdf")
        val pdf = PdfDocument(writer)
        val document = Document(pdf)

        createTablePerOutpost(document, pData)

        document.close()
    }

    suspend fun createTablePerOutpost(pDocument: Document, pData: JsonObject) {
        var table = Table(floatArrayOf(10f, 10f, 10f))
        table.width = UnitValue.createPercentValue(100f)
        var cell1 = Cell(1, 3)
        cell1.add(Paragraph(pData.getString("bezeichnung")))
        table.addHeaderCell(cell1)
        var childGroups = pData.getJsonArray("childGroups")
        childGroups.forEach {
            var group = it as JsonObject
            var cell1 = Cell(1, 3)
            cell1.add(Paragraph(group.getString("bezeichnung")))
            table.addHeaderCell(cell1)
        }

        pDocument.add(table)
    }


    suspend fun mapPersonsToTeams(pGroup: JsonObject) {
        
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
