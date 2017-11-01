package com.isaac.utils;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class Filters {

	/**
	 * Simplest Color Balance. Performs color balancing via histogram
	 * normalization.
	 *
	 * @param img input color or gray scale image
	 * @param percent controls the percentage of pixels to clip to white and black. (normally, choose 1~10)
	 * @return Balanced image in CvType.CV_32F
	 */
	public static Mat SimplestColorBalance(Mat img, int percent) {
		if (percent <= 0)
			percent = 5;
		img.convertTo(img, CvType.CV_32F);
		List<Mat> channels = new ArrayList<>();
		int rows = img.rows(); // number of rows of image
		int cols = img.cols(); // number of columns of image
		int chnls = img.channels(); //  number of channels of image
		double halfPercent = percent / 200.0;
		if (chnls == 3) Core.split(img, channels);
		else channels.add(img);
		List<Mat> results = new ArrayList<>();
		for (int i = 0; i < chnls; i++) {
			// find the low and high precentile values (based on the input percentile)
			Mat flat = new Mat();
			channels.get(i).reshape(1, 1).copyTo(flat);
			Core.sort(flat, flat, Core.SORT_ASCENDING);
			double lowVal = flat.get(0, (int) Math.floor(flat.cols() * halfPercent))[0];
			double topVal = flat.get(0, (int) Math.ceil(flat.cols() * (1.0 - halfPercent)))[0];
			// saturate below the low percentile and above the high percentile
			Mat channel = channels.get(i);
			for (int m = 0; m < rows; m++) {
				for (int n = 0; n < cols; n++) {
					if (channel.get(m, n)[0] < lowVal) channel.put(m, n, lowVal);
					if (channel.get(m, n)[0] > topVal) channel.put(m, n, topVal);
				}
			}
			Core.normalize(channel, channel, 0.0, 255.0 / 2, Core.NORM_MINMAX);
			channel.convertTo(channel, CvType.CV_32F);
			results.add(channel);
		}
		Mat outval = new Mat();
		Core.merge(results, outval);
		return outval;
	}

	/**
	 * Guided Image Filter for grayscale image, O(1) time implementation of guided filter
	 *
	 * @param I guidance image (should be a gray-scale/single channel image)
	 * @param p filtering input image (should be a gray-scale/single channel image)
	 * @param r local window radius
	 * @param eps regularization parameter
	 * @return filtered image
	 */
	public static Mat GuidedImageFilter(Mat I, Mat p, int r, double eps) {
		I.convertTo(I, CvType.CV_64FC1);
		p.convertTo(p, CvType.CV_64FC1);
		//[hei, wid] = size(I);
		int rows = I.rows();
		int cols = I.cols();
		// N = boxfilter(ones(hei, wid), r); % the size of each local patch; N=(2r+1)^2 except for boundary pixels.
		Mat N = new Mat();
		Imgproc.boxFilter(Mat.ones(rows, cols, I.type()), N, -1, new Size(r, r));
		// mean_I = boxfilter(I, r) ./ N;
		Mat mean_I = new Mat();
		Imgproc.boxFilter(I, mean_I, -1, new Size(r, r));
		// mean_p = boxfilter(p, r) ./ N
		Mat mean_p = new Mat();
		Imgproc.boxFilter(p, mean_p, -1, new Size(r, r));
		// mean_Ip = boxfilter(I.*p, r) ./ N;
		Mat mean_Ip = new Mat();
		Imgproc.boxFilter(I.mul(p), mean_Ip, -1, new Size(r, r));
		// cov_Ip = mean_Ip - mean_I .* mean_p; % this is the covariance of (I, p) in each local patch.
		Mat cov_Ip = new Mat();
		Core.subtract(mean_Ip, mean_I.mul(mean_p), cov_Ip);
		// mean_II = boxfilter(I.*I, r) ./ N;
		Mat mean_II = new Mat();
		Imgproc.boxFilter(I.mul(I), mean_II, -1, new Size(r, r));
		// var_I = mean_II - mean_I .* mean_I;
		Mat var_I = new Mat();
		Core.subtract(mean_II, mean_I.mul(mean_I), var_I);
		// a = cov_Ip ./ (var_I + eps); % Eqn. (5) in the paper;
		Mat a = new Mat();
		Core.add(var_I, new Scalar(eps), a);
		Core.divide(cov_Ip, a, a);
		//b = mean_p - a .* mean_I; % Eqn. (6) in the paper;
		Mat b = new Mat();
		Core.subtract(mean_p, a.mul(mean_I), b);
		// mean_a = boxfilter(a, r) ./ N;
		Mat mean_a = new Mat();
		Imgproc.boxFilter(a, mean_a, -1, new Size(r, r));
		Core.divide(mean_a, N, mean_a);
		// mean_b = boxfilter(b, r) ./ N;
		Mat mean_b = new Mat();
		Imgproc.boxFilter(b, mean_b, -1, new Size(r, r));
		Core.divide(mean_b, N, mean_b);
		// q = mean_a .* I + mean_b; % Eqn. (8) in the paper;
		Mat q = new Mat();
		Core.add(mean_a.mul(I), mean_b, q);
		q.convertTo(q, CvType.CV_32F);
		return q;
	}
	
	/**
	 * Guided Image Filter for Color Image
	 * @param origI guidance image (should be a gray-scale/single channel image)
	 * @param p filtering input image (should be a gray-scale/single channel image)
	 * @param r local window radius
	 * @param eps regularization parameter
	 * @param s blocks number, s * s
	 * @param depth image depth
	 * @return filtered image
	 */
	@SuppressWarnings("unused")
	public static Mat GuidedImageFilter_Color(Mat origI, Mat p, int r, double eps, double s, int depth) {
		//Pre-defined parameters
		ArrayList<Mat> Ichannels = new ArrayList<>();
		ArrayList<Mat> Isubchannels = new ArrayList<>();
		int Idepth;
		double r_sub;
		Mat mean_I_r;
		Mat mean_I_g;
		Mat mean_I_b;
		Mat invrr = new Mat();
		Mat invrg = new Mat();
		Mat invrb = new Mat();
		Mat invgg = new Mat();
		Mat invgb = new Mat();
		Mat invbb = new Mat();
		// Process
		Mat I;
		if (origI.depth() == CvType.CV_32F || origI.depth() == CvType.CV_64F) {
			I = origI.clone();
		} else {
			I = convertTo(origI, CvType.CV_32F);
		}
		Idepth = I.depth();
		Core.split(I, Ichannels);
		Mat I_sub = new Mat();
		Imgproc.resize(I, I_sub, new Size(I.cols() / s, I.rows() / s), 0.0, 0.0, Imgproc.INTER_NEAREST);
		Core.split(I_sub, Isubchannels);
		r_sub = r / s;
		mean_I_r = boxfilter(Isubchannels.get(0), (int) r_sub);
		mean_I_g = boxfilter(Isubchannels.get(1), (int) r_sub);
		mean_I_b = boxfilter(Isubchannels.get(2), (int) r_sub);

		// variance of I in each local patch: the matrix Sigma in Eqn (14).
		// Note the variance in each local patch is a 3x3 symmetric matrix:
		//           rr, rg, rb
		//   Sigma = rg, gg, gb
		//           rb, gb, bb    	
		Mat var_I_rr = new Mat();
		Mat var_I_rg = new Mat();
		Mat var_I_rb = new Mat();
		Mat var_I_gg = new Mat();
		Mat var_I_gb = new Mat();
		Mat var_I_bb = new Mat();
		Mat temp1 = new Mat();

		Core.subtract(boxfilter(Isubchannels.get(0).mul(Isubchannels.get(0)), (int) r_sub), 
				mean_I_r.mul(mean_I_r), temp1);
		Core.add(temp1, new Scalar(eps), var_I_rr);
		Core.subtract(boxfilter(Isubchannels.get(0).mul(Isubchannels.get(1)), (int) r_sub), 
				mean_I_r.mul(mean_I_g), var_I_rg);
		Core.subtract(boxfilter(Isubchannels.get(0).mul(Isubchannels.get(2)), (int) r_sub), 
				mean_I_r.mul(mean_I_b), var_I_rb);
		Core.subtract(boxfilter(Isubchannels.get(1).mul(Isubchannels.get(1)), (int) r_sub), 
				mean_I_g.mul(mean_I_g), temp1);
		Core.add(temp1, new Scalar(eps), var_I_gg);
		Core.subtract(boxfilter(Isubchannels.get(1).mul(Isubchannels.get(2)), (int) r_sub), 
				mean_I_g.mul(mean_I_b), var_I_gb);
		Core.subtract(boxfilter(Isubchannels.get(2).mul(Isubchannels.get(2)), (int) r_sub), 
				mean_I_b.mul(mean_I_b), temp1);
		Core.add(temp1, new Scalar(eps), var_I_bb);

		// Inverse of Sigma + eps * I
		Core.subtract(var_I_gg.mul(var_I_bb), var_I_gb.mul(var_I_gb), invrr);
		Core.subtract(var_I_gb.mul(var_I_rb), var_I_rg.mul(var_I_bb), invrg);
		Core.subtract(var_I_rg.mul(var_I_gb), var_I_gg.mul(var_I_rb), invrb);
		Core.subtract(var_I_rr.mul(var_I_bb), var_I_rb.mul(var_I_rb), invgg);
		Core.subtract(var_I_rb.mul(var_I_rg), var_I_rr.mul(var_I_gb), invgb);
		Core.subtract(var_I_rr.mul(var_I_gg), var_I_rg.mul(var_I_rg), invbb);

		Mat covDet = new Mat();
		Core.add(invrr.mul(var_I_rr), invrg.mul(var_I_rg), temp1);
		Core.add(temp1, invrb.mul(var_I_rb), covDet);

		Core.divide(invrr, covDet, invrr);
		Core.divide(invrg, covDet, invrg);
		Core.divide(invrb, covDet, invrb);
		Core.divide(invgg, covDet, invgg);
		Core.divide(invgb, covDet, invgb);
		Core.divide(invbb, covDet, invbb);

		Mat p2 = convertTo(p, Idepth);
		Mat result = new Mat();
		if (p.channels() == 1) {
			result = filterSingleChannel(p2, s, Isubchannels, Ichannels, mean_I_r, mean_I_g, mean_I_b, invrr, invrg, 
					invrb, invgg, invgb, invbb, r_sub);
		} else {
			ArrayList<Mat> pc = new ArrayList<>();
			Core.split(p2, pc);
			for (int i = 0; i < pc.size(); i++) {
				pc.set(i, filterSingleChannel(pc.get(i), s, Isubchannels, Ichannels, mean_I_r, mean_I_g, mean_I_b, invrr, 
						invrg, invrb, invgg, invgb, invbb, r_sub));
			}
			Core.merge(pc, result);
		}
		return convertTo(result, depth == -1 ? p.depth() : depth);
	}
	
	private static Mat boxfilter(Mat I, int r) {
		Mat result = new Mat();
		Imgproc.blur(I, result, new Size(r, r));
		return result;
	}

	private static Mat convertTo(Mat mat, int depth) {
		if (mat.depth() == depth) {
			return mat;
		}
		Mat result = new Mat();
		mat.convertTo(result, depth);
		return result;
	}

	private static Mat filterSingleChannel(Mat p, double s, ArrayList<Mat> Isubchannels, ArrayList<Mat> Ichannels, 
			Mat mean_I_r, Mat mean_I_g, Mat mean_I_b, Mat invrr, Mat invrg, Mat invrb, Mat invgg, Mat invgb, 
			Mat invbb, double r_sub) {
		Mat p_sub = new Mat();
		Imgproc.resize(p, p_sub, new Size(p.cols() / s, p.rows() / s), 0.0, 0.0, Imgproc.INTER_NEAREST);

		Mat mean_p = boxfilter(p_sub, (int) r_sub);

		Mat mean_Ip_r = boxfilter(Isubchannels.get(0).mul(p_sub), (int) r_sub);
		Mat mean_Ip_g = boxfilter(Isubchannels.get(1).mul(p_sub), (int) r_sub);
		Mat mean_Ip_b = boxfilter(Isubchannels.get(2).mul(p_sub), (int) r_sub);

		// convariance of (I, p) in each local patch
		Mat cov_Ip_r = new Mat();
		Mat cov_Ip_g = new Mat();
		Mat cov_Ip_b = new Mat();
		Core.subtract(mean_Ip_r, mean_I_r.mul(mean_p), cov_Ip_r);
		Core.subtract(mean_Ip_g, mean_I_g.mul(mean_p), cov_Ip_g);
		Core.subtract(mean_Ip_b, mean_I_b.mul(mean_p), cov_Ip_b);

		Mat temp1 = new Mat();
		Mat a_r = new Mat();
		Mat a_g = new Mat();
		Mat a_b = new Mat();
		Core.add(invrr.mul(cov_Ip_r), invrg.mul(cov_Ip_g), temp1);
		Core.add(temp1, invrb.mul(cov_Ip_b), a_r);
		Core.add(invrg.mul(cov_Ip_r), invgg.mul(cov_Ip_g), temp1);
		Core.add(temp1, invgb.mul(cov_Ip_b), a_g);
		Core.add(invrb.mul(cov_Ip_r), invgb.mul(cov_Ip_g), temp1);
		Core.add(temp1, invbb.mul(cov_Ip_b), a_b);

		Mat b = new Mat();
		Core.subtract(mean_p, a_r.mul(mean_I_r), b);
		Core.subtract(b, a_g.mul(mean_I_g), b);
		Core.subtract(b, a_b.mul(mean_I_b), b);

		Mat mean_a_r = boxfilter(a_r, (int) r_sub);
		Mat mean_a_g = boxfilter(a_g, (int) r_sub);
		Mat mean_a_b = boxfilter(a_b, (int) r_sub);
		Mat mean_b = boxfilter(b, (int) r_sub);

		Imgproc.resize(mean_a_r, mean_a_r, 
				new Size(Ichannels.get(0).cols(), Ichannels.get(0).rows()), 0.0, 0.0, Imgproc.INTER_LINEAR);
		Imgproc.resize(mean_a_g, mean_a_g, 
				new Size(Ichannels.get(0).cols(), Ichannels.get(0).rows()), 0.0, 0.0, Imgproc.INTER_LINEAR);
		Imgproc.resize(mean_a_b, mean_a_b, 
				new Size(Ichannels.get(0).cols(), Ichannels.get(0).rows()), 0.0, 0.0, Imgproc.INTER_LINEAR);
		Imgproc.resize(mean_b, mean_b, 
				new Size(Ichannels.get(0).cols(), Ichannels.get(0).rows()), 0.0, 0.0, Imgproc.INTER_LINEAR);

		Mat result = new Mat();
		Core.add(mean_a_r.mul(Ichannels.get(0)), mean_a_g.mul(Ichannels.get(1)), temp1);
		Core.add(temp1, mean_a_b.mul(Ichannels.get(2)), temp1);
		Core.add(temp1, mean_b, result);
		return result;
	}

}
