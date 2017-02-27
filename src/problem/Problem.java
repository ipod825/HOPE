package problem;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public abstract class Problem {
	protected String path;
	protected int numVar=0;
	protected int numFn=0;
	protected int[][] fnVarInd=null;
	protected double[][] fnVal=null;
	
	public int[][] getFnVarInd(){return this.fnVarInd;}
	public double[][] getFnVal(){return this.fnVal;}
	public int getNumVar(){return this.numVar;}
	public int getNumFn(){return this.numFn;}
	public String getPath(){return this.path;}
	
	public Problem(String path){
		this.path = path;
		try {
			InputStream fis = new FileInputStream(path);
			BufferedReader  br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));
			this.parse(br);
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected abstract void parse(BufferedReader  br) throws FileNotFoundException, IOException;
}
