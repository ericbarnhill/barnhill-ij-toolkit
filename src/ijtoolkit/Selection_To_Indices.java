package ijtoolkit;

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
