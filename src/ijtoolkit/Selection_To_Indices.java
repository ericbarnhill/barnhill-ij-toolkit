package ijtoolkit;


/** Selection To Indices, part of the Barnhill IJ Toolkit
 * This PlugInFilter translates between an ImageJ ROI
 * and a matlab vector of selection indices
 * It can be used alone but is best used via the Matlab scripts
 * See those scripts for further documentation
 * Copyright (c) 2015, Eric Barnhill
 * All rights reserved.

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



import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.plugin.filter.PlugInFilter;
import ij.process.FloatPolygon;
import ij.process.ImageProcessor;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

public class Selection_To_Indices implements PlugInFilter {
	
	int h, w, d;
	ImagePlus imp;
	
	@Override
	public void run(ImageProcessor ip) {
		Roi roi = imp.getRoi();
		FloatPolygon fp = roi.getFloatPolygon();
		StringBuilder sb = new StringBuilder();
		int index = 0;
		boolean containsEntry = false;
		/*
		for (int i = 0; i < fp.npoints; i++) {
			index = fp.xpoints[i] + w*fp.ypoints[i];
			sb.append(Integer.toString((int)index));
			sb.append(",");
		}
		*/
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				if ( roi.contains(x, y) ) {
					index = y + x*h;
					sb.append(Integer.toString(index));
					sb.append(",");
					containsEntry = true;
				}
			}
			if (containsEntry) {
				sb.append("...");
				sb.append(System.getProperty("line.separator"));
				containsEntry = false;
			}
		}
		Toolkit toolkit = Toolkit.getDefaultToolkit();
        Clipboard clipboard = toolkit.getSystemClipboard();
        clipboard.setContents(new StringSelection(sb.toString()), null);
		
	}

	@Override
	public int setup(String arg, ImagePlus imp) {
		this.imp = imp;
		h = imp.getHeight();
		w = imp.getWidth();
		d = imp.getImageStackSize();
		return DOES_ALL;
	}

}
