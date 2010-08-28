/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.lupic.core;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.imageio.ImageIO;

import org.jboss.lupic.retriever.FileRetriever;
import org.jboss.lupic.retriever.Retriever;
import org.jboss.lupic.suite.HorizontalAlignment;
import org.jboss.lupic.suite.Mask;
import org.jboss.lupic.suite.VerticalAlignment;

/**
 * @author <a href="mailto:ptisnovs@redhat.com">Pavel Tisnovsky</a>
 * @version $Revision$
 */
public final class ImageUtils {

    private static Retriever imageRetriever = new FileRetriever();

    private ImageUtils() {
    }

    private static BufferedImage readImage(File directory, String fileName) throws IOException {
        File imageFile = new File(directory, fileName);
        return ImageIO.read(imageFile);
    }

    public static void writeImage(BufferedImage img, File directory, String fileName) throws IOException {
        ImageIO.write(img, "png", new File(directory, fileName));
    }

    public static BufferedImage[] readSourceImages(File firstDirectory, File secondDirectory, String fileName)
        throws Exception {
        BufferedImage[] images = new BufferedImage[2];
        for (int i = 0; i < 2; i++) {
            File directory = i == 0 ? firstDirectory : secondDirectory;
            try {
                images[i] = ImageUtils.readImage(directory, fileName);
                Log.logProcess("loading image %s", new File(directory, fileName).getAbsolutePath());
            } catch (IOException e) {
                throw new Exception("Cannot load or initialize image " + fileName);
            }
        }
        return images;
    }

    public static Set<Mask> readMaskImages(File maskDirectory) throws Exception {
        Set<Mask> masks = new HashSet<Mask>();

        for (String imageFileName : maskDirectory.list()) {
            File imageFile = new File(maskDirectory, imageFileName);
            Log.logSetup("read mask image %s", imageFileName);
            masks.add(readMaskImage(imageFile));
        }
        return masks;
    }

    public static Mask readMaskImage(File maskFile) throws Exception {
        String maskFilename = maskFile.getName();
        Mask mask = new Mask(maskFile.getName(), maskFile.toString(), new Properties(), imageRetriever,
            resolveVerticalOrientation(maskFilename), resolveHorizontalOrientation(maskFilename));
        mask.run();
        return mask;
    }

    private static HorizontalAlignment resolveHorizontalOrientation(String fileName) {
        return getFileFlags(fileName).contains("right") ? HorizontalAlignment.RIGHT : HorizontalAlignment.LEFT;
    }

    private static VerticalAlignment resolveVerticalOrientation(String fileName) {
        return getFileFlags(fileName).contains("bottom") ? VerticalAlignment.BOTTOM : VerticalAlignment.TOP;
    }

    private static List<String> getFileFlags(String fileName) {
        String[] parts = fileName.split("\\.");
        // middle parts is composed of file flags, for example "top-left", "bottom-right" etc.
        String[] flags = parts[1].split("-");
        return Arrays.asList(flags);
    }
}
