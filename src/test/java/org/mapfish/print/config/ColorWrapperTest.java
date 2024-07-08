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

import static org.junit.Assert.*;

import java.awt.Color;

import org.junit.Test;

import org.mapfish.print.PrintTestCase;

public class ColorWrapperTest extends PrintTestCase {

    @Test
    public void testHexa() {
        assertEquals(new Color(0x12, 0x56, 0xA8), ColorWrapper.convertColor("#1256A8"));

        assertEquals(new Color(0x12, 0x56, 0xb8), ColorWrapper.convertColor("#1256b8"));
    }

    @Test
    public void testHexaAlpha() {

        assertEquals(new Color(0x12, 0x56, 0xb8), ColorWrapper.convertColor("#1256b8"));

        assertEquals(new Color(0xFF, 0x56, 0xb8), ColorWrapper.convertColor("#FF56b8"));

        assertEquals(new Color(0xFF, 0xFF, 0xFF), ColorWrapper.convertColor("#FFF"));
    }

    @Test
    public void testText() {

        assertEquals(Color.WHITE, ColorWrapper.convertColor("white"));

        assertEquals(Color.RED, ColorWrapper.convertColor("Red"));

        assertEquals(Color.LIGHT_GRAY, ColorWrapper.convertColor("Silver"));

        assertEquals(Color.BLACK, ColorWrapper.convertColor("BLACK"));

        assertEquals(Color.YELLOW, ColorWrapper.convertColor("yellow"));
    }
}
