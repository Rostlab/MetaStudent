'''
Created on Sep 25, 2011

@author: hampt
'''
import collections
import random

mfRoot = "GO:0003674"
bpRoot = "GO:0008150"



def parseECAnnotations(annotationFilePath=""):
	print "parsing ec annotation"

	ecAnnotations = ECAnnotations(annotationFilePath)
	
	return ecAnnotations




class ECAnnotations:
	def __init__(self, annotationsFilePath):
		self.annotationsFilePath = annotationsFilePath
		self.annotations={}
		self.allTerms = set([])
		self.termToProbability = {}
		
		self.initAnnotations()
		self.initTermProbabilities()
		
	def initTermProbabilities(self):
		for target, goTermSet in self.annotations.iteritems():
			if len(goTermSet) > 0:
				for term in goTermSet:
					self.termToProbability[term] =  self.termToProbability.setdefault(term,0)+1
		maxCount = max(self.termToProbability.values())
		for term in self.termToProbability.keys():
			self.termToProbability[term] = float(self.termToProbability[term]) / maxCount
		
	def getCustomTermProbabilities(self, targets):
		termToCount={}
		validTargets=0
		for target in targets:
			if target in self.annotations:
				validTargets+=1
				terms = self.annotations[target]
				for term in terms:
					termToCount[term] = termToCount.get(term,0)+1
		for term in termToCount.keys():
			termToCount[term] = termToCount[term] / float(validTargets)
		return termToCount
			
		

	def initAnnotations(self):
		annotationFile = open(self.annotationsFilePath)
		for line in annotationFile.readlines():
			line = line.rstrip()
			target, ecString = line.split("\t",1)
			ecList = set(ecString.split("\t"))

			self.annotations[target] = ecList
			
			if len(self.annotations[target]) == 0:
				print "No more terms for " + target + " after filtering"
			self.allTerms.update(self.annotations[target])
		print "Found " + str(len(self.allTerms)) + " terms"
		
class GOPrediction:
	def __init__(self, predictionFilePath, GOAnnotations, numberOfTargets):
		self.numberOfTargets = numberOfTargets
		self.goAnnotations = GOAnnotations
		self.predictionFilePath = predictionFilePath
		self.targetToTermToScore = {}
		self.initPredictions()
		
	
	def initPredictions(self):
		print self.predictionFilePath
		cafaFile = open(self.predictionFilePath)
		currTarget = ""
		currTermToScore = {}
		i=0
		for line in cafaFile:
			if len(line.split("\t")) == 3:
				target, term, scoreString=line.rstrip().split("\t")
				
				score = max(0.0,min(1.0,float(scoreString)))
				if target != currTarget:
#					print i
					#make pred
					if currTarget != "":
						#print currTermToScore
						#currTermToScore = self.makeCompletePrediction(currTermToScore)
						if len(currTermToScore) > 0:# and len(targetAnnotations[currTarget]) > 0:
							self.targetToTermToScore[currTarget] = currTermToScore
	#						evalInput.append((currTarget, currTermToScore, i))
	#						result = simpleMeasure(currTarget, currTermToScore)
	#						results.append(result)
							i+=1
	#						if i == 1000: break
						
					currTarget = target
					currTermToScore = {}
	
	#			print term
				#if term in self.goAnnotations.allTerms:
				if term != bpRoot and term != mfRoot:
					currTermToScore[term] = score
				#else:
				#	print "dropped " + term

		cafaFile.close()
		#currTermToScore = self.makeCompletePrediction(currTermToScore)
		if len(currTermToScore) > 0:# and len(targetAnnotations[currTarget]) > 0:
			self.targetToTermToScore[currTarget] = currTermToScore
			
		print str(len(self.targetToTermToScore.keys())) + " targets predicted"

	def getOutputFileContent(self):
		lines = []
		for target, termToScoreLocal in self.targetToTermToScore.iteritems():
			for term, score in termToScoreLocal.iteritems():
				if float(score) > 1.0:
					score = "1.0"
				elif float(score) < 0.0:
					score = "0.0"
				else:
					score = str(score)
				lines.append("%s\t%s\t%s\n" % (target, term, score))
		return "".join(lines)

	def propagatePrediction(self):
		for target in self.targetToTermToScore.keys():
			self.targetToTermToScore[target] = self.makeCompletePrediction(self.targetToTermToScore[target])
		return self

	def makeCompletePrediction(self,termsToScores):
		termsToScoresLocal = dict([(key,val) for (key,val) in termsToScores.iteritems() if key in self.goAnnotations.go.fullClosureKeys])
		
		allNodes = frozenset(self.goAnnotations.go.getAllParents(termsToScoresLocal.keys()) | set(termsToScoresLocal.keys()) )
	
	#	leaves = getLeaveTerms(allNodes)
	#	parents = allNodes - leaves
		
		localTree = {}
		for term in allNodes:
			if term in self.goAnnotations.go.fullGoTree.keys():
				allChildren = self.goAnnotations.go.fullGoTree[term].intersection(allNodes)
				if len(allChildren) > 0:
					localTree[term] = allChildren
	

		if mfRoot in allNodes:
			self.makeCompletePredictionHelper(localTree, mfRoot, termsToScoresLocal)
		if bpRoot in allNodes:
			self.makeCompletePredictionHelper(localTree, bpRoot, termsToScoresLocal)
		
		if bpRoot in termsToScoresLocal.keys():
			del termsToScoresLocal[bpRoot]
		if mfRoot in termsToScoresLocal.keys():
			del termsToScoresLocal[mfRoot]
			
		return termsToScoresLocal
	
	def makeCompletePredictionHelper(self,localTree, currNode, termsToScores):
		maxScore = 0.0
		if currNode in localTree.keys():
			for child in localTree[currNode]:
				currScore = self.makeCompletePredictionHelper(localTree, child, termsToScores)
				if currScore > maxScore:
					maxScore =  currScore
		
		if currNode not in termsToScores:
			termsToScores[currNode] = maxScore
		
		return termsToScores[currNode]