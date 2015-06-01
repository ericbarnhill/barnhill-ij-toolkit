package ijtoolkit;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.plugin.filter.PlugInFilter;
import ij.process.FloatPolygon;
import ij.process.ImageProcessor;

import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Scanner;

public class Indices_To_Selection implements PlugInFilter {
	
	int h, w, d;
	ImagePlus imp;
	
	@Override
	public void run(ImageProcessor ip) {
		FloatPolygon fp = new FloatPolygon();
		String indices = "";
		try {
			indices = (String) Toolkit.getDefaultToolkit()
			        .getSystemClipboard().getData(DataFlavor.stringFlavor);
		} catch (HeadlessException e) {
			IJ.error("HeadlessException");
			e.printStackTrace();
		} catch (UnsupportedFlavorException e) {
			IJ.error("Unsupported Data Flavor Exception");
			e.printStackTrace();
		} catch (IOException e) {
			IJ.error("IO Exception");
			e.printStackTrace();
		} 
		Scanner scanner = new Scanner(indices);
		int index = 0; double x = 0; double y = 0;
		int tally = 0;
		while (scanner.hasNextInt()) {
			tally++;
			index = scanner.nextInt();
			y = index % h;
			x = Math.floor(index / h);
			fp.addPoint(x, y);
		}
		scanner.close();
		PolygonRoi pr = new PolygonRoi(fp, 2);
		imp.setRoi(pr);
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
