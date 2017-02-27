#!/usr/bin/env python
# -*- coding: utf-8 -*-
import matplotlib.pyplot as plt
import pandas as pd
import math


inpFile = '../output/constraint.csv'

df = pd.DataFrame.from_csv(inpFile)
df /= math.log(2)
df.plot(style=['g.','r-^','b-o'])
plt.xlabel('Number of parity constraints')
plt.ylabel('Best solution')
plt.show()
