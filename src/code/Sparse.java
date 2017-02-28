package code;


import java.math.BigInteger;
import java.util.HashMap;
import java.util.Random;

public class Sparse{
	
	static double[] pF;
	static HashMap<Integer, HashMap<Integer, Double>> F;
	static HashMap<Integer, HashMap<Integer, BigInteger>> NK;
	static{
		F = new HashMap<Integer, HashMap<Integer, Double>>();
		NK = new HashMap<Integer, HashMap<Integer, BigInteger>>();
		pF = new double[]{0.1199999999999997,0.3599999999999999,0.4199999999999999,0.4399999999999999,0.45,0.4299999999999999,0.4199999999999999,0.4099999999999999,0.4099999999999999,0.4099999999999999,0.3999999999999999,0.3799999999999999,0.3699999999999999,0.3599999999999999,0.3599999999999999,0.3499999999999999,0.3299999999999998,0.3299999999999998,0.3199999999999998,0.3199999999999998,0.2999999999999998,0.2899999999999998,0.2899999999999998,0.2899999999999998,0.2799999999999998,0.2699999999999998,0.2599999999999998,0.2599999999999998,0.2499999999999998,0.2399999999999998,0.2399999999999998,0.2399999999999998,0.2299999999999998,0.2199999999999998,0.2199999999999998,0.2199999999999998,0.2099999999999997,0.2099999999999997,0.1999999999999997,0.1999999999999997,0.1899999999999997,0.1899999999999997,0.1899999999999997,0.1799999999999997,0.1799999999999997,0.1699999999999997,0.1699999999999997,0.1699999999999997,0.1599999999999997,0.1599999999999997,0.1599999999999997,0.1599999999999997,0.1499999999999997,0.1499999999999997,0.1499999999999997,0.1499999999999997,0.1399999999999997,0.1399999999999997,0.1399999999999997,0.1299999999999997,0.1299999999999997,0.1299999999999997,0.1299999999999997,0.1199999999999997,0.1199999999999997,0.1199999999999997,0.1199999999999997,0.1199999999999997,0.1099999999999997,0.1099999999999997,0.1099999999999997,0.1099999999999997,0.1099999999999997,0.09999999999999969,0.09999999999999969,0.09999999999999969,0.09999999999999969,0.09999999999999969,0.09999999999999969,0.08999999999999969,0.08999999999999969,0.08999999999999969,0.08999999999999969,0.08999999999999969,0.0799999999999997,0.0799999999999997,0.0799999999999997,0.0799999999999997,0.0799999999999997,0.0799999999999997,0.0699999999999997,0.0699999999999997,0.0699999999999997,0.0699999999999997,0.0699999999999997,0.0699999999999997,0.0599999999999997,0.0599999999999997,0.0599999999999997,0.0499999999999997};
	}
	static public BigInteger nchoosek(Integer N, Integer K){
		HashMap<Integer, BigInteger> NKForN;
		if(NK.containsKey(N))
			NKForN =  NK.get(N);
		else
			NKForN =  new HashMap<Integer, BigInteger>();
		
		if(NKForN.containsKey(K))
			return NKForN.get(K);
		else{
			BigInteger res = BigInteger.ONE;
			for (int k = 0; k < K; k++)
				res = res.multiply(BigInteger.valueOf(N-k)).divide(BigInteger.valueOf(k+1));
			NKForN.put(K, res);
			if(!NK.containsKey(N))
				NK.put(N, NKForN);
			return res;
		}
	}
	static public int calcWStar(int n, BigInteger q){
		int j = 1;
		BigInteger sum = nchoosek(n,j);
		while(sum.compareTo(q)<0 && j<n){
			j += 1;
			sum = sum.add(nchoosek(n, j));
		}
		return j-1;
	}
	static public double epsi(int n, int m, BigInteger q, double f, int ws){		
		double sum1 = 0;
		double sum2 = 0;
		for(int w=1;w<=ws;++w){
			double nck = nchoosek(n, w).doubleValue(); 
			sum1 += nck * Math.pow(.5+.5*Math.pow(1-2*f,w),m);
			sum2 += nck;
		}
		BigInteger qm1 = q.subtract(BigInteger.ONE);
		sum2 = (qm1.doubleValue() - sum2) * Math.pow(.5+.5*Math.pow(1-2*f,(ws+1)),m);
		return (sum1+sum2)/qm1.doubleValue();
	}
	  
	static public Double getF(Integer n, Integer i){
		HashMap<Integer, Double> fForN;
		if(F.containsKey(n))
			fForN =  F.get(n);
		else
			fForN =  new HashMap<Integer, Double>();
		
		if(fForN.containsKey(i))
			return fForN.get(i);
		else{
			BigInteger q = BigInteger.valueOf(2).pow(i+2);
			int ws = calcWStar(n,q);                                                                      
			double stepsize = 0.01;
			double f=0.5;
			BigInteger qm1 = q.subtract(BigInteger.ONE);
			double threshold = 6.2/(qm1.doubleValue());
			while(f>0 && epsi(n, i, q, f, ws)<threshold)
				f -= stepsize;
			
			f += stepsize;
			
			fForN.put(i, f);
			if(!F.containsKey(n))
				F.put(n, fForN);
			return f;
		}
	}
		
	public  boolean[][] generate(int numVars, int numConstraints){
		assert numConstraints<=numVars;
		if(numConstraints<=0 || numVars <=0){
			return null;
		}
		// double f = Sparse.getF(numVars, numConstraints);
		double f = pF[numConstraints-1];
//		double f = Math.min(0.5, 1-Math.pow((R/C),0.28)+0.05);
		boolean[][] matrix = new boolean[numConstraints][numVars];
		Random rand = new Random();
		for(int i=0;i<numConstraints;i++){
			for(int j=0;j<numVars;j++){
				matrix[i][j]=rand.nextDouble()<f;
			}
		}
		return matrix;
	}
}
