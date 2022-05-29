import sys



def main(cuvant):
    print(chr(27)+'[2j')
    print('\033c')
    print('\x1bc')
    cheie = cuvant

    cheie = cheie.replace('-', '')
    cheie = [cheie[i:i+2] for i in range(0, len(cheie), 2)]

    print("Dupa spargerea cheii")
    print(cheie)

    print("\n")

    print("var bytes_to_write = byteArrayOf(")

    for i in cheie:
        print(str("0x") + str(i) +str(","))

    print(")")




    print("\n")

    print("var bytes_to_write = byteArrayOf(")

    for i in cheie:
        print(str("0x") + str(i) +str(".toUByte(),"))

    print(")")




    cheie = [int(i, 16) for i in cheie]
    print("Dupa trecerea in int:")
    print(cheie)

    print("\n")

    print("var bytes_to_write = byteArrayOf(")

    for i in cheie:
        print(str(i) + ".toByte(),")

    print(")")

if __name__ == "__main__":
    main(sys.argv[1])
