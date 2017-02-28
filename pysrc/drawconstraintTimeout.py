#!/usr/bin/env python
# -*- coding: utf-8 -*-
import matplotlib.pyplot as plt
import pandas as pd
import math


inpFile = '../output/constraintTimeout.csv'

df = pd.DataFrame.from_csv(inpFile)
df /= math.log(2)
df.plot(style=['k--','b--^','g--o', 'k-','b-^','g-o'])
plt.xticks(list(df.index))
plt.xlabel('Timeout(seconds)')
plt.ylabel('Best solution')
# plt.legend(loc=(.45,.2))
plt.show()
