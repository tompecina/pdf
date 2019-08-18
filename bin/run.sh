#!/bin/bash

name=$(basename "$0")
realpath=$(realpath "$0")
path=$(dirname "$realpath")

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
    
java -cp $(find $path/../target/ -name 'pdf-*-jar-with-dependencies.jar' | sort | tail -n 1) $class "$@"
