package hope;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;

class CmdExecutor{

	private String prefix;
	
	public CmdExecutor(String prefix){
		this.prefix = prefix;
	}
	
	public double runCmd(String cmd){
		return this.runCmd(cmd, null);
	}
	
	public double runCmd(String cmd, String outputPath){
		double res = Double.NEGATIVE_INFINITY;
		try {
			Process p = Runtime.getRuntime().exec(cmd);
			if(outputPath!=null){
				Files.copy(p.getInputStream(), Paths.get(outputPath));
				return res;
			}
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
			
			String line;
			while ((line = stdInput.readLine()) != null) {
				if(line.startsWith(prefix)){
					res = Double.parseDouble(line.substring(prefix.length()));
					break;
				}
			}
			stdInput.close();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return res;
	}	
}
