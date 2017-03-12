#!/usr/bin/env python
# -*- coding: utf-8 -*-
import matplotlib.pyplot as plt
import pandas as pd
import math


inpFile = '../output/LSconstraint.csv'

df = pd.DataFrame.from_csv(inpFile)
# df = df[:40]
df /= math.log(2)
df.plot(style=['g--','b-o','g-s','r-^'])
plt.xlabel('Number of parity constraints')
plt.ylabel('Best solution')
plt.show()
