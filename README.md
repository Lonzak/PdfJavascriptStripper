# PdfJavascriptStripper
This Java utility removes all Javascript parts from a PDF document. It may be useful to avoid injection/phishing attacks. Based on openPDF.

Original project based on the code from Andrea Lombardoni on sf (https://sourceforge.net/projects/pdfjavascriptst/)
Changes:
* mavenized the project
* removed unecessary classes
* heavily refactored and bugfixed (the original code did only remove the first JavaScript it found)
* converted the methods to use Stream classes instead of byte[]
* added junit tests

License from the original project is LGPL.

How to run in standalone mode:
------------------------------

PdfJavascriptStripper in.pdf out.pdf


How to use as a lib:
--------------------

net.oneoverzero.common.itext.PdfJavascriptStripper

Use the method:
  
  stripJavascript(InputStream pdfInputStream, OutputStream javaScriptFreeResult)

Which takes a PDF document as an InputStream and writes the cleaned PDF to the supplied outputstream.
The boolean flag tells whether Javascript was removed.