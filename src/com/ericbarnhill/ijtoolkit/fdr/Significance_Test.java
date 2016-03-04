package com.ericbarnhill.ijtoolkit.fdr;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

/**
 * Significance Test Front End for ImageJ
 * 
 * [detail]
 * [attribution]
 * @author Eric Barnhill
 * @version 0.1
 */
    /*
    Permission to use the software and accompanying documentation provided on these pages for educational, 
    research, and not-for-profit purposes, without fee and without a signed licensing agreement, is hereby
    granted, provided that the above copyright notice, this paragraph and the following two paragraphs 
    appear in all copies. The copyright holder is free to make upgraded or improved versions of the 
    software available for a fee or commercially only.

    IN NO EVENT SHALL THE COPYRIGHT HOLDER BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, 
    OR CONSEQUENTIAL DAMAGES, OF ANY KIND WHATSOEVER, ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS 
    DOCUMENTATION, EVEN IF HE HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

    THE COPYRIGHT HOLDER SPECIFICALLY DISCLAIMS ANY WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
    WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE AND ACCOMPANYING 
    DOCUMENTATION IS PROVIDED "AS IS". THE COPYRIGHT HOLDER HAS NO OBLIGATION TO PROVIDE MAINTENANCE, 
    SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
    */



public class Significance_Test implements PlugInFilter {
		
	int[] wList;
	ImagePlus baselineImages, activationImages;
	double pValue, piZero, qThreshold;
	
	public int setup(String arg, ImagePlus imp){
        	return DOES_ALL;
	}

	public void run(ImageProcessor ip) {
		if (!setDialog()) return;
		SignificanceTester ST = new SignificanceTester();
		ST.createThresholdImages(baselineImages, activationImages, pValue, piZero, qThreshold);
		IJ.run("Tile");
	}
		

	public boolean setDialog() {
		String windowTitles[] = getWindowTitles();
		GenericDialog gd = new GenericDialog("Automasker Front End", IJ.getInstance());
		gd.addChoice("Choose Baseline Image Stack:", windowTitles, windowTitles[0]);
		gd.addChoice("Choose Activation Image Stack:", windowTitles, windowTitles[1]);
		gd.addNumericField("p value threshold", 0.010, 3);
		gd.addNumericField("PI Zero for FDR", .62, 3);
		gd.addNumericField("q value threshold", 0.2, 3);
		gd.showDialog();
		if (gd.wasCanceled()) {
			return false;
		}
		int baselineImageIndex = gd.getNextChoiceIndex();
		int activationImageIndex = gd.getNextChoiceIndex();
		baselineImages = WindowManager.getImage(wList[baselineImageIndex]);
		activationImages = WindowManager.getImage(wList[activationImageIndex]);
		pValue = gd.getNextNumber();
		piZero = gd.getNextNumber();
		qThreshold = gd.getNextNumber();
		return true;
	}

	public String[] getWindowTitles() {
		wList = WindowManager.getIDList();
		if (wList==null || wList.length<2) {
		    	IJ.showMessage("There must be at least two windows open. \nYou need to have your MRE phase images open as a stack \nand your MRE anatomy images open as a separate stack.");
			return new String[0];
		}
		String[] titles = new String[wList.length];
		for (int i=0; i<wList.length; i++) {
		    	ImagePlus imp = WindowManager.getImage(wList[i]);
		    	if (imp!=null)
		        	titles[i] = imp.getTitle();
		   	else
		        	titles[i] = "";	
		}
		return titles;	
	}

}


