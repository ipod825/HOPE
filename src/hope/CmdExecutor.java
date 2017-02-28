package hope;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
			InputStream istream = null;

			if(outputPath!=null){
				Files.copy(p.getInputStream(), Paths.get(outputPath));
				istream = new FileInputStream(outputPath);
			}
			else{
			    istream = p.getInputStream();
			}
			BufferedReader reader = new BufferedReader(new InputStreamReader(istream));
			
			String line;
			while ((line = reader.readLine()) != null) {
				if(line.startsWith(prefix)){
					res = Double.parseDouble(line.substring(prefix.length()));
					break;
				}
			}
			reader.close();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return res;
	}	
}
