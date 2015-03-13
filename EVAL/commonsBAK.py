'''
Created on Sep 25, 2011

@author: hampt
'''
import collections
import random

mfRoot = "GO:0003674"
bpRoot = "GO:0008150"



def parseGOAnnotations(goTreeFilePath,closureFilePath,ontology,annotationFilePath=""):
	print "parsing go annotation"
	goConfig = GOConfig()
	goConfig.setTreeFilePath(goTreeFilePath)
	goConfig.setClosureFilePath(closureFilePath)

	geneOntology = GeneOntology(goConfig)

	goAnnotations = None
	if annotationFilePath == "":
		goAnnotations = GOAnnotations("/mnt/project/interres/cafa/datasets/%s/sp_annot_exp.dat" % (ontology), geneOntology)
	else:
		goAnnotations = GOAnnotations(annotationFilePath,geneOntology)
	
	return goAnnotations


class GOConfig:
	def __init__(self):
		self.treeFilePath=""
		self.closureFilePath=""
	
	def setTreeFilePath(self, treeFilePath):
		self.treeFilePath = treeFilePath
	def setClosureFilePath(self, closureFilePath):
		self.closureFilePath = closureFilePath

class GeneOntology:
	def __init__(self, goConfig):
		self.fullClosure = {}
		self.fullGoTree = {}
		self.termToDepth = {}
		self.fullClosureKeys = None
		self.goConfig = goConfig
		self.initFullGoTree()
		self.initClosure()
		self.indexNodeDepths()
		
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
		print "Length: "  + str(len([depth for depth in self.termToDepth.values() if depth==2]))
		self.indexNodeDepthsHelper(bpRoot, 1)
		print "Length: "  + str(len([depth for depth in self.termToDepth.values() if depth==2]))
		
		
		
	def indexNodeDepthsHelper(self, currNode, currLevel):
		self.termToDepth[currNode] = min(self.termToDepth.get(currNode,1000000),currLevel)
		if currNode in self.fullGoTree:
			for child in self.fullGoTree[currNode]:
				self.indexNodeDepthsHelper(child, currLevel+1)


class GOAnnotations:
	def __init__(self, annotationsFilePath, go):
		self.annotationsFilePath = annotationsFilePath
		self.go = go
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
				terms = self.go.getAllParents(self.annotations[target]) | self.annotations[target]
				for term in terms:
					termToCount[term] = termToCount.get(term,0)+1
		for term in termToCount.keys():
			termToCount[term] = termToCount[term] / float(validTargets)
		return termToCount
			
		

	def initAnnotations(self):
		annotationFile = open(self.annotationsFilePath)
		for line in annotationFile.readlines():
			line = line.rstrip()
			target, goString = line.split("\t",1)
			goList = goString.split("\t")
			
			self.annotations[target] = self.go.getAllParents(goList) | set([term for term in goList if term in self.go.fullClosureKeys])
			
			self.annotations[target] = self.annotations[target] - set([mfRoot, bpRoot])
			
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