package com.isaac.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

@SuppressWarnings("all")
public class ImgDecompose {
	
	public static Mat[] illuRefDecompose(Mat img) {
		List<Mat> AList = new ArrayList<>();
		List<Mat> RList = new ArrayList<>();
		List<Mat> bgr = new ArrayList<>();
		Core.split(img, bgr);
		for (Mat cnl : bgr) {
			Mat alCnl = cnl.clone();
			Mat rlcnl = cnl.clone();
			double maxVal = Core.minMaxLoc(cnl).maxVal;
			Mat k = new Mat();
			Core.multiply(cnl, new Scalar(0.5 / maxVal), k);
			rlcnl = k.mul(rlcnl);
			Core.subtract(alCnl, rlcnl, alCnl);
			AList.add(alCnl);
			RList.add(rlcnl);
		}
		Mat Al = new Mat();
		Core.merge(AList, Al);
		Mat Rl = new Mat();
		Core.merge(RList, Rl);
		return new Mat[]{Al, Rl};
	}

	public static Mat[] buildGaussianPyramid(Mat img, int level) {
		Mat[] gaussPyr = new Mat[level];
		Mat mask = filterMask(img);
		Mat tmp = new Mat();
		Imgproc.filter2D(img, tmp, -1, mask);
		gaussPyr[0] = tmp.clone();
		Mat tmpImg = img.clone();
		for (int i = 1; i < level; i++) {
			// resize image
			Imgproc.resize(tmpImg, tmpImg, new Size(), 0.5, 0.5, Imgproc.INTER_LINEAR);
			// blur image
			tmp = new Mat();
			Imgproc.filter2D(tmpImg, tmp, -1, mask);
			gaussPyr[i] = tmp.clone();
		}
		return gaussPyr;
	}

	public static Mat[] buildLaplacianPyramid(Mat img, int level) {
		Mat[] lapPyr = new Mat[level];
		lapPyr[0] = img.clone();
		Mat tmpImg = img.clone();
		for (int i = 1; i < level; i++) {
			// resize image
			Imgproc.resize(tmpImg, tmpImg, new Size(), 0.5, 0.5, Imgproc.INTER_LINEAR);
			lapPyr[i] = tmpImg.clone();
		}
		// calculate the DoG
		for (int i = 0; i < level - 1; i++) {
			Mat tmpPyr = new Mat();
			Imgproc.resize(lapPyr[i + 1], tmpPyr, lapPyr[i].size(), 0, 0, Imgproc.INTER_LINEAR);
			Core.subtract(lapPyr[i], tmpPyr, lapPyr[i]);
		}
		return lapPyr;
	}

	public static Mat reconstructLaplacianPyramid(Mat[] pyramid) {
		int level = pyramid.length;
		for (int i = level - 1; i > 0; i--) {
			Mat tmpPyr = new Mat();
			Imgproc.resize(pyramid[i], tmpPyr, pyramid[i - 1].size(), 0, 0, Imgproc.INTER_LINEAR);
			Core.add(pyramid[i - 1], tmpPyr, pyramid[i - 1]);
		}
		return pyramid[0];
	}

	private static Mat filterMask(Mat img) {
		double[] h = { 1.0 / 16.0, 4.0 / 16.0, 6.0 / 16.0, 4.0 / 16.0, 1.0 / 16.0 };
		Mat mask = new Mat(h.length, h.length, img.type());
		for (int i = 0; i < h.length; i++) {
			for (int j = 0; j < h.length; j++) {
				mask.put(i, j, h[i] * h[j]);
			}
		}
		return mask;
	}

	public static Mat fuseTwoImage (Mat w1, Mat img1, Mat w2, Mat img2, int level) {
		// construct the gaussian pyramid for weight
		Mat[] weight1 = ImgDecompose.buildGaussianPyramid(w1, level);
		Mat[] weight2 = ImgDecompose.buildGaussianPyramid(w2, level);
		// construct the laplacian pyramid for input image channel
		img1.convertTo(img1, CvType.CV_32F);
		img2.convertTo(img2, CvType.CV_32F);
		List<Mat> bgr = new ArrayList<>();
		Core.split(img1, bgr);
		Mat[] bCnl1 = ImgDecompose.buildLaplacianPyramid(bgr.get(0), level);
		Mat[] gCnl1 = ImgDecompose.buildLaplacianPyramid(bgr.get(1), level);
		Mat[] rCnl1 = ImgDecompose.buildLaplacianPyramid(bgr.get(2), level);
		bgr.clear();
		Core.split(img2, bgr);
		Mat[] bCnl2 = ImgDecompose.buildLaplacianPyramid(bgr.get(0), level);
		Mat[] gCnl2 = ImgDecompose.buildLaplacianPyramid(bgr.get(1), level);
		Mat[] rCnl2 = ImgDecompose.buildLaplacianPyramid(bgr.get(2), level);
		// fusion process
		Mat[] bCnl = new Mat[level];
		Mat[] gCnl = new Mat[level];
		Mat[] rCnl = new Mat[level];
		for (int i = 0; i < level; i++) {
			Mat cn = new Mat();
			Core.add(bCnl1[i].mul(weight1[i]), bCnl2[i].mul(weight2[i]), cn);
			bCnl[i] = cn.clone();
			Core.add(gCnl1[i].mul(weight1[i]), gCnl2[i].mul(weight2[i]), cn);
			gCnl[i] = cn.clone();
			Core.add(rCnl1[i].mul(weight1[i]), rCnl2[i].mul(weight2[i]), cn);
			rCnl[i] = cn.clone();
		}
		// reconstruct & output
		Mat bChannel = ImgDecompose.reconstructLaplacianPyramid(bCnl);
		Mat gChannel = ImgDecompose.reconstructLaplacianPyramid(gCnl);
		Mat rChannel = ImgDecompose.reconstructLaplacianPyramid(rCnl);
		Mat fusion = new Mat();
		Core.merge(new ArrayList<>(Arrays.asList(bChannel, gChannel, rChannel)), fusion);
		return fusion;
	}
	
}
