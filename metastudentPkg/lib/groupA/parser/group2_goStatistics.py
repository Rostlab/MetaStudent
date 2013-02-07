#!/usr/bin/python

import sys
import math

def main():
    
    progname = sys.argv[0]
    try:
        infileName = sys.argv[1]
    except ValueError:
        sts.exit("usage: %s INFILE" % progname)
        
    # open infile
    infile = open(infileName, 'r')
    
    seqCount = 0
    goCount = 0
    seqWithOneGo = 0
    
    # deriving the needed numbers
    for line in infile:
        if line.startswith('>'):
            seqCount += 1
            gos = line.split(',')
            if len(gos) == 1:
                seqWithOneGo += 1
            goCount += len(gos)
            
    # close infile
    infile.close()
    
    # calculating the statistics
    goPerSeqDiam = float(goCount) / float(seqCount)
    goPerSeqDiam = round(goPerSeqDiam , 3)
    goPerSeqDiamFloored = int(math.floor(goPerSeqDiam))
    
    # output of the statistics
    print("%s protein sequences with GO term found" % seqCount)
    print("%s protein sequences with just one GO found" % seqWithOneGo)
    print("%(goPSDF)s GOs per sequence averagely found (%(goPSD)s)" % {
        'goPSDF': goPerSeqDiamFloored,
        'goPSD': goPerSeqDiam})    



if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        pass
