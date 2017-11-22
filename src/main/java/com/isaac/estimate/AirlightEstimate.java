package com.isaac.estimate;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;

public class AirlightEstimate {

	public static double[] estimate(Mat img, int blockSize) {
		int rows = img.rows();
		int cols = img.cols();
		while (rows * cols > blockSize) {
			int midRow = (int) Math.floor(rows / 2.0);
			int midCol = (int) Math.floor(cols / 2.0);
			// divided image into 4 rectangular region
			Mat[] subIm = new Mat[4];
			subIm[0] = img.submat(0, midRow, 0, midCol); // left-top corner
			subIm[1] = img.submat(midRow, rows, 0, midCol); // right-top corner
			subIm[2] = img.submat(0, midRow, midCol, cols); // left-bottom corner
			subIm[3] = img.submat(midRow, rows, midCol, cols); // right-bottom corner
			// for each sub-image, calculate its score (mean - standard deviation)
			double[] score = new double[4];
			score[0] = calculateScore(subIm[0]);
			score[1] = calculateScore(subIm[1]);
			score[2] = calculateScore(subIm[2]);
			score[3] = calculateScore(subIm[3]);
			int index = 0;
			for (int i = 1; i < score.length; i++) {
				if (score[index] < score[i]) index = i;
			}
			img = subIm[index].clone();
			rows = img.rows();
			cols = img.cols();
		}
		// get the selected region and select the correct air-light
		int index_X = 0;
		int index_Y = 0;
		double pointValue = Double.MAX_VALUE;
		for (int i = 0; i < img.rows(); i++) {
			for (int j = 0; j < img.cols(); j++) {
				double[] data = img.get(i, j);
				double tmpValue = Math.sqrt(Math.pow(data[0] - 255.0, 2.0) + Math.pow(data[1] - 255.0, 2.0) + Math.pow(data[2] - 255.0, 2.0));
				if (pointValue > tmpValue) {
					index_X = i;
					index_Y = j;
					pointValue = tmpValue;
				}
			}
		}
		return img.get(index_X, index_Y);
	}

	private static double calculateScore(Mat im) {
		MatOfDouble mean = new MatOfDouble();
		MatOfDouble std = new MatOfDouble();
		Core.meanStdDev(im, mean, std);
		double[] means = mean.get(0, 0);
		double[] stds = std.get(0, 0);
		double score = 0.0;
		for (int i = 0; i < means.length; i++) score += means[i] - stds[i];
		return score;
	}

}