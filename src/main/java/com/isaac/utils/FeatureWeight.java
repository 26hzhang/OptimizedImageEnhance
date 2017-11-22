package com.isaac.utils;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class FeatureWeight {

	public static Mat Saliency(Mat img) {
		// blur image with a 3x3 or 5x5 Gaussian filter
		Mat gfbgr = new Mat();
		Imgproc.GaussianBlur(img, gfbgr, new Size(3, 3), 3);
		// Perform sRGB to CIE Lab color space conversion
		Mat LabIm = new Mat();
		Imgproc.cvtColor(gfbgr, LabIm, Imgproc.COLOR_BGR2Lab);
		// Compute Lab average values (note that in the paper this average is found from the
		// un-blurred original image, but the results are quite similar)
		List<Mat> lab = new ArrayList<>();
		Core.split(LabIm, lab);
		Mat l = lab.get(0);
		l.convertTo(l, CvType.CV_32F);
		Mat a = lab.get(1);
		a.convertTo(a, CvType.CV_32F);
		Mat b = lab.get(2);
		b.convertTo(b, CvType.CV_32F);
		double lm = Core.mean(l).val[0];
		double am = Core.mean(a).val[0];
		double bm = Core.mean(b).val[0];
		// Finally compute the saliency map
		Mat sm = Mat.zeros(l.rows(), l.cols(), l.type());
		Core.subtract(l, new Scalar(lm), l);
		Core.subtract(a, new Scalar(am), a);
		Core.subtract(b, new Scalar(bm), b);
		Core.add(sm, l.mul(l), sm);
		Core.add(sm, a.mul(a), sm);
		Core.add(sm, b.mul(b), sm);
		return sm;
	}

	public static Mat LaplacianContrast(Mat img) {
		Mat laplacian = new Mat();
		Imgproc.Laplacian(img, laplacian, img.depth());
		//Imgproc.Laplacian(img, laplacian, img.depth(), 3, 1, 0);
		Core.convertScaleAbs(laplacian, laplacian);
		return laplacian;
	}

	public static Mat LocalContrast(Mat img) {
		double[] h = { 1.0 / 16.0, 4.0 / 16.0, 6.0 / 16.0, 4.0 / 16.0, 1.0 / 16.0 };
		Mat mask = new Mat(h.length, h.length, img.type());
		for (int i = 0; i < h.length; i++) {
			for (int j = 0; j < h.length; j++) {
				mask.put(i, j, h[i] * h[j]);
			}
		}
		Mat localContrast = new Mat();
		Imgproc.filter2D(img, localContrast, img.depth(), mask);
		for (int i = 0; i < localContrast.rows(); i++) {
			for (int j = 0; j < localContrast.cols(); j++) {
				if (localContrast.get(i, j)[0] > Math.PI / 2.75) localContrast.put(i, j, Math.PI / 2.75);
			}
		}
		Core.subtract(img, localContrast, localContrast);
		return localContrast.mul(localContrast);
	}

	public static Mat Exposedness(Mat img) {
		double sigma = 0.25;
		double average = 0.5;
		int rows = img.rows();
		int cols = img.cols();
		Mat exposedness = Mat.zeros(rows, cols, img.type());
		// W = exp(-(img - aver).^2 / (2*sigma^2));
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				double value = Math.exp(-1.0 * Math.pow(img.get(i, j)[0] - average, 2.0) / (2 * Math.pow(sigma, 2.0)));
				exposedness.put(i, j, value);
			}
		}
		return exposedness;
	}
	
	public static Mat LuminanceWeight(Mat img, Mat L) {
		Mat bCnl = new Mat();
		Core.extractChannel(img, bCnl, 0);
		bCnl.convertTo(bCnl, CvType.CV_32F);
		Mat gCnl = new Mat();
		Core.extractChannel(img, gCnl, 1);
		gCnl.convertTo(gCnl, CvType.CV_32F);
		Mat rCnl = new Mat();
		Core.extractChannel(img, rCnl, 2);
		rCnl.convertTo(rCnl, CvType.CV_32F);
		Mat lum = new Mat(L.rows(), L.cols(), L.type());
		for (int i = 0; i < L.rows(); i++) {
			for (int j = 0; j < L.cols(); j++) {
				double data = Math.sqrt( ( Math.pow(bCnl.get(i, j)[0] / 255.0 - L.get(i, j)[0], 2.0) +
						Math.pow(gCnl.get(i, j)[0] / 255.0 - L.get(i, j)[0], 2.0) +
						Math.pow(rCnl.get(i, j)[0] / 255.0 - L.get(i, j)[0], 2.0) ) / 3 );
				lum.put(i, j, data);
			}
		}
		return lum;
	}

}
