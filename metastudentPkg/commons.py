'''
Created on Sep 25, 2011

@author: hampt
'''
import collections
import random
import os
import sys
import shlex
import time
from multiprocessing.pool import Pool

mfRoot = "GO:0003674"
bpRoot = "GO:0008150"
ccRoot = "GO:0005575"

whiteSpaceCode = "~"

silent=False
go=None

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


def encodeFastaHeaders(fastaFilePath):
	try:
		fastaFile = open(fastaFilePath)
		fileContent = fastaFile.read().rstrip("\n")
		fastaFile.close()
		if fileContent == "":
			p("Error: empty fasta file")
			raise
		
		lines = fileContent.split("\n")
		newLines = []
		i=0
	
		for line in lines:
			if line.startswith(">"):
				newLines.append(line.replace(" ", whiteSpaceCode).replace("\t", whiteSpaceCode))
			else:
				newLines.append(line)
		
		fastaFile = open(fastaFilePath,'w')
		fastaFile.write("\n".join(newLines))
		fastaFile.close()
	except (IOError, os.error), why:
		Logger.log("Error reading/writing fasta file " + str(why))
	
	
def decodeFastaHeader(fastaHeader):
	return fastaHeader.replace(whiteSpaceCode, " ")



def makeCompletePrediction(termsToScores):
	
	global go
	

	
	termsToScoresLocal = dict([(key,val) for (key,val) in termsToScores.iteritems() if key in go.fullClosureKeys])
	

	
	allNodes = frozenset(go.getAllParents(termsToScoresLocal.keys()) | set(termsToScoresLocal.keys()) )



	localTree = {}
	for term in allNodes:
		if term in go.fullGoTree:
			allChildren = go.fullGoTree[term].intersection(allNodes)
			if len(allChildren) > 0:
				localTree[term] = allChildren



	if mfRoot in allNodes:
		makeCompletePredictionHelper(localTree, mfRoot, termsToScoresLocal)
	if bpRoot in allNodes:
		makeCompletePredictionHelper(localTree, bpRoot, termsToScoresLocal)
	if ccRoot in allNodes:
		makeCompletePredictionHelper(localTree, ccRoot, termsToScoresLocal)
	

	
	if bpRoot in termsToScoresLocal.keys():
		del termsToScoresLocal[bpRoot]
	if mfRoot in termsToScoresLocal.keys():
		del termsToScoresLocal[mfRoot]
	if ccRoot in termsToScoresLocal.keys():
		del termsToScoresLocal[ccRoot]
		
	return termsToScoresLocal

def makeCompletePredictionHelper(localTree, currNode, termsToScores):
	maxScore = 0.0
	if currNode in localTree:
		for child in localTree[currNode]:
			currScore = makeCompletePredictionHelper(localTree, child, termsToScores)
			if currScore > maxScore:
				maxScore =  currScore
	
	if currNode not in termsToScores:
		termsToScores[currNode] = maxScore
	
	return termsToScores[currNode]




class GOConfig:
	def __init__(self):
		self.treeFilePath=""
		self.closureFilePath=""
		self.nameMappingFilePath=""
	
	def setTreeFilePath(self, treeFilePath):
		self.treeFilePath = treeFilePath
	def setClosureFilePath(self, closureFilePath):
		self.closureFilePath = closureFilePath
	def setNameMappingFilePath(self, nameMappingFilePath):
		self.nameMappingFilePath = nameMappingFilePath

class GeneOntology:
	def __init__(self, goConfig):
		self.fullClosure = {}
		self.fullGoTree = {}
		self.termToDepth = {}
		self.termToName={}
		self.fullClosureKeys = None
		self.goConfig = goConfig
		self.initFullGoTree()
		self.initClosure()
		#self.indexNodeDepths()
		
		if os.path.exists(self.goConfig.nameMappingFilePath):
			self.initNames()
		
	def initNames(self):		
		nameMappingFile = open(self.goConfig.nameMappingFilePath)
		nameMapping = {}
		for line in nameMappingFile:
			#print line.rstrip("\n").split()
			goTermId, goTermName = line.rstrip("\n").split("\t")
			self.termToName[goTermId] = goTermName

		nameMappingFile.close()



	def initFullGoTree(self):
		treeFile = open(self.goConfig.treeFilePath)
		for line in treeFile.readlines():
			go1, go2 = line.split("\t")[0:2]
			self.fullGoTree.setdefault(go1, set([])).add(go2)
		treeFile.close()
	
	def initClosure(self):
		allValues = set([])
		closureFile = open(self.goConfig.closureFilePath)
		for line in closureFile.readlines():
			go1, bla, go2 = line.split("\t")[0:3]
			self.fullClosure.setdefault(go1, set([])).add(go2)
			allValues.add(go1)	
		closureFile.close()
		
		for root in allValues - set(self.fullClosure.keys()):
			self.fullClosure[root] = set([])
		
		self.fullClosureKeys = frozenset(self.fullClosure.keys())

	def getAllParents(self, terms):
		returnTerms = set([])
		for term in terms:
			if term in self.fullClosureKeys:
				returnTerms |= self.fullClosure[term]
			else:
				None
	#			print "dropped: " + term
		return returnTerms
	
	def getLeaveTerms(self, terms):
		allTerms = [self.fullClosure[term] for term in terms if term in self.fullClosureKeys]
		leaves = set([])
		if len(allTerms) > 0:
			parents = reduce(set.union, allTerms)
			allTerms = parents | set(terms)
			leaves = allTerms - parents
		return leaves
	
	def indexNodeDepths(self):
		self.indexNodeDepthsHelper(mfRoot, 1)
		#print "Length: "  + str(len([depth for depth in self.termToDepth.values() if depth==2]))
		self.indexNodeDepthsHelper(bpRoot, 1)
		#print "Length: "  + str(len([depth for depth in self.termToDepth.values() if depth==2]))
		self.indexNodeDepthsHelper(ccRoot, 1)
		#print "Length: "  + str(len([depth for depth in self.termToDepth.values() if depth==2]))
		
		
	def indexNodeDepthsHelper(self, currNode, currLevel):
		self.termToDepth[currNode] = min(self.termToDepth.get(currNode,1000000),currLevel)
		if currNode in self.fullGoTree:
			for child in self.fullGoTree[currNode]:
				self.indexNodeDepthsHelper(child, currLevel+1)


class GOPrediction:
	def __init__(self, predLines, go):
		self.go = go
		self.targetToTermToScore = collections.defaultdict(dict)
		self.predlines=predLines
		self.targetsSorted = []
		self.initPredictions()
		
	
	def initPredictions(self):
		currTarget = ""
		currTermToScore = {}
		i=0
		for line in self.predlines:
			if len(line.split("\t")) == 3:
				target, term, scoreString=line.rstrip().split("\t")
				
				score = max(0.0,min(1.0,float(scoreString)))
				if target != currTarget:

					if currTarget != "":

						if len(currTermToScore) > 0:
							self.targetToTermToScore[currTarget] = currTermToScore
							if currTarget not in self.targetsSorted:
								self.targetsSorted.append(currTarget)

							i+=1

						
					currTarget = target
					currTermToScore = {}
	

				if term != bpRoot and term != mfRoot and term != ccRoot:
					currTermToScore[term] = score

		if len(currTermToScore) > 0:
			self.targetToTermToScore[currTarget] = currTermToScore
			if currTarget not in self.targetsSorted:
				self.targetsSorted.append(currTarget)

	def toOutputLines(self):
		lines = []
		for targetId in self.targetsSorted:
			for term, scorei in sorted(self.targetToTermToScore[targetId].items(), key=lambda x: x[1], reverse=True):
				lines.append("%s\t%s\t%.2f\n" % (decodeFastaHeader(targetId), term, scorei) )
		return lines


	def toOutputLinesWithNames(self):
		lines = []
		for targetId in self.targetsSorted:
			for term, scorei in sorted(self.targetToTermToScore[targetId].items(), key=lambda x: x[1], reverse=True):
				lines.append("%s\t%s\t%.2f\t%s\n" % (decodeFastaHeader(targetId), term, scorei, self.go.termToName.get(term, "n/a") ))
		return lines


	def propagatePrediction(self):
		
		sortedTargets = sorted(self.targetToTermToScore.keys())
		
		inputs = [self.targetToTermToScore[targeti] for targeti in sortedTargets]

		global go
		go=self.go
		
		p = Pool(processes=10)
		results= p.map(makeCompletePrediction, inputs, chunksize=20)
		p.close()
		p.join()
		
		for i, result in enumerate(results):
			self.targetToTermToScore[sortedTargets[i]] = result
		
		return self


