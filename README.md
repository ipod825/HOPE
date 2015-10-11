# HOPE
Approximate inference for MRF based on ECC

# Disclaimer
The project has only been tested on Ubuntu 12.04

# Prerequisites
CPLEX

LocalSolver

Eclipse (or any Java IDE that supports Eclipse's project configuration, ex: IntelliJ IDEA)

# Install
git clone https://github.com/pjiangtw/HOPE.git

Open Eclipse. Import project.

Modify "Config.java", specifically the variable: public static final String output = "/home/jp/output/" (You should create a work folder anywhere you like and point this variable to the folder.)

Import localsolver.jar by modifying Java build path. In Eclipse, you can right click on the project, select "Properties", and choose "Java Build Path".

# Running
An example can be seen in the main() function in Hope.java:
		Hope hope = new Hope();
		RunParams params = new RunParams(30, ConstraintType.PARITY_CONSTRAINED,
				CodeType.PEG, SolverType.CPLEX);
		double est = hope.fastRun(<path to .uai file>, 7, params);

Some notes on the parameter:
		RunParams params = new RunParams(30 <timeout in seconds>, ConstraintType.PARITY_CONSTRAINED<>,
				CodeType.PEG<what code to use>, SolverType.CPLEX<what solver to use>);

		double est = hope.fastRun(<path to .uai file>, 7 <how many optimization instances per quantile>, params);
