#!/usr/bin/python


import sys

def main():
    "the main function"
    progname = sys.argv[0]
    try:
        infileName, outfileName = sys.argv[1:]
    except ValueError:
        sys.exit( "usage: %s INFILE OUTFILE" % progname)
        
    # open infile
    infile = open(infileName, 'r')
    
    # open outfile
    outfile = open(outfileName, 'w')
  
    sqCounter = 0
    go = ""
    sq = ""
    for line in infile:
        # serach for GO
        if line.startswith('DR   GO'):
            tmpline = line.split(';',2)
            if go == '':
                go = '>' + tmpline[1].lstrip()[4:]
            else:
                go = go + ',' + tmpline[1].lstrip()[4:]
        # search for SQ
        elif line.startswith('SQ'):
            for line in infile:
                if line.startswith('//'):
                    # if GO and SQ has been found in one block (separated by '//') the GOs and SQ is written into the outfile
                    if go != '':
                        outfile.write(go+'\n'+sq+'\n')
                        sqCounter+=1
                        if (sqCounter%50000 == 0 and sqCounter!=0):
                            print("%(sqCounter)d Sequences written into %(outfile)s" % {
                                'sqCounter': sqCounter, 
                                'outfile': outfileName})
                    go = ''
                    sq = ''
                    break
                else:    
                    sq = sq + line.lstrip()       
    print("%(sqCounter)d Sequences written into %(outfile)s\nfinished" % {
        'sqCounter': sqCounter, 
        'outfile': outfileName})
    # close outfile
    outfile.close()
    
    # close infile
    infile.close()
    
if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        pass
