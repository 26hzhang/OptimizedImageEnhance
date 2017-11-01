package com.isaac.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.isaac.utils.Filters;
import com.isaac.utils.FeatureWeight;
import com.isaac.utils.ImgDecompose;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.CLAHE;
import org.opencv.imgproc.Imgproc;

public class FusionEnhance {

	public static Mat enhance (Mat image, int level) {
		// color balance
		Mat img1 = Filters.SimplestColorBalance(image, 5);
		img1.convertTo(img1, CvType.CV_8UC1);
		// Perform sRGB to CIE Lab color space conversion
		Mat LabIm1 = new Mat();
		Imgproc.cvtColor(img1, LabIm1, Imgproc.COLOR_BGR2Lab);
		Mat L1 = new Mat();
		Core.extractChannel(LabIm1, L1, 0);
		// apply CLAHE
		Mat[] result = applyCLAHE(LabIm1, L1);
		Mat img2 = result[0];
		Mat L2 = result[1];
		// calculate normalized weight
		Mat w1 = calWeight(img1, L1);
		Mat w2 = calWeight(img2, L2);
		Mat sumW = new Mat();
		Core.add(w1, w2, sumW);
		Core.divide(w1, sumW, w1);
		Core.divide(w2, sumW, w2);
		// merge image1 and image2
		return ImgDecompose.fuseTwoImage(w1, img1, w2, img2, level);
	}

	private static Mat[] applyCLAHE(Mat img, Mat L) {
		Mat[] result = new Mat[2];
		CLAHE clahe = Imgproc.createCLAHE();
		clahe.setClipLimit(2.0);
		Mat L2 = new Mat();
		clahe.apply(L, L2);
		Mat LabIm2 = new Mat();
		List<Mat> lab = new ArrayList<>();
		Core.split(img, lab);
		Core.merge(new ArrayList<>(Arrays.asList(L2, lab.get(1), lab.get(2))), LabIm2);
		Mat img2 = new Mat();
		Imgproc.cvtColor(LabIm2, img2, Imgproc.COLOR_Lab2BGR);
		result[0] = img2;
		result[1] = L2;
		return result;
	}

	private static Mat calWeight(Mat img, Mat L) {
		Core.divide(L, new Scalar(255.0), L);
		L.convertTo(L, CvType.CV_32F);
		// calculate laplacian contrast weight
		Mat WL = FeatureWeight.LaplacianContrast(L);
		WL.convertTo(WL, L.type());
		// calculate Local contrast weight
		Mat WC = FeatureWeight.LocalContrast(L);
		WC.convertTo(WC, L.type());
		// calculate the saliency weight
		Mat WS = FeatureWeight.Saliency(img);
		WS.convertTo(WS, L.type());
		// calculate the exposedness weight
		Mat WE = FeatureWeight.Exposedness(L);
		WE.convertTo(WE, L.type());
		// sum
		Mat weight = WL.clone();
		Core.add(weight, WC, weight);
		Core.add(weight, WS, weight);
		Core.add(weight, WE, weight);
		return weight;
	}

}
