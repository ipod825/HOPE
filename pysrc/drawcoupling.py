#!/usr/bin/env python
# -*- coding: utf-8 -*-
import matplotlib.pyplot as plt
import numpy as np
import os
from collections import defaultdict
import pandas as pd
import math
import re
import sys
import argparse


def parse_arg(argv):
    parser = argparse.ArgumentParser(description='draw coupling')
    parser.add_argument('inpdir', help='input directory')
    return parser.parse_args(argv[1:])

args = parse_arg(sys.argv)
inpdir = args.inpdir
coupling = list(np.arange(0.25,3.25,0.25))
data = defaultdict(lambda:[np.nan]*len(coupling))
for path in os.listdir(inpdir):
    model = path
    with open(os.path.join(inpdir, path), 'r') as f:
        for line in f:
            c = re.search("_w(\d\.\d*)_", line).group(1)
            score = float(line.split(',')[1])
            data[model][coupling.index(float(c))] = score

df = pd.DataFrame(data)
df['coupling'] = coupling
df = df.set_index('coupling')
df = df.rename(columns={'JunctionTree':'Exact'})
for alg in df:
    if alg!='Exact':
        df[alg] = (df[alg] - df['Exact'])/math.log(2)
df['Exact'] = 0
# cols = [ci for ci in ['TRWBP', 'wish', 'Exact', 'hope', 'MF'] if ci in df.columns]
# df = df[cols]
df.plot(style=['c-o','b-^','--', 'g-s','r--'])
plt.xticks(np.arange(0.5,3.25,0.5))
# plt.yticks(range(-40,20,10))
plt.xlabel('Coupling strength')
plt.ylabel('Log Error')
plt.ylim(min(df.min())-1, max(df.max())+1)
plt.legend(loc="lower left")
plt.show()
