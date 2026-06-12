/*
 * Copyright (C) 2013  Camptocamp
 *
 * This file is part of MapFish Print
 *
 * MapFish Print is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MapFish Print is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MapFish Print.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mapfish.print.output;

import com.lowagie.text.DocumentException;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.mapfish.print.RenderingContext;
import org.mapfish.print.utils.PJsonObject;
import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Similar to {@link InMemoryJaiMosaicOutputFactory} in that it uses pdf box to parse pdf.  However it writes
 * each page to disk as an image before combining them using ImageN mosaic.
 *
 * @author jeichar
 */
public class FileCachingJaiMosaicOutputFactory extends InMemoryJaiMosaicOutputFactory {

    @Override
    public OutputFormat create(String format) {
        return new ImageOutputScalable(format);
    }

    public String enablementStatus() {
        if(super.enablementStatus() != null) {
            return super.enablementStatus();
        }
        if(!formats().contains("TIFF")) {
            return "TIFF not supported by ImageIO";
        }
        return null;
    }

    public static class ImageOutputScalable extends AbstractImageFormat {

        public static final Logger LOGGER = LogManager.getLogger(ImageOutputScalable.class);

        public ImageOutputScalable(String format) {
            super(format);
        }

        public RenderingContext print(PrintParams params) throws DocumentException {
            File tmpFile = null;
            try {
                tmpFile = File.createTempFile("mapfishprint", ".pdf");
                FileOutputStream tmpOut = new FileOutputStream(tmpFile);
                RenderingContext context;
                try {
                    context = doPrint(params.withOutput(tmpOut));
                } finally {
                    tmpOut.close();
                }

                List<ImageInfo> images = createImages(params.jsonSpec, tmpFile, context);

                drawImage(params.outputStream, images);

                return context;
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                if (tmpFile != null) {
                    if(!tmpFile.delete()) {
                        LOGGER.warn(tmpFile+" was not able to be deleted for unknown reason.  Will try again on shutdown");
                    }
                    tmpFile.deleteOnExit();
                }
            }
        }

        private void drawImage(OutputStream out, List<ImageInfo> images) throws IOException {
            if (images == null || images.isEmpty()) {
                return;
            }

            // 1. Calculate the final mosaic dimensions
            int totalHeight = 0;
            int maxWidth = 0;

            for (ImageInfo imageinfo : images) {
                totalHeight += imageinfo.height + MARGIN;
                if (maxWidth < imageinfo.width) {
                    maxWidth = (int) imageinfo.width;
                }
            }

            // 2. Create the blank canvas
            // Handle JPEG specific color model to avoid incorrect colors on transparent pixels
            boolean isJpeg = "jpg".equalsIgnoreCase(format) || "jpeg".equalsIgnoreCase(format);
            int imageType = isJpeg ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;

            BufferedImage mosaic = new BufferedImage(maxWidth, totalHeight, imageType);
            Graphics2D g2d = mosaic.createGraphics();

            // Fill background with white for JPEGs
            if (isJpeg) {
                g2d.setColor(Color.WHITE);
                g2d.fillRect(0, 0, maxWidth, totalHeight);
            }

            // Explicitly enable ImageIO disk caching. This honors the original intent 
            // of the "FileCaching" class by preventing stream data from flooding the RAM.
            ImageIO.setUseCache(true);

            // 3. Draw each image onto the canvas
            int currentY = 0;
            int i = 0;

            for (ImageInfo imageinfo : images) {
                BufferedImage pageImage = ImageIO.read(imageinfo.imageFile);

                if (pageImage == null) {
                    g2d.dispose();
                    throw new IllegalArgumentException("Cannot read image: " + imageinfo.imageFile.getAbsolutePath() + " - missing ImageIO plugin for this format.");
                }

                i++;
                LOGGER.debug("Adding page image " + i + " bounds: [0," + currentY + " " + pageImage.getWidth() + "," + (currentY + pageImage.getHeight()) + "]");

                g2d.drawImage(pageImage, 0, currentY, null);
                currentY += imageinfo.height + MARGIN;
            }

            g2d.dispose();

            ImageIO.write(mosaic, format, out);
        }
        
        private List<ImageInfo> createImages(PJsonObject jsonSpec, File tmpFile, RenderingContext context) throws IOException {
            List<ImageInfo> images = new ArrayList<ImageInfo>();
            PDDocument pdf = PDDocument.load(tmpFile);
            try {
                PDFRenderer pdfRenderer = new PDFRenderer(pdf);
                for (PDPage page : pdf.getPages())
                {
                    BufferedImage img = pdfRenderer.renderImageWithDPI(images.size(), calculateDPI(context, jsonSpec), ImageType.RGB);
                    File file = File.createTempFile("pdfToImage", "tiff");
                    ImageIO.write(img, "TIFF", file);
                    images.add(new ImageInfo(file, img.getWidth(), img.getHeight()));
                }
            } finally {
                pdf.close();
            }
            return images;
        }

    }

    static class ImageInfo {
        final File imageFile;
        final int width, height;

        ImageInfo(File imageFile, int width, int height) {
            this.imageFile = imageFile;

            this.width = width;
            this.height = height;
        }
    }
}
