#!/bin/bash

name=$(basename "$0")
realpath=$(realpath "$0")
path=$(dirname "$realpath")

echo $path
case "$name" in
     addpdfmeta) class='cz.pecina.pdf.addpdfmeta.AddPdfMeta';;
     addpdfstream) class='cz.pecina.pdf.addpdfstream.AddPdfStream';;
     inspectpdf) class='cz.pecina.pdf.inspectpdf.InspectPdf';;
     pdftoxml) class='cz.pecina.pdf.pdftoxml.PdfToXml';;
     readpdfstream) class='cz.pecina.pdf.readpdfstream.ReadPdfStream';;
     rmwmark) class='cz.pecina.pdf.rmwmark.RmWmark';;
     signboxpdf) class='cz.pecina.pdf.signboxpdf.SignBoxPdf';;
     signpdf) class='cz.pecina.pdf.signpdf.SignPdf';;
     stamppdf) class='cz.pecina.pdf.stamppdf.StampPdf';;
     *) class='cz.pecina.pdf.PdfHelp';;
esac
    
cd $path/../target
java -cp $(ls pdf-*-jar-with-dependencies.jar) $class "$*"
