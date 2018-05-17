package me.lsdo.processing.util;

public class MathUtil {

    public static double LN2 = Math.log(2.);

    // Fix java's stupid AF mod operator to always return a positive result
    public static int mod(int a, int b) {
        return ((a % b) + b) % b;
    }

    public static double fmod(double a, double b) {
        double mod = a % b;
        if (mod < 0) {
            mod += b;
        }
        return mod;
    }

    public static double log2(double x) {
        return Math.log(x) / LN2;
    }

    // easings: some curve connecting (0,0) to (1,1)
    public static double linearEasing(double x) {
	return x;
    }
    public static double sineEasing(double x) {
	return .5*(1 - Math.cos(x * Math.PI));
    }
    public static double polyEasing(double x) {
	return polyEasing(x, 3.);
    }
    public static double polyEasing(double x, double power) {
	double k = Math.pow(1 - Math.abs(2*x - 1), power);
	return (x < .5 ? .5*k : 1 - .5*k);
    }

}
