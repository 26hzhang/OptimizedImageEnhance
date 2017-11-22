package com.isaac.estimate;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

@SuppressWarnings("all")
public class BlkTransEstimate {

	public static double blkEstimate(Mat blkIm, double[] airlight, double lambda, double fTrans) {
		double Trans = 0.0;
		double nTrans = Math.floor(1.0 / fTrans * 128);
		double fMinCost = Double.MAX_VALUE;
		int numberOfPixels = blkIm.rows() * blkIm.cols() * blkIm.channels();
		double nCounter = 0.0;
		List<Mat> bgr = new ArrayList<>();
		Core.split(blkIm, bgr);
		while (nCounter < (1.0 - fTrans) * 10) {
			// initial dehazing process to calculate the loss information
			Mat bChannel = bgr.get(0).clone();
			bChannel = preDehaze(bChannel, airlight[0], nTrans);
			Mat gChannel = bgr.get(1).clone();
			gChannel = preDehaze(gChannel, airlight[1], nTrans);
			Mat rChannel = bgr.get(2).clone();
			rChannel = preDehaze(rChannel, airlight[2], nTrans);
			// find the pixels with over-255 value and below-0 value, and
			// calculate the sum of information loss
			double nSumOfLoss = 0.0;
			for (int i = 0; i < bChannel.rows(); i++) {
				for (int j = 0; j < bChannel.cols(); j++) {
					if (bChannel.get(i, j)[0] > 255.0) nSumOfLoss += (bChannel.get(i, j)[0] - 255.0) * (bChannel.get(i, j)[0] - 255.0);
					else if (bChannel.get(i, j)[0] < 0.0) nSumOfLoss += bChannel.get(i, j)[0] * bChannel.get(i, j)[0];
					if (gChannel.get(i, j)[0] > 255.0) nSumOfLoss += (gChannel.get(i, j)[0] - 255.0) * (gChannel.get(i, j)[0] - 255.0);
					else if (gChannel.get(i, j)[0] < 0.0) nSumOfLoss += gChannel.get(i, j)[0] * gChannel.get(i, j)[0];
					if (rChannel.get(i, j)[0] > 255.0) nSumOfLoss += (rChannel.get(i, j)[0] - 255.0) * (rChannel.get(i, j)[0] - 255.0);
					else if (rChannel.get(i, j)[0] < 0.0) nSumOfLoss += rChannel.get(i, j)[0] * rChannel.get(i, j)[0];
				}
			}
			// calculate the value of sum of square out
			double nSumOfSquareOuts = Core.sumElems(bChannel.mul(bChannel)).val[0] + Core.sumElems(gChannel.mul(gChannel)).val[0] + Core.sumElems(rChannel.mul(rChannel)).val[0];
			// calculate the value of sum of out
			double nSumOfOuts = Core.sumElems(bChannel).val[0] + Core.sumElems(gChannel).val[0] + Core.sumElems(rChannel).val[0];
			// calculate the mean value of the block image
			double fMean = nSumOfOuts / numberOfPixels;
			// calculate the cost function
			double fCost = lambda * nSumOfLoss / numberOfPixels - (nSumOfSquareOuts / numberOfPixels - fMean * fMean);
			// find the minimum cost and the related transmission
			if (nCounter == 0 || fMinCost > fCost) {
				fMinCost = fCost;
				Trans = fTrans;
			}
			fTrans = fTrans + 0.1;
			nTrans = 1.0 / fTrans * 128.0;
			nCounter = nCounter + 1;
		}
		return Trans;
	}

	public static double blkEstimateEachChannel(Mat blkIm, double airlight, double lambda, double fTrans) {
		double Trans = 0.0;
		double nTrans = Math.floor(1.0 / fTrans * 128);
		double fMinCost = Double.MAX_VALUE;
		int numberOfPixels = blkIm.rows() * blkIm.cols() * blkIm.channels();
		int nCounter = 0;
		while (nCounter < (int) (1 - fTrans) * 10) {
			// initial dehazing process to calculate the loss information
			Mat channel = blkIm.clone();
			channel = preDehaze(channel, airlight, nTrans);
			// find the pixels with over-255 value and below-0 value, and
			// calculate the sum of information loss
			double nSumOfLoss = 0.0;
			for (int i = 0; i < channel.rows(); i++) {
				for (int j = 0; j < channel.cols(); j++) {
					if (channel.get(i, j)[0] > 255.0) nSumOfLoss += (channel.get(i, j)[0] - 255.0) * (channel.get(i, j)[0] - 255.0);
					else if (channel.get(i, j)[0] < 0.0) nSumOfLoss += channel.get(i, j)[0] * channel.get(i, j)[0];
				}
			}
			// calculate the value of sum of square out
			double nSumOfSquareOuts = Core.sumElems(channel.mul(channel)).val[0];
			// calculate the value of sum of out
			double nSumOfOuts = Core.sumElems(channel).val[0];
			// calculate the mean value of the block image
			double fMean = nSumOfOuts / numberOfPixels;
			// calculate the cost function
			double fCost = lambda * nSumOfLoss / numberOfPixels - (nSumOfSquareOuts / numberOfPixels - fMean * fMean);
			// find the minimum cost and the related transmission
			if (nCounter == 0 || fMinCost > fCost) {
				fMinCost = fCost;
				Trans = fTrans;
			}
			fTrans = fTrans + 0.1;
			nTrans = 1.0 / fTrans * 128;
			nCounter = nCounter + 1;
		}
		return Trans;
	}

	private static Mat preDehaze(Mat img, double a, double nTrans) {
		// nOut = ( (blkIm - a) * nTrans + 128 * a ) / 128;
		Core.subtract(img, new Scalar(a), img);
		Core.multiply(img, new Scalar(nTrans), img);
		Core.add(img, new Scalar(128.0 * a), img);
		Core.divide(img, new Scalar(128.0), img);
		return img;
	}

}
