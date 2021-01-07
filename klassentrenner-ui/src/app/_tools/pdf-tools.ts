
import * as jspdf from 'jspdf';  
import html2canvas from 'html2canvas'; 


export class PdfTools{

    static teacherSummaryPDF(className:string, password: string, teacherURL: string){
        var doc: jspdf.jsPDF = new jspdf.jsPDF({format: 'a4', orientation: 'p'});

        doc.setFontSize(22)
        doc.text("Zugangsdaten für die Klasse", 20, 20)

        doc.setTextColor(200, 142, 51)
        doc.text(className, 28, 35)

        doc.setFontSize(15)
        doc.setTextColor(0, 0, 0)
        doc.text("Link zur Verwaltungsseite für die Klasse:", 20, 60)
        doc.setTextColor(0, 0, 255)

        const fullURL = `http://${teacherURL}`

        doc.textWithLink(teacherURL, 20, 70, { url: fullURL });


        doc.setTextColor(0,0,0)
        doc.text("Klassenpasswort:", 20, 100)
        doc.setTextColor(193, 0, 0);
        doc.text(password, 80, 100 )



        doc.save(`${className}.pdf`)
    }
}