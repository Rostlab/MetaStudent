'''
Created on Jan 28, 2011

@author: hampt
'''
import math
from multiprocessing.pool import Pool
import copy
import collections
import sys
import random

fullClosure = {}
fullGoTree = {}
targetAnnotations = {}
fullClosureKeys = None

def logit(triple):
	linCom = 4.0716*triple[0] + 4.3419*triple[1] + 4.6231*triple[2] - 4.2599
	return 1.0 / (1.0 + math.exp(-linCom)) 

def lin(triple):
	return 0.2899*triple[0] + 0.3762*triple[1] + 0.6102*triple[2] - 0.0101

def initFullGoTree(treeFilePath):
	treeFile = open(treeFilePath)
	for line in treeFile.readlines():
		go1, go2 = line.split("\t")[0:2]
		fullGoTree.setdefault(go1, set([])).add(go2)
	treeFile.close()

def initClosure(closureFilePath):
	allValues = set([])
	closureFile = open(closureFilePath)
	for line in closureFile.readlines():
		go1, bla, go2 = line.split("\t")[0:3]
		fullClosure.setdefault(go2, set([])).add(go1)
		allValues.add(go1)	
	closureFile.close()
	
	for root in allValues - set(fullClosure.keys()):
		fullClosure[root] = set([])
	
	global fullClosureKeys
	fullClosureKeys = frozenset(fullClosure.keys())

def initAnnotations(annotationFilePath):
	annotationFile = open(annotationFilePath)
	for line in annotationFile.readlines():
		line = line.rstrip()
		print line.split("\t")
		target, goString = line.split("\t")
		goList = goString.split(",")

		targetAnnotations[target] = getAllParents(goList) | set([term for term in goList if term in fullClosureKeys])

#		if target == "T01022":
#			print goList
#			print getAllParents(goList)
#			print set([term for term in goList if term in fullClosureKeys])
#			print targetAnnotations[target]

def propagateClosure(termsToScores):
	returnClosure = {}
	for term in termsToScores.keys():
		returnClosure[term] = fullClosure[term]
		
	return returnClosure

def getAllParents(terms):
	returnTerms = set([])
	for term in terms:
		if term in fullClosureKeys:
			returnTerms |= fullClosure[term]
		else:
			None
#			print "dropped: " + term
	return returnTerms


def getLeaveTerms(terms):
	
	parents = reduce(set.union, [fullClosure[term] for term in terms if term in fullClosureKeys])
	allTerms = parents | set(terms)
	leaves = allTerms - parents
	return leaves
			
def getKBest(termsToScores, k):
	scoreToTerms = {}
	for term, score in termsToScores.iteritems():
		scoreToTerms.setdefault(score, set([])).add(term)
	
	returnMap = {}
	if len(scoreToTerms.keys()) >= k:	
		terms = set([])
		for score in sorted(scoreToTerms.keys(), reverse=True)[0:k]:
			terms |= scoreToTerms[score]
		
		returnMap = dict([(term, termsToScores[term]) for term in terms])
	
#	print returnMap
	return returnMap

def makeCompletePrediction(termsToScores):
	termsToScoresLocal = dict([(key,val) for (key,val) in termsToScores.iteritems() if key in fullClosureKeys])
	
	allNodes = frozenset(getAllParents(termsToScoresLocal.keys()) | set(termsToScoresLocal.keys()) )

#	leaves = getLeaveTerms(allNodes)
#	parents = allNodes - leaves
	
	localTree = {}
	for term in allNodes:
		if term in fullGoTree.keys():
			allChildren = fullGoTree[term].intersection(allNodes)
			if len(allChildren) > 0:
				localTree[term] = allChildren

	mfRoot = "GO:0003674"
	bpRoot = "GO:0008150"
	if mfRoot in allNodes:
		makeCompletePredictionHelper(localTree, mfRoot, termsToScoresLocal)
	if bpRoot in allNodes:
		makeCompletePredictionHelper(localTree, bpRoot, termsToScoresLocal)
	
	return termsToScoresLocal

def makeCompletePredictionHelper(localTree, currNode, termsToScores):
	maxScore = 0.0
	if currNode in localTree.keys():
		for child in localTree[currNode]:
			currScore = makeCompletePredictionHelper(localTree, child, termsToScores)
			if currScore > maxScore:
				maxScore =  currScore
	
	if currNode not in termsToScores:
		termsToScores[currNode] = maxScore
	
	return termsToScores[currNode]
	
def calcPrecRecall(target, terms):
#	print "Target: " + target
#	print "Terms: " + str(terms)

	refSet = targetAnnotations[target]
	
#	print "Refs: " + str(refSet)
	
	TPs = len(refSet.intersection(terms))
	FPs = len(terms - refSet)
	FNs = len(refSet - terms)
	
	recall = TPs / float(TPs + FNs)
	precision = TPs / float(TPs + FPs)

	print str(precision) + "\t" + "FPs: " + str(terms - refSet) + "\t" + "TPs: " + str(refSet.intersection(terms))
	
	return recall, precision
	
def simpleMeasure((target, termToScore, progress)):
	print progress
	
	result = []
	
	
#	print termToScore
	for i in range(1):
#		print i
		termToScoreKBest =  getKBest(termToScore, i+1)
		termToScoreKBest = makeCompletePrediction(termToScoreKBest)
		if len(termToScoreKBest) > 0:
			result.append((calcPrecRecall(target, set(termToScoreKBest.keys()))))
		else:
			break
#	print result

	return result



def leafMeasure((target, termToScore, progress)):
#	print str(progress) + " " + target + " " + str(termToScore)
	
	result = []
	
	leaves = getLeaveTerms(termToScore.keys())
	scoreToLeaves = {}
	for leaf in leaves:
		scoreToLeaves.setdefault(termToScore[leaf], []).append(leaf)
	

	propagatedLeaves = set([])
	for score in sorted(scoreToLeaves.keys(), reverse=True):
		propagatedLeaves |= (getAllParents(scoreToLeaves[score]) | set(scoreToLeaves[score]))
		result.append((calcPrecRecall(target, propagatedLeaves)))
	
#	print "Leaves: " + str(leaves)
#	print "Prop: " + str(propagatedLeaves)
	
#	print result

	return result
	
def brutal((target, termToScore, progress)):
#	print target
#	print termToScore
#	print targetAnnotations[target]
	targetLeaves = getLeaveTerms(targetAnnotations[target])
#	print targetLeaves
	leafPropagations = []
	for leaf in targetLeaves:
		leafProp = getAllParents([leaf])
		leafProp.add(leaf)
		leafPropagations.append(leafProp)
	
	uniqueLeafProps = []
	for leafProp in leafPropagations:
		uniqueLeafProp = set([])
		for term in leafProp:
			unique = True
			for otherLeafProp in leafPropagations:
				if otherLeafProp!=leafProp:
					if term in otherLeafProp:
						unique = False
						break
			if unique:
				uniqueLeafProp.add(term)
		uniqueLeafProps.append(uniqueLeafProp)
	
#	print uniqueLeafProps
	
	
	predictedLeaves = getLeaveTerms(termToScore.keys())
	
#	print predictedLeaves
	
	functionHitCounter = 0
	for uniqueLeafProp in uniqueLeafProps:
		if len(predictedLeaves.intersection(uniqueLeafProp)) > 0:
			functionHitCounter += 1
#	print functionHitCounter
#	print functionHitCounter / float(len(predictedLeaves))
	return (functionHitCounter / float(len(targetLeaves)), functionHitCounter / float(len(predictedLeaves)))
	
def ultra((target, termToScore, progress)):
	targetLeaves = getLeaveTerms(targetAnnotations[target])
	predictedLeaves = getLeaveTerms(termToScore.keys())
	
	leavesMissed = targetLeaves - predictedLeaves
	leavesSuperfl = predictedLeaves - targetLeaves
	
	return (1-len(leavesMissed) / float(len(targetLeaves)), 1-len(leavesSuperfl) / float(len(predictedLeaves)))
	
def ultraCurve((target, termToScore, progress)):
	print progress
	
	result = []
	targetLeaves = getLeaveTerms(targetAnnotations[target])
	predictedLeaves = getLeaveTerms(termToScore.keys())
	
	precAll = None
	precLeaf = None
#	leavesMissed = targetLeaves - predictedLeaves
#	leavesSuperfl = predictedLeaves - targetLeaves
#	rec, prec = (1-len(leavesMissed) / float(len(targetLeaves)), 1-len(leavesSuperfl) / float(len(predictedLeaves)))
#	print (rec, prec)
	
	for i in range(1):
#		print i
		termToScoreKBest =  getKBest(termToScore, i+1)
		termToScoreKBest = makeCompletePrediction(termToScoreKBest)
		if len(termToScoreKBest) > 0:
			rec, precAll = calcPrecRecall(target, set(termToScoreKBest.keys()))
		else:
			break
	
	termToScoreLeafs = dict([(key,val) for key,val in termToScore.iteritems() if key in predictedLeaves])
	
#	print termToScore
	for i in range(1):
#		print i
		predictedLeavesSub =  set(getKBest(termToScoreLeafs, i+1).keys())
		if len(predictedLeavesSub) > 0:
			leavesMissed = targetLeaves - predictedLeavesSub
			leavesSuperfl = predictedLeavesSub - targetLeaves
			rec, precLeaf = (1-len(leavesMissed) / float(len(targetLeaves)), 1-len(leavesSuperfl) / float(len(predictedLeavesSub)))
			print str(precLeaf) + "\tleaves: " + str(predictedLeavesSub)
			result.append((rec, precLeaf))
		else:
			break
	
	
	if (precAll == None or precLeaf == None) and (precAll != precLeaf):
		print "ERRROR"
	else:
		if precAll < precLeaf:
			print "!!!!!!!!!!!!!"
		
#	print result

	return result
	
def fillData((cafaFilePath)):
	cafaFile = open(cafaFilePath)
	methodToTargetToTermToScore = {}
	currTarget = ""
	currTermToScore = {}
	i=0
	for line in cafaFile:
		if line[0] == "T":
			target, term, scoreString=line.rstrip().split("\t")
			score = max(0.0,min(1.0,float(scoreString)))
			if target != currTarget:
				print i
				#make pred
				if currTarget != "" and random.random() > 0.95:
					currTermToScore = makeCompletePrediction(currTermToScore)
					if len(currTermToScore) > 0 and len(targetAnnotations[currTarget]) > 0:
						methodToTargetToTermToScore.setdefault(cafaFilePath, {})[currTarget] = currTermToScore
#						evalInput.append((currTarget, currTermToScore, i))
#						result = simpleMeasure(currTarget, currTermToScore)
#						results.append(result)
						i+=1
#						if i == 1000: break
					
				currTarget = target
				currTermToScore = {}

#			print term
			currTermToScore[term] = score
	cafaFile.close()
	currTermToScore = makeCompletePrediction(currTermToScore)
	if len(currTermToScore) > 0 and len(targetAnnotations[currTarget]) > 0:
		methodToTargetToTermToScore.setdefault(cafaFilePath, {})[currTarget] = currTermToScore

	
	return methodToTargetToTermToScore

def filterEvalInput(evalInputs, threshold):
#	print "Filtering " + str(threshold)
	returnEvalInputs = []
	for evalInput in evalInputs:
#		print "\tNew Method"
		returnEvalInput = []
		for triple in evalInput:
			currTermToScore = {}
			target, termToScore, i = triple
#			print "\t" + target + " " + str(termToScore)
			for term in termToScore.keys():
				if termToScore[term] >= threshold:
#					print "\tadded " + term
					currTermToScore[term] = termToScore[term]
			if len(currTermToScore) > 0:
				returnEvalInput.append((target, currTermToScore, i))
		returnEvalInputs.append(returnEvalInput)

#	print "After Filter: " + str(returnEvalInputs)
	return returnEvalInputs

if __name__ == "__main__":
	
	treeFilePath = "goGraph.txt"
	closurePath = "fullTransitiveClosureGO.txt"
	annotationsFilePath = "/mnt/home/hampt/workspace/doctorProject/src/CAFA/sprot_go.fasta.test.annotation"
#	annotationsFilePath = "/mnt/home/hampt/workspace/doctorProject/src/CAFA/testCafaAnnot.txt"
	initFullGoTree(treeFilePath)
	initClosure(closurePath)
	initAnnotations(annotationsFilePath)
	
#	cafaFiles= ["/mnt/home/hampt/workspace/doctorProject/src/CAFA/sprot_go.fasta.test.outM1.txt","/mnt/home/hampt/workspace/doctorProject/src/CAFA/sprot_go.fasta.test.outM2.txt","/mnt/home/hampt/workspace/doctorProject/src/CAFA/sprot_go.fasta.test.outR.txt","/mnt/home/hampt/workspace/doctorProject/src/CAFA/sprot_go.fasta.test.outX.txt", "/mnt/home/hampt/workspace/doctorProject/src/CAFA/sprot_go.fasta.test.outY.txt", "/mnt/home/hampt/workspace/doctorProject/src/CAFA/sprot_go.fasta.test.outZ.txt", "/mnt/home/hampt/workspace/doctorProject/src/CAFA/sprot_go.fasta.test.outM2R.txt" ]
	cafaFiles= ["/mnt/home/hampt/workspace/doctorProject/src/CAFA/sprot_go.fasta.test.outX.txt"  ]
	

	
	methodToTargetToTermToScore = {}
	inputs = []

	for filePath in cafaFiles:
		inputs.append((filePath))
	
	print inputs
	pool = Pool(processes=10)
	resultMaps = pool.map(fillData, inputs, chunksize=1)
	for resultMap in resultMaps:
		for key, val in resultMap.iteritems():
			methodToTargetToTermToScore[key] = val
	
	targetToTermToMethodToScore = collections.defaultdict(dict)
	for method, methodDict in methodToTargetToTermToScore.iteritems():
		for target, targetDict in methodDict.iteritems():
			for term, score in targetDict.iteritems():
				targetToTermToMethodToScore[target].setdefault(term,{})[method] = score
	
#	outMeta = open("/mnt/home/hampt/workspace/doctorProject/src/CAFA/meta2.out",'w')
#	i=0
#	for target, targetDict in targetToTermToMethodToScore.iteritems():
#			for term, termDict in targetDict.iteritems():
#				currPred = [0.0,0.0,0.0]
#				for i, method in enumerate(cafaFiles):
#					if method in termDict.keys():
#						currPred[i] = termDict[method]
#				outMeta.write(target + "\t" + term + "\t" + str(lin(currPred)) + "\n")
#			i+=1
#	outMeta.close()
#
#	sys.exit()

#	print methodToTargetToTermToScore

	commonTargets = reduce(set.intersection, [set(methodToTargetToTermToScore[method].keys()) for method in methodToTargetToTermToScore.keys()])
	
	evalInputsCommonTargets = []
	evalInputsAllTargets = []
	currEvalInputAll = []
	currEvalInputCommon = []
	
	for method in sorted(methodToTargetToTermToScore.keys()):
		i=0
#		print method
		for target in sorted(methodToTargetToTermToScore[method].keys()):
#			print "\t" + target
			currEvalInputAll.append((target, methodToTargetToTermToScore[method][target], i))
			i += 1
			print i
			if target in commonTargets:
				currEvalInputCommon.append((target, methodToTargetToTermToScore[method][target], i))
			
#			for term in sorted(methodToTargetToTermToScore[method][target].keys()):
#				print "\t\t" + term + " " + str(methodToTargetToTermToScore[method][target][term])
		

		evalInputsAllTargets.append(currEvalInputAll)
		evalInputsCommonTargets.append(currEvalInputCommon)
		currEvalInputAll = []
		currEvalInputCommon = []
		
#	print "\t==BRUTAL=="
#	for k, evalInputTargets in enumerate(evalInputsAllTargets):
#		if len(evalInputTargets) > 0:
#			outputFile = open("/mnt/home/hampt/workspace/doctorProject/src/CAFA/outputBrutal_all_" + str(k+1) + ".txt", 'w')
#	
#			print "mapping " + str(len(evalInputTargets))
#			results = pool.map(brutal, evalInputTargets, chunksize=10)
#			print "finished mapping with " + str(len(results))
##			print results
#			
#			tuplesSummed = reduce(lambda x,y: (x[0]+y[0], x[1]+y[1]), results)
#			tuplesNorm = (tuplesSummed[0] / 10000, tuplesSummed[1] /10000)
#			
#			outputFile.write("%.5f %.5f" % tuplesNorm + "\n")
#		
#			outputFile.close()
#			
#	for k, evalInputTargets in enumerate(evalInputsCommonTargets):
#		if len(evalInputTargets) > 0:
#			outputFile = open("/mnt/home/hampt/workspace/doctorProject/src/CAFA/outputBrutal_common_" + str(k+1) + ".txt", 'w')
#	
#			print "mapping " + str(len(evalInputTargets))
#			results = pool.map(brutal, evalInputTargets, chunksize=10)
#			print "finished mapping with " + str(len(results))
##			print results
#				
#			tuplesSummed = reduce(lambda x,y: (x[0]+y[0], x[1]+y[1]), results)
#			tuplesNorm = (tuplesSummed[0] / len(evalInputTargets), tuplesSummed[1] /len(evalInputTargets))
#			
#			outputFile.write("%.5f %.5f" % tuplesNorm + "\n")
#		
#			outputFile.close()
#			
#	print "\t==ULTRA=="
#	for k, evalInputTargets in enumerate(evalInputsAllTargets):
#		if len(evalInputTargets) > 0:
#			outputFile = open("/mnt/home/hampt/workspace/doctorProject/src/CAFA/outputUltra_all_" + str(k+1) + ".txt", 'w')
#	
#			print "mapping " + str(len(evalInputTargets))
#			results = pool.map(ultra, evalInputTargets, chunksize=10)
#			print "finished mapping with " + str(len(results))
##			print results
#				
#			tuplesSummed = reduce(lambda x,y: (x[0]+y[0], x[1]+y[1]), results)
#			tuplesNorm = (tuplesSummed[0] / 10000, tuplesSummed[1] /10000)
#			
#			outputFile.write("%.5f %.5f" % tuplesNorm + "\n")
#		
#			outputFile.close()
#			
#	for k, evalInputTargets in enumerate(evalInputsCommonTargets):
#		if len(evalInputTargets) > 0:
#			outputFile = open("/mnt/home/hampt/workspace/doctorProject/src/CAFA/outputUltra_common_" + str(k+1) + ".txt", 'w')
#	
#			print "mapping " + str(len(evalInputTargets))
#			results = pool.map(ultra, evalInputTargets)
#			print "finished mapping with " + str(len(results))
##			print results
#				
#			tuplesSummed = reduce(lambda x,y: (x[0]+y[0], x[1]+y[1]), results)
#			tuplesNorm = (tuplesSummed[0] / len(evalInputTargets), tuplesSummed[1] /len(evalInputTargets))
#			
#			outputFile.write("%.5f %.5f" % tuplesNorm + "\n")
#		
#			outputFile.close()
#		
#	for threshold in [0.01, 0.5, 0.9]:
#		print "THRESHOLD " + str(threshold) 
#		
#		evalInputsAllTargetsFiltered = filterEvalInput(evalInputsAllTargets, threshold)
#		tmpList = []
#		for evalInput in evalInputsAllTargetsFiltered:
#			targets = set([])
#			for target, termToScore, i in evalInput:
#				targets.add(target)
#			tmpList.append(targets)
#		commonTargets = reduce(set.intersection, tmpList)
#		
#		evalInputsCommonTargetsFiltered = []
#		for evalInput in evalInputsAllTargetsFiltered:
#			currInputList = []
#			for triple in evalInput:
#				if triple[0] in commonTargets:
#					currInputList.append(triple)
#			evalInputsCommonTargetsFiltered.append(currInputList)
#
#		print "\t==ALL=="
#		for k, evalInputTargets in enumerate(evalInputsAllTargetsFiltered):
#			if len(evalInputTargets) > 0:
#				outputFile = open("/mnt/home/hampt/workspace/doctorProject/src/CAFA/outputLeaves_all_" + str(k+1) + "_"+ str(threshold) +".txt", 'w')
#		
#				print "mapping " + str(len(evalInputTargets))
#				results = pool.map(leafMeasure, evalInputTargets,chunksize=10)
#				print "finished mapping with " + str(len(results))
#				for i in range(10):
#					print i
#					allPrec = []
#					allRecs = []
#					for result in results:
#						allRecs.append(result[min(len(result)-1,i)][0])
#						allPrec.append(result[min(len(result)-1,i)][1])
#					
#					outputFile.write("%.5f" % (sum(allPrec) / len(allPrec)) + " " + "%.5f" % (sum(allRecs) / 10000) + "\n")
#			
#				outputFile.close()
#			
#		print "\t==COMMON=="
#		for k, evalInputTargets in enumerate(evalInputsCommonTargetsFiltered):
#			if len(evalInputTargets) > 0:
#				outputFile = open("/mnt/home/hampt/workspace/doctorProject/src/CAFA/outputLeaves_common_" + str(k+1) + "_"+ str(threshold) +".txt", 'w')
#		
#				print "mapping " + str(len(evalInputTargets))
#				results = pool.map(leafMeasure, evalInputTargets,chunksize=10)
#				print "finished mapping with " + str(len(results))
#				for i in range(10):
#					print i
#					allPrec = []
#					allRecs = []
#					for result in results:
#						allRecs.append(result[min(len(result)-1,i)][0])
#						allPrec.append(result[min(len(result)-1,i)][1])
#					
#					outputFile.write("%.5f" % (sum(allPrec) / len(allPrec)) + " " + "%.5f" % (sum(allRecs) / len(allRecs)) + "\n")
#			
#				outputFile.close()
#			
#		print "\t==USERPOV=="
#		for k, evalInputTargets in enumerate(evalInputsAllTargetsFiltered):
#			if len(evalInputTargets) > 0:
#				outputFile = open("/mnt/home/hampt/workspace/doctorProject/src/CAFA/outputLeaves_userPov_" + str(k+1) + "_"+ str(threshold) +".txt", 'w')
#		
#				print "mapping " + str(len(evalInputTargets))
#				results = pool.map(leafMeasure, evalInputTargets,chunksize=10)
#				print "finished mapping with " + str(len(results))
#				for i in range(10):
#					print i
#					allPrec = []
#					allRecs = []
#					for result in results:
#						allRecs.append(result[min(len(result)-1,i)][0])
#						allPrec.append(result[min(len(result)-1,i)][1])
#					
#					outputFile.write("%.5f" % (sum(allPrec) / len(allPrec)) + " " + "%.5f" % (sum(allRecs) / len(allRecs)) + "\n")
#			
#				outputFile.close()
		


	for k, evalInputTargets in enumerate(evalInputsAllTargets):
		outputFile = open("/mnt/home/hampt/workspace/doctorProject/src/CAFA/Routput_all_" + str(k+1) + ".txt", 'w')

		print "mapping " + str(len(evalInputTargets))
		results = map(simpleMeasure, evalInputTargets)
		print "finished mapping "
		for i in range(1):
			print i
			allPrec = []
			allRecs = []
			for result in results:
				allRecs.append(result[min(len(result)-1,i)][0])
				allPrec.append(result[min(len(result)-1,i)][1])
			
			if i==0:
				print allPrec
			
			outputFile.write("%.5f" % (sum(allPrec) / len(allPrec)) + " " + "%.5f" % (sum(allRecs) / 10000) + "\n")
		outputFile.close()
#
#	for k, evalInputTargets in enumerate(evalInputsCommonTargets):
#		outputFile = open("/mnt/home/hampt/workspace/doctorProject/src/CAFA/output_common_" + str(k+1) + ".txt", 'w')
#
#		print "mapping " + str(len(evalInputTargets))
#		results = pool.map(simpleMeasure, evalInputTargets, chunksize=10)
#		print "finished mapping "
#		for i in range(100):
#			print i
#			allPrec = []
#			allRecs = []
#			for result in results:
#				allRecs.append(result[min(len(result)-1,i)][0])
#				allPrec.append(result[min(len(result)-1,i)][1])
#			
#			outputFile.write("%.5f" % (sum(allPrec) / len(allPrec)) + " " + "%.5f" % (sum(allRecs) / len(allRecs)) + "\n")
#	
#		outputFile.close()

# BRUTAL CURVE	
	
	for k, evalInputTargets in enumerate(evalInputsAllTargets):
		outputFile = open("/mnt/home/hampt/workspace/doctorProject/src/CAFA/RoutputLeavesCurve_all_" + str(k+1) + ".txt", 'w')

		print "mapping " + str(len(evalInputTargets))
		results = map(ultraCurve, evalInputTargets)
		print "finished mapping "
		for i in range(1):
			print i
			allPrec = []
			allRecs = []
			for result in results:
				allRecs.append(result[min(len(result)-1,i)][0])
				allPrec.append(result[min(len(result)-1,i)][1])
			
			if i==0:
				print allPrec
			outputFile.write("%.5f" % (sum(allPrec) / len(allPrec)) + " " + "%.5f" % (sum(allRecs) / 10000) + "\n")
		outputFile.close()

#	for k, evalInputTargets in enumerate(evalInputsCommonTargets):
#		outputFile = open("/mnt/home/hampt/workspace/doctorProject/src/CAFA/RoutputLeavesCurve_common_" + str(k+1) + ".txt", 'w')
#
#		print "mapping " + str(len(evalInputTargets))
#		results = pool.map(ultraCurve, evalInputTargets, chunksize=10)
#		print "finished mapping "
#		for i in range(100):
#			print i
#			allPrec = []
#			allRecs = []
#			for result in results:
#				allRecs.append(result[min(len(result)-1,i)][0])
#				allPrec.append(result[min(len(result)-1,i)][1])
#			
#			outputFile.write("%.5f" % (sum(allPrec) / len(allPrec)) + " " + "%.5f" % (sum(allRecs) / len(allRecs)) + "\n")
#	
#		outputFile.close()
	
	

	
	
