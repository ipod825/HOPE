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
