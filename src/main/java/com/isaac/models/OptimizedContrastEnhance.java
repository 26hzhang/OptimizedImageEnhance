package com.isaac.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.isaac.estimate.AirlightEstimate;
import com.isaac.estimate.TransmissionEstimate;
import com.isaac.utils.Filters;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class OptimizedContrastEnhance {

	public static Mat enhance(Mat image, int blkSize, int patchSize, double lambda, double eps, int krnlSize) {
		image.convertTo(image, CvType.CV_32F);
		// obtain air-light
		double[] airlight = AirlightEstimate.estimate(image, blkSize);
		// obtain coarse transmission map
		double fTrans = 0.5;
		Mat T = TransmissionEstimate.transEstimate(image, patchSize, airlight, lambda, fTrans);
		// refine the transmission map
		Mat gray = new Mat();
		Imgproc.cvtColor(image, gray, Imgproc.COLOR_RGB2GRAY);
		Core.divide(gray, new Scalar(255.0), gray);
		T = Filters.GuidedImageFilter(gray, T, krnlSize, eps);
		// dehaze
		List<Mat> bgr = new ArrayList<>();
		Core.split(image, bgr);
		Mat bChannel = dehaze(bgr.get(0), T, airlight[0]);
		//Core.normalize(bChannel, bChannel, 0, 255, Core.NORM_MINMAX);
		Mat gChannel = dehaze(bgr.get(1), T, airlight[1]);
		//Core.normalize(gChannel, gChannel, 0, 255, Core.NORM_MINMAX);
		Mat rChannel = dehaze(bgr.get(2), T, airlight[2]);
		//Core.normalize(rChannel, rChannel, 0, 255, Core.NORM_MINMAX);
		Mat dehazedImg = new Mat();
		Core.merge(new ArrayList<>(Arrays.asList(bChannel, gChannel, rChannel)), dehazedImg);
		return dehazedImg;
	}

	@SuppressWarnings("unused")
	public static Mat enhanceEachChannel(Mat image, int blkSize, int patchSize, double lambda, double eps, int krnlSize) {
		image.convertTo(image, CvType.CV_32F);
		// split image to three channels
		List<Mat> bgr = new ArrayList<>();
		Core.split(image, bgr);
		Mat bChannel = bgr.get(0);
		Mat gChannel = bgr.get(1);
		Mat rChannel = bgr.get(2);
		// obtain air-light
		double[] airlight = AirlightEstimate.estimate(image, blkSize);
		// obtain coarse transmission map and refine it for each channel
		double fTrans = 0.3;
		Mat T = TransmissionEstimate.transEstimateEachChannel(bChannel, patchSize, airlight[0], lambda, fTrans);
		Core.subtract(T, new Scalar(1.0), T);
		Core.multiply(T, new Scalar(-1.0), T);
		Mat Tb = Filters.GuidedImageFilter(bChannel, T, krnlSize, eps);
		T = TransmissionEstimate.transEstimateEachChannel(gChannel, patchSize, airlight[1], lambda, fTrans);
		Core.subtract(T, new Scalar(1.0), T);
		Core.multiply(T, new Scalar(-1.0), T);
		Mat Tg = Filters.GuidedImageFilter(gChannel, T, krnlSize, eps);
		T = TransmissionEstimate.transEstimateEachChannel(rChannel, patchSize, airlight[2], lambda, fTrans);
		Core.subtract(T, new Scalar(1.0), T);
		Core.multiply(T, new Scalar(-1.0), T);
		Mat Tr = Filters.GuidedImageFilter(rChannel, T, krnlSize, eps);
		// dehaze
		bChannel = dehaze(bChannel, Tb, airlight[0]);
		gChannel = dehaze(gChannel, Tg, airlight[1]);
		rChannel = dehaze(rChannel, Tr, airlight[2]);
		Mat outval = new Mat();
		Core.merge(new ArrayList<>(Arrays.asList(bChannel, gChannel, rChannel)), outval);
		return outval;
	}

	private static Mat dehaze(Mat img, Mat T, double airlight) {
		// J = (img - airlight) ./ T + airlight;
		Core.subtract(img, new Scalar(airlight), img);
		Core.divide(img, T, img);
		Core.add(img, new Scalar(airlight), img);
		return img;
	}
}
