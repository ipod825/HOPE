# HOPE
Approximate inference for MRF based on ECC

# Disclaimer
The project has only been tested on Ubuntu 12.04

# Prerequisites
CPLEX

LocalSolver: sign up at http://www.localsolver.com. You can download the library .jar file. There is a hard cap on the problem size for trial license. You can also apply for academic license.

Eclipse (or any Java IDE that supports Eclipse's project configuration, ex: IntelliJ IDEA)

# Setup
git clone https://github.com/pjiangtw/HOPE.git

Open Eclipse. Import project.

Modify "Config.java", specifically the variable: public static final String output = "/home/jp/output/" (You should create a work folder anywhere you like and point this variable to the folder.)

Import localsolver.jar by modifying Java build path. In Eclipse, you can right click on the project, select "Properties", and choose "Java Build Path".

# Running
An example can be seen in the main() function in Hope.java:

		Hope hope = new Hope();
		RunParams params = new RunParams(30, ConstraintType.PARITY_CONSTRAINED,
				CodeType.PEG, SolverType.CPLEX);
		double est = hope.fastRun("/home/user/test.uai", 7, params);

Some notes on the parameter:

		RunParams params = new RunParams(30 [timeout in seconds], ConstraintType.PARITY_CONSTRAINED[what type of constraints to use],
				CodeType.PEG[what code to use], SolverType.CPLEX[what solver to use]);


		double est = hope.fastRun("/home/user/test.uai"[path to .uai file], 7 [how many optimization instances per quantile], params);

Therefore, for CPLEX+Parity(PEG LDPC), one can use:

		Hope hope = new Hope();
		RunParams params = new RunParams(30, ConstraintType.PARITY_CONSTRAINED,
				CodeType.PEG, SolverType.CPLEX);
		double est = hope.fastRun("/home/user/test.uai", 7, params);

Similarly, for LocalSolver+affine map(Unconstrained), one can use

		Hope hope = new Hope();
		RunParams params = new RunParams(30, ConstraintType.UNCONSTRAINED,
				CodeType.PEG, SolverType.LocalSolver);
		double est = hope.fastRun("/home/user/test.uai", 7, params);

If one would like to use LDPC for large domain and affine map for low domain to get the best of both worlds, as recommended, 

		Hope hope = new Hope();
		RunParams params = new RunParams(30, ConstraintType.TWO_THIRD,
				CodeType.PEG, SolverType.BY_CONSTRAINTS);
		double est = hope.fastRun("/home/user/test.uai", 7, params);

In this case, if the code rate r is greater than or equal to 2/3, CPLEX+Parity will be used. Otherwise, the base solver will be switched to LocalSolver+affine map. If we would like to switch between solvers automatically based on optimization performance, instead of using a fixed switch point, we can change the constraint type to "ConstraintType.GO_WITH_THE_BEST".

# Interface with CPLEX
The interface with CPLEX is adapted from the source code of the UAI version of WISH. A modified version has been packaged under the folder "WishCplex". Here we modify the WishCplex by providing a few additional arguments:

-skipelim: if provided, WishCplex will skip Gaussian elimination. For LDPC it is recommended to skip Gaussian elimination as the elimination process will impair the structure of parity matrix.

-matrix [parity matrix]: We also provide the option to specify a parity matrix for WishCplex to use. The parity matrix is expressed in the following format:

00111_10110_01000

where "_" is used as the row delimiter. This string represents a parity matrix of 5 variables and 3 checks.

We can use the following command to invoke WishCplex on the problem "test.uai" with a 30-second timeout, 3 checks 
and the parity matrix 00111_10110_01000

WH_cplex -paritylevel 1 -timelimit 30(timeout in seconds) -number 3(number of checks) -skipelim -matrix 00111_10110_01000 /home/user/test.uai

# A quick guide to the source code

Hope.java: the core of the inference algorithm. It will choose which quantile to estimate and generate optimization instances.

RunParams.java: the class to hold the parameters (timeout, solver, constraint type,...etc). It also provide the function to generate an optimization instance based on the parameters.

CplexInstance: a Cplex optimization instance. It will invoke the WishCplex and provide a desired parity matrix.

LSInstance: a LocalSolver optimization instance. It will parse the .uai file and convert the problem into LocalSolver format.

The aforementioned files constitutes the core of the code. The rest are mostly helpers.
