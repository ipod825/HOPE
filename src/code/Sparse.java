package code;


import java.math.BigInteger;
import java.util.HashMap;
import java.util.Random;

public class Sparse{
	
	static HashMap<Integer, HashMap<Integer, Double>> F;
	static HashMap<Integer, HashMap<Integer, BigInteger>> NK;
	static{
		F = new HashMap<Integer, HashMap<Integer, Double>>();
		NK = new HashMap<Integer, HashMap<Integer, BigInteger>>();
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
		if(numConstraints<=0 || numVars <=0){
			return null;
		}
		double f = Sparse.getF(numVars, numConstraints);
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
