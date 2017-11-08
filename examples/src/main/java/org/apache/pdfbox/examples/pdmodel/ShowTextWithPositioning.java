/*
 * Copyright 2017 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pdfbox.examples.pdmodel;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.util.Matrix;

/**
 * This example shows how to justify a string using the showTextWithPositioning method. First only
 * spaces are adjusted, and then every letter.
 *
 * @author Dan Fickling
 */
public class ShowTextWithPositioning
{
    public static void main(String[] args) throws Exception
    {
        doIt("Hello World, this is a test!", "justify-example.pdf");
    }

    public static void doIt(String message, String outfile) throws Exception
    {
        // the document
        PDDocument doc = new PDDocument();
        InputStream is = PDDocument.class.getResourceAsStream("/org/apache/pdfbox/resources/ttf/LiberationSans-Regular.ttf");
        final float FONT_SIZE = 20.0f;

        // Page 1
        PDFont font = PDType0Font.load(doc, is, true);
        //PDFont font = PDType1Font.COURIER;
        PDPage page = new PDPage(PDRectangle.A4);
        doc.addPage(page);

        // Get the non-justified string width in text space units.
        float stringWidth = font.getStringWidth(message) * FONT_SIZE;

        // Get the string height in text space units.
        float stringHeight = font.getFontDescriptor().getFontBoundingBox().getHeight() * FONT_SIZE;

        // Get the width we have to justify in.
        PDRectangle pageSize = page.getMediaBox();

        PDPageContentStream contentStream = new PDPageContentStream(doc,
                page, AppendMode.OVERWRITE, false);

        contentStream.beginText();
        contentStream.setFont(font, FONT_SIZE);

        // Start at top of page.
        contentStream.setTextMatrix(
                Matrix.getTranslateInstance(0, pageSize.getHeight() - stringHeight / 1000f));

        // First show non-justified.
        contentStream.showText(message);

        // Move to next line.
        contentStream.setTextMatrix(
                Matrix.getTranslateInstance(0, pageSize.getHeight() - ((stringHeight / 1000f) * 2)));

        // Now show word justified.
        // The space we have to make up, in text space units.
        float justifyWidth = ((pageSize.getWidth() * 1000f) - (stringWidth));

        List<Object> text = new ArrayList<Object>();
        String[] parts = message.split("\\s");

        float spaceWidth = (justifyWidth / (parts.length - 1)) / FONT_SIZE;

        for (int i = 0; i < parts.length; i++)
        {
            if (i != 0)
            {
                text.add(" ");
                // Positive values move to the left, negative to the right.
                text.add(-spaceWidth);
            }
            text.add(parts[i]);
        }
        contentStream.showTextWithPositioning(text.toArray());
        contentStream.setTextMatrix(Matrix.getTranslateInstance(0, pageSize.getHeight() - ((stringHeight / 1000f) * 3)));

        // Now show letter justified.
        text = new ArrayList<Object>();
        justifyWidth = ((pageSize.getWidth() * 1000f) - stringWidth);
        float extraLetterWidth = (justifyWidth / (message.codePointCount(0, message.length()) - 1)) / FONT_SIZE;

        for (int i = 0; i < message.length();)
        {
            if (i != 0)
            {
                text.add(-extraLetterWidth);
            }

            text.add(String.valueOf(Character.toChars(message.codePointAt(i))));

            i += Character.charCount(message.codePointAt(i));
        }
        contentStream.showTextWithPositioning(text.toArray());

        // Finish up.
        contentStream.endText();
        contentStream.close();

        doc.save(outfile);
        doc.close();
    }
}
