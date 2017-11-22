package com.isaac.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import com.isaac.utils.Filters;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class ALTMRetinex {

	// Global Adaptation Parameters -- fixed
	private static final double rParam = 0.299;
	private static final double gParam = 0.587;
	private static final double bParam = 0.114;

	public static Mat enhance(Mat image, int r, double eps, double eta, double lambda, double krnlRatio) {
		image.convertTo(image, CvType.CV_32F);
		// extract each color channel
		List<Mat> bgr = new ArrayList<>();
		Core.split(image, bgr);
		Mat bChannel = bgr.get(0);
		Mat gChannel = bgr.get(1);
		Mat rChannel = bgr.get(2);
		int m = rChannel.rows();
		int n = rChannel.cols();
		// Global Adaptation
		List<Mat> list = globalAdaptation(bChannel, gChannel, rChannel, m, n);
		Mat Lw = list.get(0);
		Mat Lg = list.get(1);
		// Local Adaptation
		Mat Hg = localAdaptation(Lg, m, n, r, eps, krnlRatio);
		Lg.convertTo(Lg, CvType.CV_32F);
		// process
		Mat alpha = new Mat(m, n, rChannel.type());
		Core.divide(Lg, new Scalar(Core.minMaxLoc(Lg).maxVal / eta), alpha);
		//Core.multiply(alpha, new Scalar(eta), alpha);
		Core.add(alpha, new Scalar(1.0), alpha);
		//alpha = adjustment(alpha, 1.25);
		Mat Lg_ = new Mat(m, n, rChannel.type());
		Core.add(Lg, new Scalar(1.0 / 255.0), Lg_);
		Core.log(Lg_, Lg_);
		double beta = Math.exp(Core.sumElems(Lg_).val[0] / (m * n)) * lambda;
		Mat Lout = new Mat(m, n, rChannel.type());
		Core.divide(Lg, Hg, Lout);
		Core.add(Lout, new Scalar(beta), Lout);
		Core.log(Lout, Lout);
		Core.normalize(alpha.mul(Lout), Lout, 0, 255, Core.NORM_MINMAX);
		Mat gain = obtainGain(Lout, Lw, m, n);
		// output
		Core.divide(rChannel.mul(gain), new Scalar(Core.minMaxLoc(rChannel).maxVal / 255.0), rChannel); // Red Channel
		Core.divide(gChannel.mul(gain), new Scalar(Core.minMaxLoc(gChannel).maxVal / 255.0), gChannel); // Green Channel
		Core.divide(bChannel.mul(gain), new Scalar(Core.minMaxLoc(bChannel).maxVal / 255.0), bChannel); // Blue Channel
		// merge three color channels to a image
		Mat outval = new Mat();
		Core.merge(new ArrayList<>(Arrays.asList(bChannel, gChannel, rChannel)), outval);
		outval.convertTo(outval, CvType.CV_8UC1);
		return outval;
	}

	private static Mat localAdaptation(Mat Lg, int rows, int cols, int r, double eps, double krnlRatio) {
		int krnlSz = Stream.of(3.0, rows * krnlRatio, cols * krnlRatio).max(Double::compare).orElse(3.0).intValue();
		// maximum filter: using dilate to extract the local maximum of a image block, which acts as the maximum filter
		// Meanwhile, minimum filter can be achieved by using erode function
		Mat Lg_ = new Mat();
		Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(krnlSz, krnlSz), new Point(-1, -1));
		Imgproc.dilate(Lg, Lg_, kernel);
		// guided image filter
		return Filters.GuidedImageFilter(Lg, Lg_, r, eps);
	}

	private static List<Mat> globalAdaptation(Mat b, Mat g, Mat r, int rows, int cols) {
		// Calculate Lw & maximum of Lw
		Mat Lw = new Mat(rows, cols, r.type());
		Core.multiply(r, new Scalar(rParam), r);
		Core.multiply(g, new Scalar(gParam), g);
		Core.multiply(b, new Scalar(bParam), b);
		Core.add(r, g, Lw);
		Core.add(Lw, b, Lw);
		double LwMax = Core.minMaxLoc(Lw).maxVal; // the maximum luminance value
		// Calculate log-average luminance and get global adaptation result
		Mat Lw_ = Lw.clone();
		Core.add(Lw_, new Scalar(0.001), Lw_);
		Core.log(Lw_, Lw_);
		double LwAver = Math.exp(Core.sumElems(Lw_).val[0] / (rows * cols));
		Mat Lg = Lw.clone();
		Core.divide(Lg, new Scalar(LwAver), Lg);
		Core.add(Lg, new Scalar(1.0), Lg);
		Core.log(Lg, Lg);
		Core.divide(Lg, new Scalar(Math.log(LwMax / LwAver + 1.0)), Lg); // Lg is the global adaptation
		List<Mat> list = new ArrayList<>();
		list.add(Lw);
		list.add(Lg);
		return list;
	}

	private static Mat obtainGain(Mat Lout, Mat Lw, int rows, int cols) {
		Mat gain = new Mat(rows, cols, Lout.type());
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				if (Lw.get(i, j)[0] == 0) gain.put(i, j, Lout.get(i, j)[0]);
				else gain.put(i, j, Lout.get(i, j)[0] / Lw.get(i, j)[0]);
			}
		}
		return gain;
	}

	@SuppressWarnings("unused")
	private static Mat adjustment(Mat alpha, double a) {
		double b = Core.minMaxLoc(alpha).maxVal;
		int rows = alpha.rows();
		int cols = alpha.cols();
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				//double val = alpha.get(i, j)[0];
				alpha.put(i, j, (2 * Math.atan(a * alpha.get(i, j)[0] / b) / Math.PI * b));
			}
		}
		return alpha;
	}

}
