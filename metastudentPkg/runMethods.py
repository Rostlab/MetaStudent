'''
Created on Mar 17, 2012

@author: hampt
'''

import os
import commands
from Logger import Logger
from BlastWrapper import blastallParameters, createBlastPGPCommand
import sys

silent=False

def setSilent(val):
	global silent
	silent=val

def isSilent():
	global silent
	return silent

def p(string):
	if not silent:
		print string

def getPkgPath():
	myPath=""
	if hasattr(sys, "frozen"):
		myPath = os.path.dirname(unicode(sys.executable, sys.getfilesystemencoding( )))
	else:
		myPath = os.path.dirname(os.path.abspath(__file__))
	return myPath

#preserves order of sequences
def splitBigFastaFile(fastaFilePath, tmpDir, splitSize): 
	
	fastaSplitFolder = os.path.join(tmpDir, "fasta_splits")
	if not os.path.exists(fastaSplitFolder):
		os.mkdir(fastaSplitFolder)
	
	fastaFile = open(fastaFilePath)
	fileContent = fastaFile.read().rstrip("\n")
	fastaFile.close()
	if fileContent == "":
		Logger.log("Error: empty fasta file")
		raise
	
	fastaFilePaths=[]
	lines = fileContent.split("\n")
	i=0
	numEntries=0
	fileCount=0
	currFileLines=[]
	
	while True:
		currHeader=""
		while(currHeader=="") and i<len(lines):
			if lines[i].startswith(">"):
				currHeader=lines[i]
			i+=1	

		currSequence = ""
		while i<len(lines) and not lines[i].startswith(">"):
			if lines[i][0].isalpha():
				currSequence += lines[i].replace(" ","").replace("\t","")
			i+=1
		currSequence.replace("\n","")
		
		currFileLines.append(currHeader+"\n")
		currFileLines.append(currSequence + "\n")
	
		numEntries += 1
		if numEntries % splitSize == 0 or i == len(lines):
			newFastaFilePath = os.path.join(fastaSplitFolder,"split_%d.fasta" % (fileCount))
			fastaFilePaths.append(newFastaFilePath)
			currFile = open(newFastaFilePath, 'w')
			currFile.write("".join(currFileLines))
			currFileLines = []
			currFile.close()
			fileCount += 1
			
		if i == len(lines):
			break
	
	return fastaFilePaths


def runBlast(inputFilePath, blastDatabasePath, outputFilePath, tmpDir, eValue, iters, configMap):
	
	fastaSplits = splitBigFastaFile(inputFilePath, tmpDir, configMap["FASTA_SPLIT_SIZE"])

	for fastaSplit in fastaSplits:
		blastParas = blastallParameters()
		blastParas.setBlastExePath(configMap["BLASTPGP_EXE_PATH"])
		blastParas.setBlastDatabasePath(blastDatabasePath)
		blastParas.setEValue(eValue)
		blastParas.setJ(iters)
		blastParas.setInputFilePath(fastaSplit)
		blastParas.setOutputFilePath(fastaSplit+".blast")
		
		blastCommand = createBlastPGPCommand(blastParas)
		
		s, o = commands.getstatusoutput(blastCommand)
		if s != 0:
			Logger.log("!!!Error!!! " + blastCommand)
			Logger.log(s)
			Logger.log(o)
			raise Exception
		
	p("Merging Blast Output")
#	allRoundSplits = ["" for bla in range(int(NUMBER_OF_ITERATIONS))]
	bigOutputFile = open(outputFilePath,'w')
	for fastaSplit in fastaSplits:
		blastFile = open(fastaSplit+".blast")
		blastOutput = "\n" + blastFile.read()
		blastFile.close()
		bigOutputFile.write(blastOutput)
	bigOutputFile.close()


def runMethodA(blastOutputFilePath, fastaFilePath, tmpDir, GROUP_A_THRESHOLD, GROUP_A_K, iters, onto, configMap):#
	p("Running Method A")
	
	currCwd = os.getcwd()
	
	tmpDirPath = os.path.join(tmpDir, "methodA")
	outputFilePath = os.path.join(tmpDirPath, "output.%s.cafa.txt" % (onto))
	commandsi = ["mkdir -p " + tmpDirPath, 
	   			"cd " + configMap["GROUP_A_PATH"],
				"java -jar GOSSIP.jar %s %s %s %s %s %s" % (fastaFilePath, blastOutputFilePath, outputFilePath, GROUP_A_THRESHOLD, GROUP_A_K, iters)]
	commandString = ";".join(commandsi)
	#print commandString

	os.chdir(currCwd)

	logFile=None
	logPath=os.path.join(tmpDir, "logA.txt")
	if not os.path.exists(logPath):
		logFile = open(logPath, 'w')
	else:
		logFile = open(logPath, 'a')
	
	s, o = commands.getstatusoutput(commandString)
	if True:#s != 0:
		logFile.write("Command: " + commandString + "\n")
		logFile.write(str(s)+"\n")
		logFile.write(o+"\n")
	if s != 0:
		print >> sys.stderr,"!!!Error!!! " + commandString
		print >> sys.stderr,str(s)
		print >> sys.stderr,o
		#raise
	logFile.close()
	
	outputFilePath = outputFilePath+".cafa"
	outputFile = open(outputFilePath)
	preds =  set([])
	predFilesContent=[]
	for line in outputFile:
		if line.startswith("AUTHOR") or line.startswith("MODEL") or line.startswith("ACCURACY") or line.startswith("KEYWORDS") or line.startswith("END"):
			None
		else:
			targetId, goTerm, rel = line.rstrip().split("\t")
			targetId = targetId[1:].split("(")[0]
		
			relFloat = max(min(float(rel), 1.00), 0.00)
			rel = "%.2f" % (relFloat)

			line = targetId + "\t" + goTerm + "\t" + rel

			if targetId + "\t" + goTerm not in preds and goTerm.strip() != "":
				predFilesContent.append(line)
				preds.add(targetId + "\t" + goTerm)

	return predFilesContent

def runMethodB(blastOutputFilePath, fastaFilePath, tmpDir, GROUP_B_K, onto, configMap):#
	p("Running Method B")
	
	currCwd = os.getcwd()
	
	tmpDirPath = os.path.join(tmpDir, "methodB")
	if not os.path.exists(tmpDirPath):
		os.mkdir(tmpDirPath)
	outputFilePath = os.path.join(tmpDirPath, onto)
	os.mkdir(outputFilePath)
	commandsi = ["mkdir -p " + tmpDirPath, 
	   			"cd " + configMap["GROUP_B_PATH"],
				"perl knn_weighted -m weighted_knn -j %s -d %s -i %s -o %s -k %s" % (blastOutputFilePath, blastOutputFilePath, fastaFilePath, outputFilePath, GROUP_B_K)]
	commandString = ";".join(commandsi)
	#print commandString
	os.chdir(currCwd)

	logFile=None
	logPath=os.path.join(tmpDir, "logB.txt")
	if not os.path.exists(logPath):
		logFile = open(logPath, 'w')
	else:
		logFile = open(logPath, 'a')
	
	s, o = commands.getstatusoutput(commandString)
	if True:#s != 0:
		logFile.write("Command: " + commandString + "\n")
		logFile.write(str(s) + "\n")
		logFile.write(o+ "\n")
	if s != 0:
		print >> sys.stderr,"!!!Error!!! " + commandString
		print >> sys.stderr,str(s)
		print >> sys.stderr,o
		#raise
	logFile.close()
	
	outputFilePath = os.path.join(outputFilePath, os.path.basename(fastaFilePath)+".weighted_knn.predicted_leaves")

	idToGoTermCount = {}
	predFilesContent=[]
	with open(outputFilePath) as f: 
		for line in f.readlines():
			if line.startswith("AUTHOR") or line.startswith("MODEL") or line.startswith("ACCURACY") or line.startswith("KEYWORDS") or line.startswith("END"):
				None
			else:
				currId = line.split("\t")[0]
				if idToGoTermCount.get(currId, 0) < 1000: 
					if float(line.split("\t")[2]) > 1.0:
						line = line.rstrip("\n").replace("1.01", "1.00")
					idToGoTermCount[currId] = idToGoTermCount.get(currId, 0) + 1
					predFilesContent.append(line)
	return predFilesContent

def runMethodC(blastOutputFilePath, fastaFilePath, tmpDir, scoring, onto, configMap):#
	p("Running Method C")
	
	currCwd = os.getcwd()
	
	tmpDirPath = os.path.join(tmpDir, "methodC")
	outputFilePath = os.path.join(tmpDirPath, "output.%s.txt" % (onto))
	commandsi = ["mkdir -p " + tmpDirPath, 
	   			"cd " + configMap["GROUP_C_PATH"],
				"perl CafaWrapper3.pl %s %s %s %s" % (blastOutputFilePath, outputFilePath, scoring, tmpDirPath)]
	commandString = ";".join(commandsi)
	#print commandString
	os.chdir(currCwd)

	logFile=None
	logPath=os.path.join(tmpDir, "logC.txt")
	if not os.path.exists(logPath):
		logFile = open(logPath, 'w')
	else:
		logFile = open(logPath, 'a')
	
	s, o = commands.getstatusoutput(commandString)
	if True:#s != 0:
		logFile.write("Command: " + commandString)
		logFile.write(str(s))
		logFile.write(o)
	if s != 0:
		print >> sys.stderr, "!!!Error!!! " + commandString
		print >> sys.stderr,str(s)
		print >> sys.stderr,o
		#raise
	logFile.close()

	predFilesContent = []
	with open(outputFilePath) as f: 						
		preds =  set([])
		for line in f.readlines():
			if line.rstrip() == "" or line.startswith("AUTHOR") or line.startswith("MODEL") or line.startswith("ACCURACY") or line.startswith("KEYWORDS") or line.startswith("END"):
				None
			else:
				targetId, goTerm, rel = line.rstrip().split(" ")						
				relFloat = max(min(float(rel), 1.00), 0.00)
				rel = "%.2f" % (relFloat)					
				line = targetId + "\t" + goTerm + "\t" + rel				
				if targetId + "\t" + goTerm not in preds and goTerm.strip() != "" and float(rel) > 0.0:
					predFilesContent.append(line)
					preds.add(targetId + "\t" + goTerm)

	return predFilesContent
