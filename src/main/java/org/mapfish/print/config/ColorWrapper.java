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

package org.mapfish.print.config;

import java.awt.Color;
import com.lowagie.text.html.WebColors;

/**
 * Yaml wrapper for allowing color fields. The supported formats are:
 * <ul>
 * <li> hexadecimal, like: #FFFFFF
 * <li> strings like (in fact all the constants declared in the Color class): white, black, red, ...
 * </ul>
 */
public class ColorWrapper {

    public static Color convertColor(String color) throws IllegalArgumentException {
        if (color == null) {
            return null;
        } else if (color.startsWith("#") && color.length() == 4) {
            int r = Integer.parseInt(color.substring(1, 2)+color.substring(1, 2), 16);
            int g = Integer.parseInt(color.substring(2, 3)+color.substring(2, 3), 16);
            int b = Integer.parseInt(color.substring(3)+color.substring(3), 16);
            return new Color(r,g,b);
        } else {
            Color c = WebColors.getRGBColor(color);
            if (c != null && c.getAlpha() == 0) {
                c = new Color(c.getRed(),c.getGreen(),c.getBlue());
            }
            return c;
        }

    }
}