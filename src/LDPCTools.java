import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;
import java.util.StringTokenizer;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.Executor;

public class LDPCTools {
	public static int startRPEG = 14;
	public static int[] regDeg = {38,27,25,24,23,21,20,19,19,18,17,16,16,15,15,14,14};
	
	private static void shuffleArray(int[] array){
		Random rand = new Random();
		if(array==null || array.length==0)
			return;
		int n=array.length;
		for(int i=n-1;i>0;i--){
			int j = (int) (rand.nextDouble()*(i+1));
			int temp=array[i];
			array[i]=array[j];
			array[j]=temp;
		}
	}
	
	public static boolean[][] getRegularSparseMatrix(int n, int d){
		double f = 0.37;
		return BinaryMatrixHelper.getRandomMatrix(d,n,f);
	}
	
	public static boolean[][] getSupposedlyEasilyDecodableCode(int n, int d){
		boolean[][] matrix = new boolean[d][n];
		Random rand = new Random();
		for(int i=0;i<d;i++){
			matrix[i][i]=true;;
			for(int j=i+1;j<d;j++){
				matrix[i][j]=rand.nextBoolean();
			}
			for(int j=d;j<n && j<d+10;j++){
				if(i==d){
					matrix[i][j]=true;
				}else{
					matrix[i][j]=rand.nextBoolean();					
				}
			}
		}
		return matrix;
	}
	
	public static boolean[][] getRegularSparseMatrix(int n, int d, double f){
		return BinaryMatrixHelper.getRandomMatrix(d,n,f);
	}
	
	public static boolean[][] getPEGMatrix(int n, int d){
		return getPEGMatrix(n,d,true);
	}
	
	public static boolean[][] getPEGMatrix(int n, int d, boolean fallback){
		String path=null;
		boolean[][] fallbackMatrix;
		if(fallback){
			fallbackMatrix = getRegularSparseMatrix(n,d);
		}else{
			fallbackMatrix = null;
		}
		
		try {
			path = generateDegreeFile(n,d);
		} catch (ExecuteException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return fallbackMatrix;
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return fallbackMatrix;
		}
		try {
			return parseDegrees(path);
//			BinaryMatrixHelper.printMatrix(constraints);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return fallbackMatrix;
	}
	
	public static boolean[][] getRPEGMatrix(int n, int d, int deg, boolean fallback){
		String path=null;
		boolean[][] fallbackMatrix;
		if(fallback){
			fallbackMatrix = getRegularSparseMatrix(n,d);
		}else{
			fallbackMatrix = null;
		}
		
		try {
			generatePEGRegDegree(deg);
			path = generateRegularDegreeFile(n,d,deg);
		} catch (ExecuteException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return fallbackMatrix;
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return fallbackMatrix;
		}
		try {
			boolean[][] matrix =  parseDegrees(path);
			BinaryMatrixHelper.printMatrix(matrix);
			return matrix;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return fallbackMatrix;
	}
	
	public static boolean[][] getRPEGMatrix(int n, int d, boolean fallback){
		String path=null;
		boolean[][] fallbackMatrix;
		if(fallback){
			fallbackMatrix = getRegularSparseMatrix(n,d);
		}else{
			fallbackMatrix = null;
		}
		
		try {
			int deg=10;
			generatePEGRegDegree(deg);
			path = generateRegularDegreeFile(n,d,deg);
		} catch (ExecuteException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return fallbackMatrix;
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return fallbackMatrix;
		}
		try {
			boolean[][] matrix =  parseDegrees(path);
			BinaryMatrixHelper.printMatrix(matrix);
			return matrix;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return fallbackMatrix;
	}
	
	public static boolean[][] getRPEGMatrix(int n, int d){
		String path=null;
		if(d>=startRPEG){
			return null;
		}else{
			try {
				path = generateRegularDegreeFile(n,d,d*regDeg[d]/n);
			} catch (ExecuteException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				return null;
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				return null;
			}
			try {
				return parseDegrees(path);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		
	}
	
	private static int[] generateSequentialArray(int n){
		int[] array = new int[n];
		for(int i=0;i<array.length;i++){
			array[i]=i;
		}
		return array;
	}
	
	public static String generateDegreeFile(int n, int checks) throws ExecuteException, IOException{
		String outName = "n"+n+"m"+checks+"d"+Config.degree+".dat";
		File outf = new File(Config.output+outName);
		if(outf.exists()){
			return outf.getAbsolutePath();
		}
			
		CommandLine cl = new CommandLine("./MainPEG");
		cl.addArgument("-numM");
		cl.addArgument(""+checks);
		cl.addArgument("-numN");
		cl.addArgument(""+n);
		cl.addArgument("-codeName");
		cl.addArgument(Config.output+outName);
		cl.addArgument("-degFileName");
		cl.addArgument("DenEvl_"+Config.degree+".deg");
		  
		Executor exec = new DefaultExecutor();
		exec.setWorkingDirectory(new File(Config.pathToMPEG));
		exec.execute(cl);
		return outf.getAbsolutePath();
	}
	
	public static void generatePEGRegDegree(int deg) throws ExecuteException, IOException{
		String degFile = "Reg_"+deg+".deg";
		String path = Config.output+degFile;
		File f = new File(path);
		if(f.exists())
			return;
		PrintWriter out = new PrintWriter(path);
		out.println("1");
		out.println("5");
		out.println("1.0");
		out.close();
	}
	
	public static String generateRegularDegreeFile(int n, int checks, int deg) throws ExecuteException, IOException{
		String degFile = "Reg_"+deg+".deg";
		String outName = "n"+n+"m"+checks+"regd"+deg+".dat";
		File outf = new File(Config.output+outName);
		if(outf.exists()){
			return outf.getAbsolutePath();
		}
			
		CommandLine cl = new CommandLine("./MainPEG");
		cl.addArgument("-numM");
		cl.addArgument(""+checks);
		cl.addArgument("-numN");
		cl.addArgument(""+n);
		cl.addArgument("-codeName");
		cl.addArgument(Config.output+outName);
		cl.addArgument("-degFileName");
		cl.addArgument(degFile);
		  
		Executor exec = new DefaultExecutor();
		exec.setWorkingDirectory(new File(Config.pathToMPEG));
		exec.execute(cl);
		return outf.getAbsolutePath();
	}
	
	private static boolean[][] parseDegrees(String path) throws IOException{
		FileReader fr = new FileReader(new File(path));
		BufferedReader br = new BufferedReader(fr);
		String line;
		boolean[][] constraints=null;
		int n=0,d=0;
		int counter = 1;
		int[] shuffledIdxes=null;
		while ((line = br.readLine()) != null) {
			if(counter==1){
				n=Integer.parseInt(line);
				shuffledIdxes = generateSequentialArray(n);
				shuffleArray(shuffledIdxes);
			}else if(counter==2){
				d=Integer.parseInt(line);
				constraints = new boolean[d][n];
			}else if(counter>=4){
				StringTokenizer st = new StringTokenizer(line," ");
				int idx=0;
				while(st.hasMoreTokens()){
					idx=Integer.parseInt(st.nextToken());
					if(idx==0){
						break;
					}
					int newIdx = shuffledIdxes[idx-1];
					constraints[counter-4][newIdx]=true;
				}
			}
			counter++;
		}
		br.close();
		return constraints;
	}
}
