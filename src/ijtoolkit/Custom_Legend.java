package ijtoolkit;

import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.gui.GUI;
import ij.gui.GenericDialog;
import ij.gui.Line;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.gui.TextRoi;
import ij.plugin.LutLoader;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.plugin.*;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.image.IndexColorModel;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

public class Custom_Legend implements PlugIn {

GenericDialog gd;
int height, width, fontSize, ticks, decimalPlaces, barHeight, barWidth, xMargin, yMargin, fontHeight;
double min, max;
String[] fonts, luts;
String path, font, lut, title, fillColor, textColor, boxOutlineColor, barOutlineColor;
static final String[] colors = {"White","Light Gray","Dark Gray","Black","Red","Green","Blue","Yellow","None"};
ImageProcessor ip;
ImagePlus imp;
IndexColorModel m;
Overlay o;
Font f;
FontMetrics metrics;
Preferences prefs;
boolean widthAuto, heightAuto, barHeightAuto, barWidthAuto;

//PREFERENCE FIELDS

	final String LUT = "lut";
	final String MIN = "min";
	final String MAX = "max";
	final String TICKS = "ticks";
	final String DECIMALPLACES = "decimalPlaces";
	final String FONTSIZE = "fontSize";
	final String FONT = "font";
	final String XMARGIN = "xMargin";
	final String YMARGIN = "yMargin";
	final String FILLCOLOR = "fillColor";
	final String TEXTCOLOR = "textColor";
	final String BOXOUTLINECOLOR = "boxOutlineColor";
	final String BAROUTLINECOLOR = "barOutlineColor";
	final String HEIGHT = "height";
	final String WIDTH = "width";
	final String BARHEIGHT = "barHeight";
	final String BARWIDTH = "barWidth";


/** ImageJ Custom_Legend, part of the Barnhill IJ Toolkit
 * This plugin builds on the IJ Calibration Bar of Bob Dougherty 
 * to offer more customised legend design.
 * Copyright (c) 2015, Eric Barnhill
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer. 
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

The views and conclusions contained in the software and documentation are those
of the authors and should not be interpreted as representing official policies, 
either expressed or implied, of the FreeBSD Project.
*/

public void run(String arg) {
	readPreferences();
	path = IJ.getDirectory("luts");
	if (!showDialog())  return;
	writePreferences();
    ip = new FloatProcessor(width, height + fontHeight);
    title = new String("Legend_"+lut+"_"+min+"_"+max);
    lut = new String(path+lut);
	imp = new ImagePlus(title, ip);
	try {
		m = LutLoader.open(lut);
	} catch (IOException e) { IJ.showMessage("IO Exception. LUT probably not valid.");}
	drawLegend();
	imp = imp.flatten();
	imp.show();
}


private boolean showDialog() {
	GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();         
	fonts = env.getAvailableFontFamilyNames();
	int arialIndex = 0;
	for (int n = 0; n < fonts.length; n++) {
		if (fonts[n].equalsIgnoreCase("Arial")) {
			arialIndex = n;
			break;
		}
	}
	luts = getLuts();
    gd = new GenericDialog("Custom Legend");
    gd.addChoice("LUT", luts, lut);
    gd.addNumericField("Min:", min, 0);
    gd.addNumericField("Max:", max, 0);
    gd.addNumericField("Num Ticks:", ticks, 0);
    gd.addNumericField("Decimal places:", decimalPlaces, 0);
    gd.addNumericField("Font size:", fontSize, 0);
    gd.addChoice("Font:", fonts, font);
    gd.addNumericField("X Margin:", xMargin, 0);
    gd.addNumericField("Y Margin:", yMargin, 0);
    gd.addChoice("Fill color: ", colors, fillColor);
    gd.addChoice("Text color: ", colors, textColor);
    gd.addChoice("Box outline color: ", colors, boxOutlineColor);
    gd.addChoice("Bar outline color: ", colors, barOutlineColor);
    gd.addNumericField("Legend Height (Zero=Auto): ", height, 0);
    gd.addNumericField("Legend Width (Zero=Auto):", width, 0);
    gd.addNumericField("Color Bar Height (Zero=Auto): ", barHeight, 0);
    gd.addNumericField("Color Bar Width (Zero=Auto):", barWidth, 0);
    gd.setInsets(10, 30, 0);
    gd.showDialog();              
    if (gd.wasCanceled())
        return false;
    lut = gd.getNextChoice();
    min = gd.getNextNumber();
    max = gd.getNextNumber();
    ticks = (int)gd.getNextNumber();
    decimalPlaces = (int)gd.getNextNumber();
    fontSize = (int)gd.getNextNumber();
    font = gd.getNextChoice();
    fillColor = gd.getNextChoice();
    textColor = gd.getNextChoice();
    xMargin = (int)gd.getNextNumber();
    yMargin = (int)gd.getNextNumber();
    boxOutlineColor = gd.getNextChoice();
    barOutlineColor = gd.getNextChoice();
    height = (int)gd.getNextNumber();
    width = (int)gd.getNextNumber();
    barHeight = (int)gd.getNextNumber();
    barWidth = (int)gd.getNextNumber();
    f = new Font(font, Font.PLAIN, fontSize);
    // offscreen image to get font metrics and add to margin
    Image img = GUI.createBlankImage(128, 64);
    Graphics g = img.getGraphics();
    metrics = g.getFontMetrics(f);
    fontHeight = metrics.getHeight();
    // if width is set to auto, get width
    if (width == 0) {
    	widthAuto = true;
    	int length1 = metrics.stringWidth(IJ.d2s(max, decimalPlaces+2));
    	int length2 = metrics.stringWidth(IJ.d2s(min, decimalPlaces+2));
    	width = (length1>length2) ? length1 : length2;  
    }
    if (barWidth == 0) {
    	barWidthAuto = true;
    	barWidth = (int)Math.round(width / 4.0);
    	if (widthAuto == true) {
    		width += barWidth + xMargin*2;
    	}
    } else {
    	if (widthAuto == true) {
    		width += barWidth;
    	}
    	width += xMargin*2;
    }
    // if height is set to auto, get height
    if (height == 0) {
    	heightAuto = true;
    	height = fontHeight * ticks * 2 + yMargin*2;  	
    }
    if (barHeight == 0) {
    	barHeightAuto = true;
    	barHeight = height - yMargin*2;
    }
    return true;
}

private String[] getLuts() {
	File f = new File(path);
	String[] list = null;
	if (f.exists() && f.isDirectory())
	list = f.list();
	List<String> luts = new ArrayList<String>();
	for (String s : list) {
		if ( s.endsWith(".lut") ) {
			luts.add(s);
		}
	}
	return luts.toArray(new String[luts.size()]);
}

private void drawLegend() {
    o = new Overlay();
    Color c = getColor(fillColor);
    if (c!=null) {
        Roi r = new Roi(0,0,width,height+fontHeight);
        r.setFillColor(c);
        o.add(r);
    }
    addVerticalColorBar();
    addText();
    c = getColor(boxOutlineColor);
    imp.setOverlay(o);
}

    private void addVerticalColorBar() {
        
    	int mapSize = m.getMapSize();
        byte[] rLUT = new byte[mapSize];
        byte[] gLUT = new byte[mapSize];
        byte[] bLUT = new byte[mapSize];
        m.getReds(rLUT);
        m.getGreens(gLUT);
        m.getBlues(bLUT);
        for (int i = 0; i<(int)(barHeight); i++) {
            int iMap = (int)Math.floor((i*mapSize)/(barHeight));
            int j = (int)(barHeight) - i - 1;
            Line line = new Line(xMargin, yMargin*2 + j, xMargin+barWidth, yMargin*2 + j);
            line.setStrokeColor(new Color(rLUT[iMap]&0xff, gLUT[iMap]&0xff, bLUT[iMap]&0xff));
            line.setStrokeWidth(1.0001);
            o.add(line);
        }  
        
        Color c = getColor(barOutlineColor);
        Roi r = new Roi(xMargin, yMargin, barWidth, barHeight);
        r.setStrokeColor(c);
        r.setStrokeWidth(1.0);
        o.add(r);
        
    }

    private int addText() {

        Color c = getColor(textColor);
        double barStep = (double)(barHeight) ;
        if (ticks > 2) {
            barStep /= ((double)ticks - 1.0);
        }
       
        //Blank offscreen image for font metrics
        int maxLength = 0;
        for (int i = 0; i < ticks; i++) {
            int yLabel = (int)(Math.round( barHeight - i*barStep - 1));
            double tick = min + (max-min)/((double)ticks-1) * i;
            //TextRoi label = new TextRoi( xMargin*2+barWidth, yLabel + fontHeight/2.0, IJ.d2s(tick,decimalPlaces),f);
            TextRoi label = new TextRoi( xMargin*2+barWidth, yMargin*2 + yLabel - fontHeight/2.0, IJ.d2s(tick,decimalPlaces),f);
            label.setStrokeColor(c);
            o.add(label);
            int iLength = metrics.stringWidth(IJ.d2s(tick,decimalPlaces));
            if (iLength > maxLength)
                maxLength = iLength;
        }
        return maxLength;
    }


    Color getColor(String color) {
        Color c = Color.white;
        if (color.equals(colors[1]))
            c = Color.lightGray;
        else if (color.equals(colors[2]))
            c = Color.darkGray;
        else if (color.equals(colors[3]))
            c = Color.black;
        else if (color.equals(colors[4]))
            c = Color.red;
        else if (color.equals(colors[5]))
            c = Color.green;
        else if (color.equals(colors[6]))
            c = Color.blue;
        else if (color.equals(colors[7]))
            c = Color.yellow;
        else if (color.equals(colors[8]))
            c = null;
        return c;
    }    

void readPreferences() {
		
		prefs = Preferences.userNodeForPackage( this.getClass() );
		lut = prefs.get(LUT, "Grays");
		min = prefs.getDouble(MIN, 0);
		max = prefs.getDouble(MAX, 5000);
		ticks = prefs.getInt(TICKS, 6);
		decimalPlaces = prefs.getInt(DECIMALPLACES, 0);
		fontSize = prefs.getInt(FONTSIZE, 12);
		font = prefs.get(FONT, "Arial");
		xMargin = prefs.getInt(XMARGIN, 4);
		yMargin = prefs.getInt(YMARGIN, 4);
		fillColor = prefs.get(FILLCOLOR, colors[0]);
		textColor = prefs.get(TEXTCOLOR, colors[3]);
		boxOutlineColor = prefs.get(BOXOUTLINECOLOR, colors[0]);
		barOutlineColor = prefs.get(BAROUTLINECOLOR, colors[3]);
		height = prefs.getInt(HEIGHT, 0);
		width = prefs.getInt(WIDTH,0);
		barHeight = prefs.getInt(BARHEIGHT, 0);
		barWidth = prefs.getInt(BARWIDTH,0);
		
		return;
		
		
	}
	
	void writePreferences() {
			
		prefs.put(LUT, lut);
		prefs.putDouble(MIN, min);
		prefs.putDouble(MAX, max);
		prefs.putInt(TICKS, ticks);
		prefs.putInt(DECIMALPLACES, decimalPlaces);
		prefs.putInt(FONTSIZE, fontSize);
		prefs.putInt(XMARGIN, xMargin);
		prefs.putInt(YMARGIN, yMargin);
		prefs.put(FILLCOLOR, fillColor);
		prefs.put(TEXTCOLOR, textColor);
		prefs.put(BOXOUTLINECOLOR, boxOutlineColor);
		prefs.put(BAROUTLINECOLOR, barOutlineColor);
		if (heightAuto) {
			prefs.putInt(HEIGHT, 0);
		} else {
			prefs.putInt(HEIGHT, height);
		}
		if (widthAuto) {
			prefs.putInt(WIDTH, 0);
		} else {
			prefs.putInt(WIDTH, width);
		}
		if (barHeightAuto) {
			prefs.putInt(BARHEIGHT, 0);
		} else {
			prefs.putInt(BARHEIGHT, barHeight);
		}
		if (barWidthAuto) {
			prefs.putInt(BARWIDTH, 0);
		} else {
			prefs.putInt(BARWIDTH, barWidth);
		}
		
		try {
			prefs.flush();
		} catch (Exception e) {
			System.out.println("Couldn't flush prefs");
			e.printStackTrace();
		}
		
		return;	
		
	}

}
