# -*- coding: utf-8 -*-
"""
Created on Thu May 12 12:20:02 2022

@author: tudor_ytmdyrk
"""

"""Acesta este un parser de packete si generator de cod"
"ca sa imit exact configuratia facuta de bratara pe aplicatia"
"initiala si aia e"""

import pandas as pd
import matplotlib.pyplot as plt
import os


#%%

"""Parserul fisierului"""

if os.getcwd() != 'C:\\Users\\tudor_ytmdyrk\\Desktop':
    os.chdir('C:\\Users\\tudor_ytmdyrk\\Desktop')
    
fisier_text = open('comenzi_kt.txt')

#fie o lista mare cu liste mici
#ordinea urmatoarea: uuid, valoare, source, destination
#de asemenea in fisierul text are 52 de linii un packet

lista_linii = []
iCounter_p = 0
end_of_file = 1

frame_number = ''
while end_of_file == 1:
    linie = fisier_text.readline()
    if linie == '':
        end_of_file = 0
        break
    if "byteArrayOf" in linie:
        linie = linie.rstrip()
        linie = linie.lstrip()
        iCounter_p = 0
        if "(" in linie: #verificam daca e scris pe o linie
            iCounter_p = iCounter_p + 1
        if ")" in linie: 
            iCounter_p = iCounter_p - 1
        while iCounter_p != 0:
            linie2 = fisier_text.readline()
            if "(" in linie2:
                iCounter_p = iCounter_p + 1
            if ")" in linie2: 
                iCounter_p = iCounter_p - 1
            linie2 = linie2.lstrip()
            linie2 = linie2.rstrip()    
            linie = linie + linie2
        #linie = linie +"\n"
        lista_linii.append(linie)
        print(linie)

#%%            

lista_bytes = []
lista_charac = []
for i in lista_linii:
    j = i.split("?", maxsplit=1)
    lista_charac.append(j[0])
    j = j[1].split(" = ")[1]
    lista_bytes.append(j)


#%%
    

cod_iesire = open("reformatat.txt", "w")

for i in range(len(lista_bytes)):
    cod_iesire.write("listaComenzi.add(Comanda(\"" + str(lista_charac[i]) +"\"," + str(lista_bytes[i]) + ")\n")

    
cod_iesire.close()
    
    
    
    
    
    
    
    
    
    
    
