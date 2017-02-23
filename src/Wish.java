import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;


public class Wish implements Solver{

	private int timeout;
	
	public Wish(int timeout){
		this.timeout = timeout;
	
	}
	
	public double solve(String path) throws ExecuteException, IOException{
		CommandLine cl = new CommandLine("python");
		cl.addArgument(Config.pathToWISH );
		cl.addArgument(path);
		cl.addArgument("log_"+new Date().getTime());
		cl.addArgument("-timeout");
		cl.addArgument(""+this.timeout);
		
		Executor exec = new DefaultExecutor();
		CmdOutputHandler cmdOutputHandler = new CmdOutputHandler("Final log-estimate: ");
		exec.setStreamHandler(new PumpStreamHandler(cmdOutputHandler));
		exec.setWorkingDirectory(new File(Config.pathToWISHFolder));
		exec.setExitValues(null);
		exec.execute(cl);
		return Math.exp(cmdOutputHandler.result);
	}
}