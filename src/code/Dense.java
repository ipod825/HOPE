package code;


import java.util.Random;


public class Dense extends Code{
	public  boolean[][] generate(int numVars, int numConstraint){
		if(numConstraint<=0 || numVars <=0){
			return null;
		}
		boolean[][] matrix = new boolean[numConstraint][numVars];
		Random rand = new Random();
		for(int i=0;i<numConstraint;i++){
			for(int j=0;j<numVars;j++){
				matrix[i][j]=rand.nextBoolean();
			}
		}
		return matrix;
	}
}
