#!/usr/bin/env python
# -*- coding: utf-8 -*-
import sys
import os
import timeit
import config
from config import SolverType
from libdai_solver import JunctionTree, MF, TRWBP
from wish_solver import Wish
import argparse
import math
from utils import listEnum


def parse_arg():
    parser = argparse.ArgumentParser(description='Run different algorithms for a problem in a directory')
    parser.add_argument("indir", help="Direcory where problems are stored.")
    parser.add_argument("solver_type", type=int, help="SolverType:"+listEnum(SolverType))
    parser.add_argument("-s", "--samplesize", type=int, default=7, help="Saplesize per quantile.")
    parser.add_argument("-t", "--timeout", type=int, default=10, help="Timeout for optimization.")
    parser.add_argument("-o", "--outdir", default=config.output_dir, help="Directory where output are stored")
    return parser.parse_args()

if __name__ == '__main__':
    args = parse_arg()
    solver_type = args.solver_type
    timeout = args.timeout

    isLog = True
    if solver_type == SolverType.JT:
        solver = JunctionTree()
    elif solver_type == SolverType.MF:
        solver = MF()
    elif solver_type == SolverType.TRWBP:
        solver = TRWBP()
    elif solver_type == SolverType.WISH:
        solver = Wish(samplesize=args.samplesize, timeout=args.timeout)
    else:
        isLog = False

    output_path = os.path.join(args.outdir,solver.__class__.__name__)
    if solver_type==SolverType.WISH:
        output_path += '_'+str(timeout)

    # empty the file ifit exists
    open(output_path, 'w').close()

    for path in os.listdir(args.indir):
        if not path.endswith('.uai'):
            continue
        path = os.path.join(args.indir, path)

        start = timeit.default_timer()
        res = solver.solve(path)
        stop = timeit.default_timer()
        if not isLog:
            res = math.log(res)
        output = open(output_path, 'a')
        output.write('{},{},{}\n'.format(os.path.basename(path), res, stop - start))
        print '{},{},{}\n'.format(os.path.basename(path), res, stop - start)
        output.close()
