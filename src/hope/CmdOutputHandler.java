package hope;
import org.apache.commons.exec.LogOutputStream;

class CmdOutputHandler extends LogOutputStream{

	private String prefix;
	public double result=-1;
	public CmdOutputHandler(String prefix){
		this.prefix = prefix;
	}
	
	@Override
	protected void processLine(String line, int level) {
		if(line!=null && line.contains(this.prefix)){			
			assert result==-1;
			this.result = Double.parseDouble(line.substring(this.prefix.length()));
		}
	}
}