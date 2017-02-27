package hope;
import java.util.Random;


public class BinaryMatrixHelper {
	
	public static boolean[] multiply(boolean[][] matrix, boolean[] vector){
		if(matrix == null || vector == null){
			return null;
		}
		if(matrix[0].length != vector.length){
			return null;
		}
		int n = matrix[0].length;
		int m = matrix.length;
		boolean[] result = new boolean[m];
		for(int i=0;i<m;i++){
			for(int j=0;j<n;j++){
				result[i] = result[i] ^ (matrix[i][j] & vector[j]);
			}
		}
		return result;
	}
	
	public static boolean[][] copyMatrix(boolean[][] old){
		return copyMatrix(old, old.length);
	}
	
	public static boolean[][] copyMatrix(boolean[][] old, int upToRow){
		int m = Math.min(old.length, upToRow);
		if(m==0){
			return null;
		}
		int n = old[0].length;
		boolean[][] duplicate = new boolean[m][n];
		for(int i=0; i<m; i++){
			  for(int j=0; j<n; j++){
			    duplicate[i][j]=old[i][j];
			  }
		}
		
		return duplicate;
	}

	public static boolean[][] getFullRankRegularMatrix(int size){
		if(size==0){
			return null;
		}
		boolean[][] matrix;
		do{
//			System.out.println/(rank);
			matrix = getRandomRegularMatrix(size,size);
//			printMatrix(matrix);
		}while(gaussJordanElimination(copyMatrix(matrix),size)<size);
		return matrix;
	}
	

	
	public static boolean[][] getFullRankMatrix(int size){
		if(size==0){
			return null;
		}
		boolean[][] matrix;
		do{
			matrix = getRandomMatrix(size,size);
		}while(gaussJordanElimination(copyMatrix(matrix),size)<size);
		return matrix;
	}
	
	/**
	 * given a vector v, "project" v into the span of the first reducedDim columns of matrix  
	 * @param matrix
	 * @param reducedDim
	 * @param vec
	 */
	public static boolean[] decode(boolean[][] matrix, boolean[][] superMatrix, int reducedDim, boolean[] x, boolean[] offset){
		if(matrix==null || superMatrix==null || matrix.length==0 || x==null || offset==null 
				|| x.length!= offset.length || matrix[0].length!= x.length
				|| superMatrix.length!=matrix.length+1){
			return null;
		}
		if(isXInRange(x, matrix, offset)){
			return null;
		}
		boolean[] xPlusOffset = addVector(x,offset);
		boolean[] temp=null;
		boolean[] solution=null;
		gaussJordanElimination(matrix, reducedDim);
		gaussJordanElimination(superMatrix, reducedDim+1);
//		printMatrix(superMatrix);
		while(solution ==null){
			boolean[] basis = superMatrix[(int) ((reducedDim+1)*Math.random())];
			temp = addVector(xPlusOffset, basis);
			solution = findSolution(temp, matrix);
		}
		BinaryMatrixHelper.printVector(addVector(temp,offset));
		return solution;
	}
	
	public static boolean[] addVector(boolean[] x, boolean[] offset){
		if(x==null || offset==null 
				|| x.length!= offset.length){
			return null;
		}
		boolean[] xPlusOffset = new boolean[x.length];
		for(int i=0;i<x.length;i++){
			xPlusOffset[i]=x[i]^offset[i];
		}
		return xPlusOffset;
	}
	
	public static void shuffleColumns(boolean[][] matrix){
		if(matrix==null || matrix.length==0)
			return;
		int n=matrix[0].length;
		for(int i=n-1;i>0;i--){
			int j = (int) (Math.random()*(i+1));
			swapColumns(i,j,matrix);
		}
	}
	
	public static boolean[][] parityToGenerator(boolean[][] h_){
		boolean[][] h = BinaryMatrixHelper.copyMatrix(h_);
		if(h==null){
			return null;
		}
		int d=h.length;
		int n=h[0].length;
		int rank=BinaryMatrixHelper.gaussJordanElimination(h);
		if(rank>=n){
			return null;
		}
		//dimension of generator matrix = n - rank(h)
		boolean[][] g=new boolean[n-rank][n];
		
		//find all pivots
		//pivotsOf[i]=j => the pivot of the ith row is variable j
		int[] pivotsOf= new int[d];
		boolean[] pivots = new boolean[n];
		for(int i=0;i<d;i++){
			boolean pivotFound=false;
			for(int j=0;j<n;j++){
				if(h[i][j] && !pivotFound){
					//variable j is a pivot of the ith row
					pivots[j]=true;
					pivotsOf[i]=j;
					pivotFound=true;
				}
			}
		}
		
		int basisCounter=0;
		for(int j=0;j<n;j++){
			if(pivots[j]){
				continue;
			}
			g[basisCounter][j]=true;
			for(int i=0;i<d;i++){
				if(h[i][j])
					g[basisCounter][pivotsOf[i]]=true;
			}
			basisCounter++;
		}
		
		return g;
	}
	
	private static void swapColumns(int i, int j, boolean[][] matrix){
		int m = matrix.length;
		if(m==0){
			// do nothing
			return;
		}
		int n = matrix[0].length;
		if(i>=n || j>=n || i==j){
			// do nothing
			return;
		}
		
		boolean temp;
		for(int k=0;k<m;k++){
			temp = matrix[k][i];
			matrix[k][i] = matrix[k][j];
			matrix[k][j] = temp;
		}
	}

	private static void swapRows(int i, int j, boolean[][] matrix){
		int m = matrix.length;
		if(m==0 || i>=m || j>=m || i==j){
			// do nothing
			return;
		}
		int n = matrix[0].length;
		boolean temp;
		for(int k=0;k<n;k++){
			temp = matrix[i][k];
			matrix[i][k] = matrix[j][k];
			matrix[j][k] = temp;
		}
	}

	public static boolean[][] getRandomRegularMatrix(int m, int n){
		return getRandomRegularMatrix(m,n,n/2);
	}
	
	public static boolean[][] getRandomRegularMatrix(int m, int n, int k){
		if(m<=0 || n <=0 || k>n){
			return null;
		}
		boolean[][] matrix = new boolean[m][n];
		int[] indexes = new int[n]; 
		for(int i=0;i<n;i++){
			indexes[i]=i;
		}
		for(int i=0;i<m;i++){
			shuffle(indexes);
			for(int j=0;j<k;j++){
				matrix[i][indexes[j]]=true;
			}
		}
		return matrix;
	}
	
	public static void shuffle(int[] indexes){
		if(indexes==null)
			return;
		int n=indexes.length;
		for(int i=n-1;i>0;i--){
			int j = (int) (Math.random()*(i+1));
			int temp = indexes[j];
			indexes[j] = indexes[i];
			indexes[i] = temp;
		}
	}
	
	public static boolean[][] getRandomMatrix(int m, int n){
		return getRandomMatrix(m,n,0.5);
	}
	
	public static boolean[][] getRandomMatrix(int m, int n, double f){
		if(m<=0 || n <=0){
			return null;
		}
		boolean[][] matrix = new boolean[m][n];
		Random rand = new Random();
		for(int i=0;i<m;i++){
			for(int j=0;j<n;j++){
//				matrix[i][j]=rand.nextBoolean();
				matrix[i][j]=rand.nextDouble()>1-f;
			}
		}
		return matrix;
	}
	
	public static boolean[] or(boolean[] x, boolean[] y){
		if(x==null || y==null || x.length!=y.length)
			return null;
		
		boolean[] result = new boolean[x.length];
		for(int i=0;i<x.length;i++){
			result[i]=x[i]|y[i];
		}
		return result;
	}
	
	public static boolean[] and(boolean[] x, boolean[] y){
		if(x==null || y==null || x.length!=y.length)
			return null;
		
		boolean[] result = new boolean[x.length];
		for(int i=0;i<x.length;i++){
			result[i]=x[i]&y[i];
		}
		return result;
	}
	
	public static boolean[][] getRandomMatrix(int m, int n, double f, int seed){
		Random rand = new Random(seed);
		if(m<=0 || n <=0){
			return null;
		}
		boolean[][] matrix = new boolean[m][n];
		for(int i=0;i<m;i++){
			for(int j=0;j<n;j++){
				matrix[i][j]=rand.nextDouble()>1-f;
			}
		}
		return matrix;
	}
	
	public static boolean[] getRandomVector(int n){
		if(n <=0){
			return null;
		}
		boolean[] vector = new boolean[n];
		Random rand = new Random();
		for(int i=0;i<n;i++){
			vector[i]=rand.nextBoolean();
		}
		return vector;
	}
	
	/**
	 * same as gaussJordanElimination(boolean[][] matrix, int upToRow, int upToCol) but on full matrix
	 * @param matrix boolean matrix
	 * @return rank
	 */
	public static int gaussJordanElimination(boolean[][] matrix){
		if(matrix==null){
			return -1;
		}
		return gaussJordanElimination(matrix,matrix.length,matrix[0].length);
	}
	
	/**
	 * same as gaussJordanElimination(boolean[][] matrix, int upToRow, int upToCol) but with all the columns
	 * @param matrix boolean matrix
	 * @return rank
	 */
	public static int gaussJordanElimination(boolean[][] matrix, int upToRow){
		if(matrix==null){
			return -1;
		}
		return gaussJordanElimination(matrix,upToRow,matrix[0].length);
	}
	
	/**
	 * Perform Gauss-Jordan elimination on matrix. Note that the matrix passed in will be changed to rref in the process so you may want to save a copy. 
	 * @param matrix boolean matrix
	 * @param upToRow ignore the rows after upToRow (1 base)
	 * @param upToCol ignore the columns after upToCol (1 base)
	 * @return the rank of the matrix
	 */
	public static int gaussJordanElimination(boolean[][] matrix, int upToRow, int upToCol){
		if(matrix==null){
			return -1;
		}
		int m,n;
		m = Math.min(upToRow, matrix.length);
		if(m<1){
			return 0;
		}
		if(m==1){
			return 1;
		}
		n = Math.min(upToCol, matrix[0].length);
		int pivot=0;
		int startRow=0;
		while(pivot<n && startRow<m){
			//find leading one
			int leadingRow=pickLeadingOne(startRow, pivot, m, n, matrix);
			if(leadingRow<0){
				pivot++;
				continue;
			}else{
				//swap
				swapRows(startRow, leadingRow, matrix);
				//elimination
				eliminate(startRow, pivot, m, matrix);
				
				pivot++;
				startRow++;
			}
		}
		return startRow;
	}
	
	private static int pickLeadingOne(int startRow, int pivot, int m, int n, boolean[][] matrix){
		int leadingRow=-1;
		for(int i=startRow;i<m;i++){
			if(matrix[i][pivot]){
				leadingRow=i;
				break;
			}
		}
		return leadingRow;
	}
	
//	private static int pickSparseLeadingOne(int startRow, int pivot, int m, int n, boolean[][] matrix){
//		int min=m*n;
//		int minIdx=-1;
//		for(int i=startRow;i<m;i++){
//			if(matrix[i][pivot]){
//				if(minIdx<0){
//					minIdx=i;
//					continue;
//				}
//				
//				int sum=0;
//				for(int ri=0;ri<m;ri++){
//					for(int rj=pivot;rj<n;rj++){
//						if(matrix[ri][pivot]){
//							if(matrix[i][rj]^matrix[ri][rj]){
//								sum ++;	
//							}
//						}else if(matrix[ri][rj]){
//							sum ++;
//						}
//					}
//				}
//				System.out.println("sum"+sum);
//				System.out.println("min"+min);				
//				if(sum<min){
//					min=sum;
//					minIdx=i;
//				}
//			}
//		}
//		System.out.println("sparse:"+minIdx);
//		return minIdx;
//	}
//	
	private static void eliminate(int pr, int pc, int m, boolean[][] matrix){
		int n;
		n=matrix[0].length;
		for(int i=0;i<m;i++){
			//skip those rows that we don't care 
			if(i==pr || !matrix[i][pc]){
				continue;
			}
			matrix[i][pc]=false;
			for(int j=pc+1;j<n;j++){
				matrix[i][j]= matrix[pr][j]^matrix[i][j];
			}
		}
	}
	
	
	/**
	 * given a vector x, find a y such that matrix*y = x 
	 * @param x
	 * @param matrix
	 * @return return y if y exists. o.w. return null
	 */
	public static boolean[] findFullSolution(boolean[] x, boolean[][] matrix){
		System.out.println(matrix.length);
		System.out.println(matrix[0].length);
		System.out.println(x.length);
		if(x==null || matrix==null){
			return null;
		}
		if(x.length!=matrix[0].length){
			return null;
		}
	
		//transpose
		boolean[][] mt = transpose(matrix);
		//augmented (add x as the lsat column)
		boolean[][] augMt = augmentedMatrix(mt, x);
		if(augMt==null){
			return null;
		}
		int numCol=mt[0].length;
		//GJ-eliminatation
		gaussJordanElimination(augMt, augMt.length, numCol);
		boolean[] solution=new boolean[numCol];
		for(int i=0;i<augMt.length;i++){
			boolean pivotFound =false;
			for(int j=0;j<numCol;j++){
				if(!pivotFound && augMt[i][j]){
					pivotFound=true;
					solution[j]=augMt[i][numCol];
				}
			}
		}
		return solution;
	}
	
	/**
	 * given a vector x, find a y such that matrix*y = x 
	 * @param x
	 * @param matrix
	 * @return return y if y exists. o.w. return null
	 */
	public static boolean[] findSolution(boolean[] x, boolean[][] matrix){
		if(x==null || matrix==null){
			return null;
		}
		if(x.length!=matrix[0].length){
			return null;
		}
	
		//transpose
		boolean[][] mt = transpose(matrix);
		//augmented (add x as the lsat column)
		boolean[][] augMt = augmentedMatrix(mt, x);
		if(augMt==null){
			return null;
		}
		int numCol=mt[0].length;
		//GJ-eliminatation
		int rank=gaussJordanElimination(augMt, augMt.length, numCol);
		boolean[] solution=new boolean[rank];
		for(int i=0;i<augMt.length;i++){
			if(augMt[i][numCol]){
				if(i<rank){
					solution[i]=true;
				}else{
					return null;
				}
			}
		}
		return solution;
	}
	
	/**
	 * given a vector x, test if there exists a y such that matrix*y + offset = x 
	 * @param x
	 * @param matrix
	 * @param offset
	 * @return true if such a y vector exists
	 */
	public static boolean isXInRange(boolean[] x, boolean[][] matrix, boolean[] offset){
		if(x==null || offset==null){
			return false;
		}
		if( x.length!=offset.length){
			return false;
		}
		//compute x'=x+offset and then test if there exists a solution y that matrix*y=x'
		boolean[] xPlusOffset = new boolean[x.length];
		for(int i=0;i<x.length;i++){
			xPlusOffset[i]=x[i]^offset[i];
		}
		return findSolution(xPlusOffset,matrix)!=null;
	}
	
//	public static boolean isXInRange(boolean[] x, boolean[][] matrix, boolean[] offset){
//		if(x==null || matrix==null || offset==null){
//			return false;
//		}
//		if(x.length!=matrix[0].length || x.length!=offset.length){
//			return false;
//		}
//		//compute x'=x+offset and then test if there exists a solution y that matrix*y=x'
//		boolean[] xPlusOffset = new boolean[x.length];
//		for(int i=0;i<x.length;i++){
//			xPlusOffset[i]=x[i]^offset[i];
//		}
//		//transpose
//		boolean[][] mt = transpose(matrix);
//		//augmented
//		boolean[][] augMt = augmentedMatrix(mt, xPlusOffset);
//		if(augMt==null){
//			return false;
//		}
//		int numCol=mt[0].length;
//		//GJ-eliminatation
//		int rank=gaussJordanElimination(augMt, augMt.length, numCol);
//		for(int i=rank;i<augMt.length;i++){
//			if(augMt[i][numCol]){
//				boolean consistent=false;
//				for(int j=0;j<numCol;j++){
//					if(augMt[i][j]){
//						consistent=true;
//						break;
//					}
//				}
//				if(!consistent){
//					return false;
//				}
//			}
//		}
//		return true;
//	}
	
	/**
	 * return the transpose of a boolean matrix
	 * @param matrix
	 * @return
	 */
	public static boolean[][] transpose(boolean[][] matrix){
		if(matrix==null){
			return null;
		}
		int m=matrix.length;
		int n=matrix[0].length;
		boolean[][] t = new boolean[n][m];
		for(int i=0;i<m;i++){
			for(int j=0;j<n;j++){
				t[j][i]=matrix[i][j];
			}
		}
		return t;
	}
	
	/**
	 * augmented matrix [m | offset], i.e., append offset to the matrix
	 * @param matrix
	 * @return
	 */
	public static boolean[][] augmentedMatrix(boolean[][] matrix, boolean[] offset){
		if(matrix==null){
			return null;
		}
		int m=matrix.length;
		int n=matrix[0].length;
		if(m!=offset.length){
			return null;
		}
		//copy the original matrix
		boolean[][] augMatrix = new boolean[m][n+1];
		for(int i=0;i<m;i++){
			for(int j=0;j<n;j++){
				augMatrix[i][j]=matrix[i][j];
			}
		}
		//append offset
		for(int i=0;i<m;i++){
			augMatrix[i][n]=offset[i];
		}
		return augMatrix;
	}
	
	public static void printVector(boolean[] vec){
		if(vec==null){
			System.out.println("null vector");
			return;
		}
		
		for(int i=0;i<vec.length;i++){
			System.out.print(vec[i]?1:0);
//			System.out.println("x"+i+":"+vec[i]);
		}
		System.out.print("\n");
	}
	
	public static void printMatrix(boolean[][] matrix){
		if(matrix==null){
			return;
		}
		int m = matrix.length;
		if(m==0){
			return;
		}
		int n = matrix[0].length;
		for(int i=0;i<m;i++){
			for(int j=0;j<n;j++){
				System.out.print(" "+(matrix[i][j]?1:0));
			}
			System.out.print('\n');
		}
	}
	
	public static int getDensity(boolean[] vec){
		int n = vec.length;
		if(n==0){
			return 0;
		}
		int count=0;
		for(int i=0;i<n;i++){
			if(vec[i])
				count++;
		}
		return count;
	}
	
	public static int getDensity(boolean[][] matrix){
		int m = matrix.length;
		if(m==0){
			return 0;
		}
		int n = matrix[0].length;
		int count=0;
		for(int i=0;i<m;i++){
			for(int j=0;j<n;j++){
				if(matrix[i][j]){
					count++;
				}
			}
		}
		return count;
	}
	
	/**
	 * transform a long value into a boolean array
	 * @param value
	 * @param length
	 * @return
	 */
	public static boolean[] toBits(long value, int length){

	    boolean[] bits = new boolean[length];
	    for (int i = length-1; i >= 0; i--) {
	        bits[i] = (value & (1 << i)) != 0;
	    }
	    return bits;
	}
}
