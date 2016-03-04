package com.ericbarnhill.ijtoolkit.fdr;
import ij.IJ;
import ij.ImagePlus;
import ij.process.FloatProcessor;

import java.util.Arrays;
import java.util.Comparator;

import org.apache.commons.math4.stat.descriptive.moment.Mean;
import org.apache.commons.math4.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math4.stat.inference.TTest;

public class SignificanceTester {

	public SignificanceTester() { }
	
	double piZero;
	int width, height;
	
	public void createThresholdImages(ImagePlus baselineImages, ImagePlus activationImages, 
			double pValue, double piZero, double qThreshold) {
		this.piZero = piZero;
		this.width = baselineImages.getWidth();
		this.height = baselineImages.getHeight();
		FloatProcessor pValues = new FloatProcessor(width, height);
		FloatProcessor tValues = new FloatProcessor(width, height);
		FloatProcessor meanBase = new FloatProcessor(width, height);
		FloatProcessor meanAct = new FloatProcessor(width, height);
		FloatProcessor stdDevBase = new FloatProcessor(width, height);
		FloatProcessor stdDevAct = new FloatProcessor(width, height);
		FloatProcessor thresholdedValues = new FloatProcessor(width, height);

		for (int i = 0 ; i < baselineImages.getWidth(); i++) {
			for (int j = 0; j < baselineImages.getHeight(); j++) {
				//Assemble pixel array for this pixel, in baseline and active images
				int pixelArraySize = baselineImages.getImageStackSize();
				double[] baselinePixelValues = new double[pixelArraySize];
				double[] activationPixelValues = new double[pixelArraySize];
				//t-test the two sets and create p value image
				for (int k = 0; k < pixelArraySize; k++) {
					baselinePixelValues[k] = baselineImages.getStack().getProcessor(k+1).getPixelValue(i, j);
					activationPixelValues[k] = activationImages.getStack().getProcessor(k+1).getPixelValue(i, j);
				}
				//double[] allValues = ArrayUtils.addAll(baselinePixelValues, activationPixelValues);
				TTest T = new TTest();
				StandardDeviation SD = new StandardDeviation();
				Mean mean = new Mean();
				pValues.putPixelValue(i, j, T.pairedTTest(baselinePixelValues, activationPixelValues));
				tValues.putPixelValue(i, j, -T.pairedT(baselinePixelValues, activationPixelValues)  );
				meanBase.putPixelValue(i, j, mean.evaluate(baselinePixelValues));
				meanAct.putPixelValue(i, j, mean.evaluate(activationPixelValues));
				stdDevBase.putPixelValue(i, j, SD.evaluate(baselinePixelValues));
				stdDevAct.putPixelValue(i, j, SD.evaluate(activationPixelValues));	
			//if less then p-value threshold, subtract active from baseline and normalize to baseline
				// then add to thresholded image
				if (T.pairedTTest(baselinePixelValues, activationPixelValues, pValue) ) {
					double baselineMeanValue = mean.evaluate(baselinePixelValues);
					double activationMeanValue = mean.evaluate(activationPixelValues);
					double differenceValue = activationMeanValue - baselineMeanValue;
					double normalizedActivation = differenceValue / baselineMeanValue;
					thresholdedValues.putPixelValue(i, j, normalizedActivation);
				}
			}
		}
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if ( Float.isNaN( pValues.getPixelValue(x, y) ) ) {
					pValues.putPixelValue(x, y, 1);
				}
			}
		}
		FloatProcessor qValues = qValueImage(pValues);
		
		FloatProcessor qThresholdMap = new FloatProcessor(width, height);
		double baseActiv, meanActiv, actOverBase;
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if ( qValues.getPixelValue(x,y) < qThreshold ) {
						baseActiv = meanBase.getPixelValue(x,y);
						meanActiv = meanAct.getPixelValue(x, y);
						actOverBase = meanActiv/baseActiv;
						qThresholdMap.putPixelValue(x, y, actOverBase );
				}
			}
		}
		
		pValues.setMinAndMax(0,1);
		tValues.setMinAndMax(-4, 4);
		stdDevBase.setMinAndMax(0, 5000);
		stdDevAct.setMinAndMax(0, 5000);
		meanBase.setMinAndMax(0, 5000);
		meanAct.setMinAndMax(0, 5000);
		thresholdedValues.setMinAndMax(0,2);
		qThresholdMap.setMinAndMax(0,2);
		ImagePlus pValuesMap = new ImagePlus("P Values", pValues);
		ImagePlus qValuesMap = new ImagePlus("Q Values", qValues);
		ImagePlus tMap = new ImagePlus("T Statistic", tValues);
		ImagePlus stdDevBaseMap = new ImagePlus("Standard Deviation, Baseline", stdDevBase);
		ImagePlus stdDevActMap = new ImagePlus("Standard Deviation, Activation", stdDevAct);
		ImagePlus meanBaseMap = new ImagePlus("Mean Baseline", meanBase);
		ImagePlus meanActMap = new ImagePlus("Mean Activation", meanAct);
		ImagePlus thresholdedP = new ImagePlus("Thresholded P", thresholdedValues);
		ImagePlus thresholdedQ = new ImagePlus("Thresholded Q", qThresholdMap);
		pValuesMap.show();
		qValuesMap.show();
		tMap.show();
		stdDevBaseMap.show();
		stdDevActMap.show();
		meanBaseMap.show();
		meanActMap.show();
		thresholdedP.show();
		thresholdedQ.show();
		IJ.selectWindow("P Values");
		IJ.run("EB-rainbow-plus-black");
		IJ.run("Invert LUT");
		IJ.selectWindow("Q Values");
		IJ.run("EB-rainbow-plus-black");
		IJ.run("Invert LUT");
		IJ.selectWindow("T Statistic");
		IJ.run("Red Hot");
		IJ.selectWindow("Standard Deviation, Baseline");
		IJ.run("EB-rainbow-plus-black");
		IJ.selectWindow("Standard Deviation, Activation");
		IJ.run("EB-rainbow-plus-black");
		IJ.selectWindow("Mean Baseline");
		IJ.run("EB-rainbow-plus-black");
		IJ.selectWindow("Mean Activation");
		IJ.run("EB-rainbow-plus-black");
		IJ.selectWindow("Thresholded P");
		IJ.run("Fire");
		IJ.selectWindow("Thresholded Q");
		IJ.run("Fire");
		IJ.run("Set NaN to Zero");
		//IJ.run("Smooth");		
	}
	
	public FloatProcessor qValueImage(FloatProcessor pValues) {
		float[][] pixelArray = new float[width*height][4];
		// 1 = pixel index 2 = p value 3 = ranking 4 = q value
		int validPixelTally = 0;
		// set up pixel array
		for (int i = 0; i < pValues.getWidth(); i++) {
			for (int j = 0; j < pValues.getHeight(); j++) {
				int index = j*width + i;
				float pixelValue = pValues.getPixelValue(i,j);
				if (pixelValue <= 0) pixelValue = 1;
				if (Double.isNaN(pixelValue)) pixelValue = 1;
				pixelArray[index][0] = index;
				pixelArray[index][1] = pixelValue;
			}
		}
		
		//sort array to get ranking
		Arrays.sort(pixelArray, new Comparator<float[]>() {
			@Override
			public int compare(float[] entry1, float[] entry2) {
				if (entry1[1] > entry2[1]) {
					return 1;
				} else return 0;
			}
		});
		
		// put ranking in third value and get total valid pixels
		for (int i = 0; i < pixelArray.length; i++) {
			pixelArray[i][2] = i;
			if (pixelArray[i][1] < 1) validPixelTally++;
		}
		
		// put q score in fourth value
		for (int i = 0; i < pixelArray.length; i++) {
			if (pixelArray[i][1] < 1) {
				double qScore = pixelArray[i][1]  * validPixelTally / pixelArray[i][2] * piZero;
				pixelArray[i][3] = (float)qScore;
			} else pixelArray[i][3] = 1;
		}
		
		//resort by image order
		Arrays.sort(pixelArray, new Comparator<float[]>() {
			@Override
			public int compare(float[] entry1, float[] entry2) {
				if (entry1[0] > entry2[0]) {
					return 1;
				} else return 0;
			}
		});
		
		FloatProcessor qValues = new FloatProcessor( width, height );
		
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				qValues.putPixelValue(i, j, pixelArray[j*width+i][3]);
			}
		}
		
		return qValues;		
	}
	
}

