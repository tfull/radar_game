import random

def main():
    print "random"
    print "OK"
    l = list(range(100))
    random.shuffle(l)
    for i in range(100):
        print (chr(ord('A') + l[i] / 10) + str(l[i] % 10))

main()

