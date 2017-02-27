package code;


import hope.Config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.Random;
import java.util.StringTokenizer;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.Executor;


public class PEG{
	public static int startRPEG = 14;
	public static int[] regDeg = {38,27,25,24,23,21,20,19,19,18,17,16,16,15,15,14,14};
	
	public  boolean[][] generate(int numVars, int numConstraints){
		assert numConstraints<=numVars;
		String path=null;
		
		try {
			path = generateDegreeFile(numVars, numConstraints);
		} catch (ExecuteException e1) {
			System.out.println("Fail to generate LDPC!! Dense matrix will be used");
			return null;
		} catch (IOException e1) {e1.printStackTrace();}
		
		try {
			return parseDegrees(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;		
	}
	
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
	
	
	private static int[] generateSequentialArray(int n){
		int[] array = new int[n];
		for(int i=0;i<array.length;i++){
			array[i]=i;
		}
		return array;
	}
	
	public static String generateDegreeFile(int n, int checks) throws ExecuteException, IOException{
		String outName = "n"+n+"m"+checks+"d"+Config.degree+"_"+ new Date().getTime()+"_"+Thread.currentThread().getId()+".dat";
		File outf = new File(Config.tmpDir+outName);
		if(outf.exists()){
			return outf.getAbsolutePath();
		}
			
		CommandLine cl = new CommandLine("./MainPEG");
		cl.addArgument("-numM");
		cl.addArgument(""+checks);
		cl.addArgument("-numN");
		cl.addArgument(""+n);
		cl.addArgument("-quiet");
		cl.addArgument(""+1);
		cl.addArgument("-codeName");
		cl.addArgument(Config.tmpDir+outName);
		cl.addArgument("-degFileName");
		cl.addArgument("DenEvl_"+Config.degree+".deg");
		  
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
