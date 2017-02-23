import argparse
from utils import bash


def parse_args():
    parser = argparse.ArgumentParser(description='Estimate the partition function using the WISH algorithm and CPLEX for the optimization.')

    parser.add_argument("infile", help="Graphical model (in UAI format)")
    parser.add_argument('-s', '--sample_num', type=int, help="Number of samples per quantile", default=7)
    parser.add_argument('-t', '--timeout', type=int, help="Timeout for each optimization instance (seconds)", default=10)
    args = parser.parse_args()
    return args

if __name__ == '__main__':
    WH_cplex = '../thirdparty/WishCplex/WH_cplex'
    args = parse_args()

    ind = 0
    origNbrVar = 0
    with open(args.infile, 'r') as f:
        for l in f:
            if not l.strip()=='':
                ind = ind +1
                if ind==2:
                    origNbrVar=int(l)
                    break

    # T = int(math.ceil(math.log(origNbrVar)*math.log(1.0/args.delta)/args.alpha))
    T = args.sample_num

    for quantile in range(0,origNbrVar+1):
        if quantile==0:
            sample_num=1
        else:
            sample_num=T
        for t in range(1,sample_num+1):
            bash("timeout {} {} -paritylevel 1 -number {} -seed 10 {}".format(args.timeout, WH_cplex, quantile, args.infile))
