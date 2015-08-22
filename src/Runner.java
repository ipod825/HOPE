import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.LogOutputStream;
import org.apache.commons.exec.PumpStreamHandler;

/**
 * For running batch tasks, e.g., run WISH algorithm on all the datasets under a given folder.
 * @author jp
 */
public class Runner {
	 
//	public static void main(String [] args) {
////		int[] time={10,30,60,180};
////		int[] time={10};
//		Runner runner = new Runner();
////		try {
////			runner.uaiToFg("/home/jp/research/ICML13-Dataset/high/");
////		} catch (FileNotFoundException e1) {
////			// TODO Auto-generated catch block
////			e1.printStackTrace();
////		}
//		try {
//			runner.runLibDai("/home/jp/research/ICML13-Dataset/att_01_fg/","/home/jp/research/grid_att_01_bp_max", ResultOutputStream.BP);
////			runner.runLibDai("/home/jp/research/ICML13-Dataset/high/","/home/jp/research/test0227", ResultOutputStream.JTREE);
////			runner.runLibDai("/home/jp/research/grid_high/","/home/jp/research/grids_high.exact", ResultOutputStream.JTREE);
////			runner.uaiToFg("/home/jp/research/grid_high/");
////			RunParams params = new RunParams(true, 360, 1, 
////					ConstraintType.PARITY_CONSTRAINED, CodeType.DENSE, SolverType.CPLEX);
////			RunParams params = new RunParams(true, 30, 1, 
////					ConstraintType.PARITY_CONSTRAINED, CodeType.DENSE, SolverType.CPLEX);
////			runner.runHopeBatch("/home/jp/research/ICML13-Dataset/Grids_att_10/", "/home/jp/Dropbox/HOPE/wish_grid_360_w10", params);
////			runner.runHopeBatch("/home/jp/research/ICML13-Dataset/Grids/", "/home/jp/Dropbox/HOPE/hope_grid_test", params);
////			parseResult();
////			runner.runHopeBatch("/home/jp/research/ICML13-Dataset/Grids/", "/home/jp/Dropbox/HOPE/hope_grid_fast_unconstrained_10",10, false);
////			runner.runHopeBatch("/home/jp/research/ICML13-Dataset/Grids/", "/home/jp/Dropbox/HOPE/hope_grid_fast_constrained_30",30, false);
////			runner.runWishBatch("/home/jp/research/ICML13-Dataset/Grids/","/home/jp/research/HOPE/wish_grid_10_new", null, 10);
////			runner.runHopeBatch("/home/jp/research/ICML13-Dataset/Cliques_small/", "/home/jp/research/hope_cliq_small");
//
////			String[] filter = {"grid_mixed_n10_w0.75_f0.1.uai","grid_attractive_n10_w2.75_f0.1.uai","grid_attractive_n10_w1.25_f1.0.uai","grid_mixed_n10_w1.75_f0.1.uai","grid_mixed_n10_w2.75_f0.1.uai","grid_attractive_n10_w2.0_f1.0.uai","grid_mixed_n10_w1.75_f1.0.uai"};
////			runner.runWishBatch("/home/jp/research/ICML13-Dataset/Cliques/","/home/jp/research/wish_clique",null);
////			runner.runWishBatch("/home/jp/research/ICML13-Dataset/Grids/","/home/jp/research/wish_grid", filter);
//			
////			runner.profileHope("/home/jp/research/ICML13-Dataset/Grids/grid_mixed_n10_w3.0_f1.0.uai", "/home/jp/research/hope_grid_mixed_3.0", time);
////			for(int i=0;i<time.length;i++){
////				int t = time[i];
////				runner.runWish("/home/jp/research/ICML13-Dataset/Grids/grid_mixed_n10_w0.75_f0.1.uai", "/home/jp/research/wish_grid_mixed_0.75", t);
////			}
//			
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}

	
	
//	/**
//	 * run junction tree on all the  datasets under a certain folder
//	 * @param pathToFolder path to the data set file
//	 */
//	public void runLibDai(String pathToFolder, String outPath, int alg){
//		File folder = new File(pathToFolder);
//		File[] datasets = folder.listFiles();
//		for(File f:datasets){
//			String path = f.getAbsolutePath();
//			if(path.endsWith(".fg")){
//				//call uai2fg
//				CommandLine cl = new CommandLine(Config.pathToLibDai);
//				cl.addArgument(path);
//				
//				Executor exec = new DefaultExecutor();
//				ResultOutputStream ros;
//				try {
//					ros = new ResultOutputStream(alg, outPath, path);
//					exec.setStreamHandler(new PumpStreamHandler(ros));
//					exec.setWorkingDirectory(new File(Config.pathToWISHFolder));
//					exec.setExitValues(null);
//					long start = new Date().getTime();
//					exec.execute(cl);
//					long end = new Date().getTime();
//					ros.printTime((end-start)/1000);
//					ros.close();
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//		}
//	}
	
	
//	/**
//	 * convert .uai to .fg (libDAI only allows .fg format)
//	 * @param pathToFolder
//	 * @throws FileNotFoundException
//	 */
//	public void uaiToFg(String pathToFolder) throws FileNotFoundException{
//		File folder = new File(pathToFolder);
//		File[] datasets = folder.listFiles();
//		for(File f:datasets){
//			String path = f.getAbsolutePath();
//			
//			if(path.endsWith(".uai")){
//				//generate empty evid file
//				PrintWriter out = new PrintWriter(path+".evid");
//				out.print("0");
//				out.close();
//				
//				//call uai2fg
//				String base = path.substring(0, path.length()-4);				
//				CommandLine cl = new CommandLine(Config.pathToUai2Fg);
//				cl.addArgument(base);
//				cl.addArgument("0");
//				cl.addArgument("0");
//				
//				Executor exec = new DefaultExecutor();
//				try {
//					exec.setExitValues(null);
//					exec.execute(cl);
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//		}		
//	}
	
	private boolean matchPattern(String input, String[] patterns){
		for(String pattern:patterns){
			if(input.indexOf(pattern)>=0){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * run wish on a single dataset
	 * @param datasetPath path to the data set file
	 * @param outputPath output file
	 * @param timeout time out (in seconds)
	 */
	public void runWish(String datasetPath, String outputPath, int timeout){
		CommandLine cl = new CommandLine("python");
		cl.addArgument(Config.pathToWISH);
		cl.addArgument(datasetPath);
		cl.addArgument("log_"+new Date().getTime());
		cl.addArgument("-timeout");
		cl.addArgument(""+timeout);
		
		Executor exec = new DefaultExecutor();
		ResultOutputStream wos;
		try {
			wos = new ResultOutputStream(ResultOutputStream.WISH,outputPath, datasetPath);
			exec.setStreamHandler(new PumpStreamHandler(wos));
			exec.setWorkingDirectory(new File(Config.pathToWISHFolder));
			exec.setExitValues(null);
			long start = new Date().getTime();
			exec.execute(cl);
			long end = new Date().getTime();
			wos.printTime((end-start)/1000);
			wos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void runHope(File f, StringBuilder result, RunParams params){
		Hope hope = new Hope();
		long start = new Date().getTime();
		RunResult rr = new RunResult();
		double estimate = hope.fastRun(f.getAbsolutePath(),7,params,rr);
		long end = new Date().getTime();
		result.append(f.getName()).append(",");
		result.append(Math.log(estimate)).append(",");
		result.append(rr.saving).append(",");
		result.append( (end-start)/1000);
		result.append("\n");
	}
	
	public void runWishBatch(String pathToFolder, String outputPath, String[] filter,int timeout) throws ExecuteException, IOException{
		File folder = new File(pathToFolder);
		File[] datasets = folder.listFiles();
		for(File f:datasets){
			String fileName = f.getName();
			if(fileName.endsWith(".uai")&& (filter==null || !matchPattern(fileName,filter))){
				runWish(f.getAbsolutePath(), outputPath, timeout);
			}
		}
	}
	
	public void runHopeBatch(String pathToFolder, String outputPath, RunParams params){
		File folder = new File(pathToFolder);
		File[] datasets = folder.listFiles();
		StringBuilder result = new StringBuilder();
		for(File f:datasets){
			if(f.getName().endsWith(".uai")){
				System.gc();
				runHope(f, result, params);
			}
		}
		try {
			PrintWriter out = new PrintWriter(outputPath);
			out.print(result.toString());
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	class ResultOutputStream extends LogOutputStream{
		private PrintWriter writer;
		private FileWriter fw;
		private BufferedWriter bw;
		private final String WISH_PREFIX = "Final log-estimate: ";
		private final String JTREE_PREFIX = "Exact log partition sum: ";
		private final String BP_PREFIX = "Approximate (loopy belief propagation) log partition sum: ";
		private final String MF_PREFIX = "mf log partition sum: ";
		private final String TRWBP_PREFIX = "trwbp log partition sum: ";
		private int type;
		public static final int WISH=0;
		public static final int JTREE=1;
		public static final int BP=2;
		public static final int MF=3;
		public static final int TRWBP=4;
		public ResultOutputStream(int type, String path, String datasetPath) throws IOException{
			this.type=type;
			this.fw = new FileWriter(path, true);
			this.bw = new BufferedWriter(fw);
			this.writer = new PrintWriter(bw);
			this.writer.print(datasetPath);
			this.writer.print(",");
		}
		
		@Override
		protected void processLine(String line, int level) {
			// TODO Auto-generated method stub
			System.out.println(line);
			String prefix;
			switch(type){
				case JTREE: 
					prefix = JTREE_PREFIX;
					break;
				case BP: 
					prefix = BP_PREFIX;
					break;
				case MF:
					prefix = MF_PREFIX;
					break;
				case TRWBP:
					prefix = TRWBP_PREFIX;
					break;
				default:
					prefix = WISH_PREFIX;
			}
			if(line!=null && line.contains(prefix)){
				this.writer.print(line.substring(prefix.length()));
				this.writer.print(",");
			}
		}
		
		public void printTime(long time){
			this.writer.print(time);
			this.writer.print("\n");
		}
		
		@Override
		public void close(){
			this.writer.flush();
			this.writer.close();
		}
		
	}
}
