import java.io.IOException;
import org.apache.commons.exec.ExecuteException;

public interface Solver{
	public double solve(String path)  throws ExecuteException, IOException;
}