# -*- coding: utf-8 -*-
"""
Created on Fri Jan 21 23:46:53 2022

@author: tudor_ytmdyrk
"""

import numpy as np
import pandas as pd
import os


if os.getcwd() != "C:\\Users\\tudor_ytmdyrk\\Desktop\\licenta":
    os.chdir("C:\\Users\\tudor_ytmdyrk\\Desktop\\licenta")


#%%

bazaOriginala = pd.read_csv("health.csv")
bazaOriginala.head()
print(len(bazaOriginala))


bazaCurata = bazaOriginala.drop_duplicates(bazaOriginala.columns)#
print(len(bazaCurata))                                            