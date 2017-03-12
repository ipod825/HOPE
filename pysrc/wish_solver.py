#!/usr/bin/env python
# -*- coding: utf-8 -*-

# This file was modified from ../thirdparty/WishCplex/WISHCPLEX.py

import os
import argparse
import config
from WISHLogProcess import process_logs_cplex_LB
import math
import datetime
from utils import bash

from joblib import Parallel, delayed


def estimateOne(i, t, samplesize, cmds):
    ind = i*samplesize+t
    bash(cmds[ind], forgive_error=[124])


class Wish(object):
    def __init__(self, samplesize=7, timeout=10, log_dir=os.path.join(config.tmp_dir, 'wish'), alpha=None, delta=None):
        self.samplesize = samplesize
        self.timeout = timeout
        self.log_dir = log_dir
        self.alpha = alpha
        self.delta = delta
        self.parallel = False

    def solve(self, path):
        WH_cplex = '../thirdparty/WishCplex/goodWH_cplex'
        # WH_cplex = '../thirdparty/WishCplex/WH_cplex'

        file_name = os.path.basename(path)
        ind = 0
        num_var = 0
        with open(path, 'r') as f:
            for l in f:
                if not l.strip()=='':
                    ind = ind +1
                    if ind==2:
                        num_var=int(l)
                        break

        if self.alpha and self.delta:
            self.samplesize = int(math.ceil(math.log(num_var)*math.log(1.0/self.delta)/self.alpha))

        log_dir = self.log_dir+str(datetime.datetime.now().time())
        bash(['mkdir -p', log_dir])
        if not self.parallel:
            for i in range(0,num_var+1):
                samplesize = 1 if i==0 else self.samplesize
                for t in range(1, samplesize+1):
                    logfilename = '{}.xor{}.loglen{}.{}.ILOGLUE.uai.LOG'.format(os.path.basename(file_name), i, 0, t)
                    os.system('timeout {} {} -paritylevel 1 -number {} -seed 10 {} > {}'.format(self.timeout, WH_cplex, i, path, log_dir+"/"+ logfilename))

        else:
            logfilename = '{}.xor{}.loglen{}.{}.ILOGLUE.uai.LOG'.format(os.path.basename(file_name), 0, 0, 1)
            os.system('timeout {} {} -paritylevel 1 -number {} -seed 10 {} > {}'.format(self.timeout, WH_cplex, 0, path, log_dir+"/"+ logfilename))

            cmds = []
            for i in range(1,num_var+1):
                for t in range(1,self.samplesize+1):
                    logfilename = '{}.xor{}.loglen{}.{}.ILOGLUE.uai.LOG'.format(os.path.basename(file_name), i, 0, t)
                    cmds.append('timeout {} {} -paritylevel 1 -number {} -seed 10 {} > {}'.format(self.timeout, WH_cplex, i, path, log_dir+"/"+ logfilename))

            Parallel(n_jobs=20)(delayed(estimateOne)(i-1, t-1, self.samplesize, cmds) for i in range(1,num_var+1) for t in range(1,self.samplesize+1))

        log_est = process_logs_cplex_LB(log_dir)
        # process_logs_cplex_UB(self.outfolder)
        return log_est


def parse_args():
    parser = argparse.ArgumentParser(description='Estimate the partition function using the WISH algorithm and CPLEX for the optimization.')

    parser.add_argument("infile", help="Graphical model (in UAI format)")
    parser.add_argument("-o", "--outfolder", default='../tmp/wish', help="Folder where logs are stored")
    parser.add_argument("-s", "--samplesize", type=int, default=7, help="Number of samples per quantile")
    parser.add_argument('-t', '--timeout', type=int, default=10, help="Timeout for each optimization instance (seconds)")
    return parser.parse_args()

if __name__ == '__main__':
    args = parse_args()
    wish = Wish(args.samplesize, args.timeout, args.outfolder)
    print(wish.solve(args.infile))
