package com.isaac.models;

import java.util.ArrayList;
import java.util.Arrays;

import com.isaac.utils.Filters;
import com.isaac.utils.FeatureWeight;
import com.isaac.utils.ImgDecompose;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import com.isaac.estimate.AirlightEstimate;
import com.isaac.estimate.TransmissionEstimate;

public class RemoveBackScatter {

	private static final int colorBalanceRatio = 5;

	public static Mat enhance (Mat image, int blkSize, int patchSize, double lambda, double gamma, int r, double eps, int level) {
		image.convertTo(image, CvType.CV_32F);
		// image decomposition
		Mat[] decomposed = ImgDecompose.illuRefDecompose(image);
		Mat AL = decomposed[0];
		Mat RL = decomposed[1];
		// For RL
		RL = Filters.SimplestColorBalance(RL, colorBalanceRatio);
		// Calculate the air-light
		double[] airlight = AirlightEstimate.estimate(AL, blkSize);
		// estimate the transmission map
		double fTrans = 0.6;
		Mat trans = TransmissionEstimate.transEstimate(AL, patchSize, airlight, lambda, fTrans, r, eps, gamma);
		AL = dehazeProcess(AL, trans, airlight);
		// calculate the weight
		Mat w1 = calWeight(AL);
		Mat w2 = calWeight(RL);
		// Fuse
		return pyramidFuse(w1, w2, AL, RL, level);
	}
	
	private static Mat pyramidFuse(Mat w1, Mat w2, Mat img1, Mat img2, int level) {
		// Normalized weight
		Mat sumW = new Mat();
		Core.add(w1, w2, sumW);
		Core.divide(w1, sumW, w1);
		Core.multiply(w1, new Scalar(2.0), w1);
		Core.divide(w2, sumW, w2);
		Core.multiply(w2, new Scalar(2.0), w2);
		// Pyramid decomposition and reconstruct
		return ImgDecompose.fuseTwoImage(w1, img1, w2, img2, level);
	}
	
	private static Mat dehazeProcess(Mat img, Mat trans, double[] airlight) {
		Mat balancedImg = Filters.SimplestColorBalance(img, 5);
		Mat bCnl = new Mat();
		Core.extractChannel(balancedImg, bCnl, 0);
		Mat gCnl = new Mat();
		Core.extractChannel(balancedImg, gCnl, 1);
		Mat rCnl = new Mat();
		Core.extractChannel(balancedImg, rCnl, 2);
		// get mean value
		double bMean = Core.mean(bCnl).val[0];
		double gMean = Core.mean(gCnl).val[0];
		double rMean = Core.mean(rCnl).val[0];
		// get transmission map for each channel
		Mat Tb = trans.clone();
		Core.multiply(Tb, new Scalar(Math.max(bMean, Math.max(gMean, rMean)) / bMean * 0.8), Tb);
		Mat Tg = trans.clone();
		Core.multiply(Tg, new Scalar(Math.max(bMean, Math.max(gMean, rMean)) / gMean * 0.9), Tg);
		Mat Tr = trans.clone();
		Core.multiply(Tr, new Scalar(Math.max(bMean, Math.max(gMean, rMean)) / rMean * 0.8), Tr);
		// dehaze by formula
		// blue channel
		Mat bChannel = new Mat();
		Core.subtract(bCnl, new Scalar(airlight[0]), bChannel);
		Core.divide(bChannel, Tb, bChannel);
		Core.add(bChannel, new Scalar(airlight[0]), bChannel);
		// green channel
		Mat gChannel = new Mat();
		Core.subtract(gCnl, new Scalar(airlight[1]), gChannel);
		Core.divide(gChannel, Tg, gChannel);
		Core.add(gChannel, new Scalar(airlight[1]), gChannel);
		// red channel
		Mat rChannel = new Mat();
		Core.subtract(rCnl, new Scalar(airlight[2]), rChannel);
		Core.divide(rChannel, Tr, rChannel);
		Core.add(rChannel, new Scalar(airlight[2]), rChannel);
		Mat dehazed = new Mat();
		Core.merge(new ArrayList<>(Arrays.asList(bChannel, gChannel, rChannel)), dehazed);
		return dehazed;
	}
	
	private static Mat calWeight(Mat img) {
		Mat L = new Mat();
		img.convertTo(img, CvType.CV_8UC1);
		Imgproc.cvtColor(img, L, Imgproc.COLOR_BGR2GRAY);
		L.convertTo(L, CvType.CV_32F);
		Core.divide(L, new Scalar(255.0), L);
		// calculate Luminance weight
		Mat WC = FeatureWeight.LuminanceWeight(img, L);
		WC.convertTo(WC, L.type());
		// calculate the Saliency weight
		Mat WS = FeatureWeight.Saliency(img);
		WS.convertTo(WS, L.type());
		// calculate the Exposedness weight
		Mat WE = FeatureWeight.Exposedness(L);
		WE.convertTo(WE, L.type());
		// sum
		Mat weight = WC.clone();
		Core.add(weight, WS, weight);
		Core.add(weight, WE, weight);
		return weight;
	}

}
