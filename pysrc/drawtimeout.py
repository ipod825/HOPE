#!/usr/bin/env python
# -*- coding: utf-8 -*-
import matplotlib.pyplot as plt
import numpy as np
import os
from collections import defaultdict
import pandas as pd
import math


inpdir = '../output/timeout'
timeout = [10,30,60,180,360]
data = defaultdict(lambda:[np.nan]*len(timeout))
for path in os.listdir(inpdir):
    s = path.split('_')
    model = s[0]
    with open(os.path.join(inpdir, path), 'r') as f:
        score = float(f.readline().split(',')[-2])
        if len(s)==2:
            data[model][timeout.index(int(s[1]))] = score
        else:
            data[model] = [score]*len(timeout)
            exact_ans = score

data['timeout'] = timeout
df = pd.DataFrame(data)
df = df.set_index('timeout')
for alg in df:
    df[alg] = (df[alg] - exact_ans)/math.log(2)
df.plot(style=['-^','-o','-s', '-v'])
plt.xticks(timeout)
plt.xlabel('Timeout(seconds)')
plt.ylabel('Log(Z)')
plt.ylim(min(df.min())-1, max(df.max())+1)
plt.legend()
plt.show()
