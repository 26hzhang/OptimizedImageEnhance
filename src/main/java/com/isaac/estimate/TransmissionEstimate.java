package com.isaac.estimate;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import com.isaac.utils.Filters;

public class TransmissionEstimate {

	public static Mat transEstimate(Mat img, int patchSz, double[] airlight, double lambda, double fTrans) {
		int rows = img.rows();
		int cols = img.cols();
		List<Mat> bgr = new ArrayList<>();
		Core.split(img, bgr);
		int type = bgr.get(0).type();
		// calculate the transmission map
		return computeTrans(img, patchSz, rows, cols, type, airlight, lambda, fTrans);
	}

	public static Mat transEstimateEachChannel(Mat img, int patchSz, double airlight, double lambda, double fTrans) {
		int rows = img.rows();
		int cols = img.cols();
		Mat T = new Mat(rows, cols, img.type());
		for (int i = 0; i < rows; i += patchSz) {
			for (int j = 0; j < cols; j += patchSz) {
				int endRow = i + patchSz > rows ? rows : i + patchSz;
				int endCol = j + patchSz > cols ? cols : j + patchSz;
				Mat blkIm = img.submat(i, endRow, j, endCol);
				double Trans = BlkTransEstimate.blkEstimateEachChannel(blkIm, airlight, lambda, fTrans);
				for (int m = i; m < endRow; m++) for (int n = j; n < endCol; n++) T.put(m, n, Trans);
			}
		}
		return T;
	}
	
	public static Mat transEstimate(Mat img, int patchSz, double[] airlight, double lambda, double fTrans, 
			int r, double eps, double gamma) {
		int rows = img.rows();
		int cols = img.cols();
		List<Mat> bgr = new ArrayList<>();
		Core.split(img, bgr);
		int type = bgr.get(0).type();
		// calculate the transmission map
		Mat T = computeTrans(img, patchSz, rows, cols, type, airlight, lambda, fTrans);
		// refine the transmission map
		img.convertTo(img, CvType.CV_8UC1);
		Mat gray = new Mat();
		Imgproc.cvtColor(img, gray, Imgproc.COLOR_BGR2GRAY);
		gray.convertTo(gray, CvType.CV_32F);
		Core.divide(gray, new Scalar(255.0), gray);
		T = Filters.GuidedImageFilter(gray, T, r, eps);
		Mat Tsmooth = new Mat();
		Imgproc.GaussianBlur(T, Tsmooth, new Size(81, 81), 40);
		Mat Tdetails = new Mat();
		Core.subtract(T, Tsmooth, Tdetails);
		Core.multiply(Tdetails, new Scalar(gamma), Tdetails);
		Core.add(Tsmooth, Tdetails, T);
		return T;
	}

	private static Mat computeTrans (Mat img, int patchSz, int rows, int cols, int type, double[] airlight, double lambda, double fTrans) {
		Mat T = new Mat(rows, cols, type);
		for (int i = 0; i < rows; i += patchSz) {
			for (int j = 0; j < cols; j += patchSz) {
				int endRow = i + patchSz > rows ? rows : i + patchSz;
				int endCol = j + patchSz > cols ? cols : j + patchSz;
				Mat blkIm = img.submat(i, endRow, j, endCol);
				double Trans = BlkTransEstimate.blkEstimate(blkIm, airlight, lambda, fTrans);
				for (int m = i; m < endRow; m++) for (int n = j; n < endCol; n++) T.put(m, n, Trans);
			}
		}
		return T;
	}

}
