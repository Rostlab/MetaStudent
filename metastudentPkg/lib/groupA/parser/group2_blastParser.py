#!/usr/bin/env python


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
  
    for line in infile:
        if line.startswith("Results from round 2"):
            for line in infile:
                tmpline = line.strip()
                # serach for GO
                if tmpline.startswith('>GO:'):
                    go = tmpline
                    for line in infile:
                        tmpline = line.strip()
                        if tmpline.startswith("GO:"):
                            go += tmpline
                        else:
                            outfile.write(go + "\n")
                            break
                    
                # search for eValue
                elif tmpline.startswith('Score ='):
                    scoreline = tmpline.split(',',2)
                    expect = scoreline[1].strip().split('=')
                    outfile.write(expect[1].strip() + "\n")
                elif tmpline.startswith("Results from round 3"):
                    # close outfile
                    outfile.close()
                    
                    # close infile
                    infile.close()
                    
                    sys.exit("finished parsing")
    
    # close outfile
    outfile.close()
    
    # close infile
    infile.close()


    
if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        pass
