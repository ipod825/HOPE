package hope;

import java.io.IOException;
import org.apache.commons.exec.ExecuteException;

import problem.Problem;

public interface Solver{
	public double solve(Problem problem)  throws ExecuteException, IOException;
}