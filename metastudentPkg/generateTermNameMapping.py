import sys


def convertGotermtxtToNameMapping(gotermtxtFilePath, nameMappingFilePath):
	
	nameMappingFile = open(nameMappingFilePath,"w")
	
	gotermtxtFile = open(gotermtxtFilePath)
	for line in gotermtxtFile:
		nr, namei, onto, termId = line.split("\t")[:4]
		nameMappingFile.write(termId + "\t" + namei + "\n")
	
	gotermtxtFile.close()
	nameMappingFile.close()

#if __name__ == "__main__":
#	convertGotermtxtToNameMapping(sys.argv[1], sys.argv[2])