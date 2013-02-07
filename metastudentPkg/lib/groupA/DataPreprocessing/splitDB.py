#!/usr/bin/python

import sys

def main():
    
    progname = sys.argv[0]
    
    try:
        infileName = sys.argv[1]
    except ValueError:
        sys.exit("usage: %s INFILE" % progname)
    
    # open infile
    infile = open(infileName,'r')
    
    # open outfiles
    training = open("training.f", 'w')
    testing = open("testing.f", 'w')
    holdout = open("holdout.f", 'w')
    
    # read the file linewise, and store entries into files
    
    entrycount = 0
#    trainEntr = 0
#    testEntr = 0
#    holdEntr = 0
    
    buffer = ""
    for line in infile:
        if line.startswith('>'):
            buffer = ""
            buffer += line
            entrycount += 1
        else:
            buffer += line
            n = entrycount%3
            if n==0:
                training.write(buffer)
#                trainEntr += 1
            elif n==1:
                testing.write(buffer)
#                testEntr += 1
            elif n==2:
                holdout.write(buffer)
#                holdEntr += 1
        
    # close outfiles
    training.close()
    testing.close()
    holdout.close()
    
    #close infile
    infile.close()
    
#    print ("training: %(tr)s, testing: %(te)s, holdout: %(ho)s" % {
#        'tr': trainEntr,
#        'te': testEntr,
#        'ho': holdEntr })
    
    
if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        pass
