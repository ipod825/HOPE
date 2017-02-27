package problem;

import java.io.BufferedReader;
import java.io.IOException;

public class Ising extends Problem{

	public Ising(String path) {
		super(path);
	}

	@Override
	protected void parse(BufferedReader br) throws NumberFormatException, IOException{
		String line;
		int lineNo = 1;
		int fnCounter = 0;
		int fnValCounter = 0;
		int numVal=0;
		boolean readFuncTable = false;
		
		while ((line = br.readLine()) != null) {
			if(line.length() == 0){
				continue;
			}else if(lineNo==2){
				this.numVar = Integer.parseInt(line);
			}else if(lineNo==4){
				this.numFn = Integer.parseInt(line);
				this.fnVarInd = new int[this.numFn][];
				this.fnVal = new double[this.numFn][];
			}
			else if(lineNo>=5 && lineNo<5+this.numFn){
				String[] parts = line.split("\\s+");
				int numVar = Integer.parseInt(parts[0]);
				
				if(numVar != parts.length-1){
					System.err.println("Invliad function line: "+line+". Number of function variable mismatch.");
					br.close();
					System.exit(-1);
				}
				
				this.fnVarInd[fnCounter] = new int[numVar];
				for(int i=1;i<parts.length;i++){
					//we put var0,var1,...,varn as [varn,...,var1,var0]
					this.fnVarInd[fnCounter][parts.length-1-i] = Integer.parseInt(parts[i]);
				}
				if(lineNo==this.numFn+4){
					assert fnCounter==this.numFn-1;
					fnCounter = 0;
				}else{
					++fnCounter;
				}
			}else if(lineNo>=5+this.numFn){
				if(!readFuncTable){
					//read the first line of a function table which specifies the number of values
					numVal = Integer.parseInt(line);
					this.fnVal[fnCounter] = new double[numVal];
					readFuncTable=true;
					fnValCounter = 0;
				}else{
					//read function table
					String[] parts = line.split("\\s+");
					for(int p=0;p<parts.length;p++){
						this.fnVal[fnCounter][fnValCounter++] = Double.parseDouble(parts[p]);
						if(fnValCounter>=numVal){
							readFuncTable=false;
							++fnCounter;
						}						
					}
				}
			}
			lineNo++;
		}
	}
}
