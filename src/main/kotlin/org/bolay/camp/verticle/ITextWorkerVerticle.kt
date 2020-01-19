package org.bolay.camp.verticle

import com.itextpdf.io.font.constants.StandardFonts
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.colors.DeviceCmyk
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.Style
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.element.Text
import com.itextpdf.layout.property.TextAlignment
import com.itextpdf.layout.property.UnitValue
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.eventbus.requestAwait
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class ITextWorkerVerticle : CoroutineVerticle() {
    var _style = Style().setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA))
            .setFontSize(10f)

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

        createTableHeader(document, pData)
        createTablePerOutpost(document, pData)

        document.close()
    }

    private suspend fun createTableHeader(document: Document, pData: JsonObject) {
        var text = pData.getString("bezeichnung")
        text += "\n" + pData.getString("notiz")
        var headerText = Text(text).addStyle(_style).setFontSize(14f)
        var header = Paragraph(headerText)
        header.setTextAlignment(TextAlignment.CENTER)
        document.add(header)
    }

    suspend fun createTablePerOutpost(pDocument: Document, pData: JsonObject) {


//        var cell1 = Cell(1, 3)
//        cell1.add(Paragraph(pData.getString("bezeichnung")))
//        table.addHeaderCell(cell1)
        var childGroups = pData.getJsonArray("childGroups")
        childGroups.forEach {
            var group = it as JsonObject

            var outpostTable = Table(floatArrayOf(10f))
            outpostTable.setWidth(UnitValue.createPercentValue(100f))
            outpostTable.setMargins(10f, 10f, 10f, 10f)

            var outPostCell = Cell()
            outPostCell.setBackgroundColor(ColorConstants.BLUE)
            var text = Text(group.getString("bezeichnung")).setFontColor(ColorConstants.WHITE).addStyle(_style)
            outPostCell.add(Paragraph(text))

            outpostTable.addHeaderCell(outPostCell)

            val mapPersonsToTeams = mapPersonsToTeams(group)
            createTablePerTeam(mapPersonsToTeams, outpostTable)

            pDocument.add(outpostTable)
        }

    }

    private suspend fun createTablePerTeam(mapPersonsToTeams: JsonObject, outpostTable: Table) {
        mapPersonsToTeams.forEach {
            var teamsCell = Cell(1, 3)

            var team = it.value as JsonObject


            var teamTable = Table(floatArrayOf(10f, 10f, 10f))
            teamTable.setWidth(UnitValue.createPercentValue(100f))
            teamTable.setMargins(2f, 2f, 2f, 2f)

            var teamHeaderCell = Cell(1, 3)
            teamHeaderCell.setBackgroundColor(DeviceRgb(105, 105, 105))
            var label = team.getString("name")
            if (team.containsKey("leader")) {
                label += " Leiter: "
                label += team.getJsonObject("leader").getString("vorname") + " "
                label += team.getJsonObject("leader").getString("name")
            }
            var text = Text(label).addStyle(_style).setFontColor(ColorConstants.WHITE)
            teamHeaderCell.add(Paragraph(text))
            teamTable.addHeaderCell(teamHeaderCell)
            var odd = true

            team.getJsonObject("members").forEach {

                var teamMember = Cell(1, 3)

                if (odd) teamMember.setBackgroundColor(DeviceRgb(220, 220, 220))
                else teamMember.setBackgroundColor(DeviceRgb(192, 192, 192))

                val member = it.value as JsonObject
                var memberLabel = member.getString("vorname") + " " + member.getString("name")
                var memberText = Text(memberLabel).addStyle(_style)
                teamMember.add(Paragraph(memberText))
                teamTable.addCell(teamMember)
                odd = !odd
            }
            teamsCell.add(teamTable)
            outpostTable.addCell(teamsCell)
        }


    }


    private suspend fun mapPersonsToTeams(pGroup: JsonObject): JsonObject {
        // the id of a teamleader (Mitarbeiter) in a team (grouptype kleingruppe)
        val teamLeaderId = "12"

        var teams = JsonObject()
        val userList = pGroup.getJsonArray("users")
        userList.forEach {
            var user = it as JsonObject
            (it as JsonObject).forEach {
                val i = 0
                user = it.value as JsonObject
            }

            if (user.getJsonObject("groupValues") != null && user.getJsonObject("groupValues").getJsonObject("Team") != null) {
                val teamName = user.getJsonObject("groupValues").getJsonObject("Team").getString("value")
                if (teamName != null && !teams.containsKey(teamName)) {
                    teams.put(teamName, JsonObject().put("name", teamName).put("members", JsonObject()))
                }
                teams.getJsonObject(teamName).getJsonObject("members").put(user.getString("p_id"), user)
                if (user.getJsonObject("groupAttributes").getString("groupmemberstatus_id") == teamLeaderId) {
                    teams.getJsonObject(teamName).put("leader", user)
                }
            }
        }
        return teams
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
