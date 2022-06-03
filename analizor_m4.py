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
    
fisier_text = open('m4_packets_16_22_27mai.txt')

#fie o lista mare cu liste mici
#ordinea urmatoarea: uuid, valoare, source, destination
#de asemenea in fisierul text are 52 de linii un packet


lista_mare = []
end_of_file = 1

frame_number = ''
while end_of_file == 1:
    packet = []
    linie = fisier_text.readline()
    if linie == '':
        end_of_file = 0
        break
    if linie.startswith('Frame'):
        frame_number = linie.split(':')[0]
    
    if linie.startswith('Bluetooth Attribute Protocol'):
        for i in range(8):
            linie = fisier_text.readline()
            
            if linie.startswith('        [UUID:'):
                linie.replace('[', '')
                linie.replace(']', '')
                linie.rstrip('\n')
                linie.rstrip(']')
                linie = linie.split(':')
                packet.append(linie[1].split(']')[0].lstrip(' ')) #uuid
            elif linie.startswith('    Value:'):
                linie = linie.rstrip('\n')
                linie = linie.split(':')
                packet.append(linie[1].lstrip('  ')) #valoare
        packet.append(frame_number)
        lista_mare.append(packet)
fisier_text.close()

#%%            
        
"""lista mare are acum toate valorile care ne intereseaza
foarte important de tinut minte ca bratara cand raspunde incepe
cu '10' la chestii deci mna, si oricum, cred ca putem sa ne dam
seama cine cui trimite"""    
    

lista_mare = pd.DataFrame(lista_mare)#stringuri
lista_mare = lista_mare.astype('|S')

#%%
    
counts = lista_mare[0].value_counts()

etichete = [counts.axes[0][i] for i in range(len(counts))]
#o sa mai scurtam id-urile caracteristiclor la primul numar
etichete = [i[:8] for i in etichete]
numere = counts.to_list()
#%%
plt.figure(figsize=(15, 5))
plt.title('Official App Messages with Characteristics')
plt.xlabel('Characteristic UUID')
plt.ylabel('Exchanges')

plt.xticks(rotation=45)
plt.bar(etichete, numere)
plt.grid()
plt.tight_layout(1.1)
    
    
    
    
#%%

#deleting the responses from the app, leaving us with just
#what values the app has written to the band
#also, fuck pandas

lista_mare = lista_mare.values.tolist()
#%%
lista_writeuri = [i for i in lista_mare if i[0].startswith(b'6e400002b')==True] #scapa de valorile none
#din cauza unui bug, trebuie sa scoatem si comunicatiile care au Frame la valoare
#facem filtrarea cu liste
lista_primite = [i for i in lista_mare if i[0].startswith(b'6e400003b')==True]

#pentru varianta care mergea, mai jos era lista mare in loc de
#lista writeuri, chair daca era gresit
#lista_writeuri = [i for i in lista_writeuri if i[1].startswith(b'Fr')==False] #scapa de valorile none
#lista_toate = [i for i in lista_mare if i[1].startswith(b'Fr')==False] #scapa de valorile none

#URMATOAREA LINIE DE COD E CRUCIALA PTR A FACE DIFERENTA INTRE WRITEURI SI RESPONSUR
#lista_writeuri = lista_toate


#%%

lista_writeuri = pd.DataFrame(lista_writeuri)
lista_mare = pd.DataFrame(lista_mare)


#%%
counts = lista_writeuri[0].value_counts()

etichete = [counts.axes[0][i] for i in range(len(counts))]
#o sa mai scurtam id-urile caracteristiclor la primul numar
etichete = [i[:8] for i in etichete]
numere = counts.to_list()

plt.figure(figsize=(15, 5))
plt.title('Official App most often written to characteristics')
plt.xlabel('Characteristic UUID')
plt.ylabel('Write Operations')

plt.xticks(rotation=45)
plt.bar(etichete, numere)
plt.grid()
plt.tight_layout(1.1)

    
#%%

#cod pentru a imita toate scrierile mai putin cea de authenticare    

lista_charac = lista_writeuri[lista_writeuri.columns[0]].unique()
lista_charac.sort()


lista_w = lista_writeuri.values.tolist()
lista_w = [[i[0], i[1]] for i in lista_w if i[0].startswith(b'00000009')==False]

#%%
time_milis = 2000 #delay la inceput de 2000
increment = 125 #milis

cod_kotlin = open("M4_generatudor.txt", "w+")

lista_char = ['pad','pad', 'pad', 'caracteristicaComenzi', 'charac_4', 'pad', 'charac_6','charac_7', 'charac_8', 'pad', 'charac_20' ]

    
for i in lista_w:
    
    
    to_write = i[1]
    to_write = to_write.decode('utf-8')
    charac = i[0]
    pozitie_charac = 0
    if charac.startswith(b'6e400002b'):
        pozitie_charac = 3

        
        #la char 9 nu scriem ca e de authenticare
        
    temp = []
    
    
    for j in range(0, len(to_write), 2):
        
        byte = str(to_write[j]) + str(to_write[j+1])
        
        if bytes(byte.encode('utf-8')) >= b'80':
            numar = str( int(byte, 16)) + ".toByte()"
        else:
            numar = '0x' + byte
        
        temp.append(numar)
    
    
    
    handler = "Handler(Looper.getMainLooper()).postDelayed({\n"
    charac = "\t " +lista_char[pozitie_charac] + "?.value = byteArrayOf(" 
    
    for j in range(len(temp)):
    
        charac = charac + temp[j]
        
        if j != len(temp) - 1:# sa nu puna virgula dupa ult elemnt
            charac = charac + ', '
        
    
    
    
    charac = charac + ")\n"
    gatt = "\t gatt?.writeCharacteristic(" + lista_char[pozitie_charac]+ ")\n"
    final =  "}, "+ str(time_milis) +")\n\n"
    time_milis = time_milis + increment
    
    de_scris = handler + charac + gatt + final
    
    cod_kotlin.write(de_scris)
    
    
cod_kotlin.close()    
    
    
    
    
#%%
    
    
valori = open("valori_m4_scrise_19_17.txt", "w+")

for i in lista_w:
    valoare = i[1]
    bituri = [valoare[j:j+2] for j in range(0, len(valoare), 2)]
    inturi = [int(bituri[j], 16) for j in range(len(bituri))]
    valori.write("\n ")
    valori.write(str(valoare))
    valori.write(str(inturi))


valori.close()
    
#%%

    
valori = open("valori_m4_primite_19_17.txt", "w+")

for i in lista_primite:
    valoare = i[1]
    bituri = [valoare[j:j+2] for j in range(0, len(valoare), 2)]
    inturi = [int(bituri[j], 16) for j in range(len(bituri))]
    valori.write("\n ")
    valori.write(str(valoare))
    valori.write(str(inturi))


valori.close()
    
    
