'''
Created on Sep 25, 2011

@author: hampt
'''
from multiprocessing.pool import Pool
from CAFA.EVAL.commons import GOPrediction, parseGOAnnotations, bpRoot, mfRoot
from operator import itemgetter

def calcTopNMeasure(goPrediction):
	print "calcing top N measure"
	returnVals = []
	
	inputs = [(target, goPrediction) for target in goPrediction.targetToTermToScore.keys()]
	
	p=None
#	if len(inputs) > 1000: 
#		pass
##		p=Pool(processes=10)
##		results = p.map(topNmeasure, inputs,chunksize=50)
#	else:
	results = map(topNmeasure, inputs)
	
	for i in range(20):
		allPrec = []
		allRecs = []
		for result in results:
			if result != None:
				if len(result) >= i+1:
					allRecs.append(result[i][0])
					allPrec.append(result[i][1])
		
#		if i==0:
#			print "First prec: " + str(allPrec)

		if len(allPrec) == 0:
			returnVals.append((0, 0))
		else:
			returnVals.append((sum(allPrec) / len(allPrec), sum(allRecs) / goPrediction.numberOfTargets))
	
	print "\n".join([str(tuplei) for tuplei in returnVals])
	return returnVals

def topNmeasure((target, goPrediction)):
	result = []
	termsToScores = goPrediction.targetToTermToScore[target]
	termScoreTuples = []
	go = goPrediction.goAnnotations.go

	#to Tuples
	for term in termsToScores.keys():
		termScoreTuples.append((term, termsToScores[term]))
	
	termScoreTuplesSorted = sorted(termScoreTuples, key=itemgetter(1))
	
	termRankTuples = scores2ranks(termScoreTuplesSorted) 

	for index in range(20):
		i=index+1
		goodTerms = set([tuplei[0] for tuplei in termRankTuples if tuplei[1] <= i])
		goodTermParents = go.getAllParents(goodTerms)
		allGoodTerms = goodTerms | goodTermParents
		
		if bpRoot in allGoodTerms:
			allGoodTerms.remove(bpRoot)
		if mfRoot in allGoodTerms:
			allGoodTerms.remove(mfRoot)


		if len(goPrediction.goAnnotations.annotations[target]) == 0:
			print "!! ERROR !! target empty"
			print "target: " + target
		else:
			if len(allGoodTerms) > 0:
				result.append((calcPrecRecall(target, allGoodTerms, goPrediction.goAnnotations.annotations[target] )))
			else: 
				print "!! ERROR !! first hit empty"
				print "target: " + target

		if index==0 and len(result) == 0:
			print "!! ERROR !! no result for target " + target

	return result

def getKBest(termsToScores, k):
	scoreToTerms = {}
	for term, score in termsToScores.iteritems():
		scoreToTerms.setdefault(score, set([])).add(term)
	
	termToRank = {}

	returnMap = {}

	if len(scoreToTerms.keys()) >= k:
		terms = set([])
		for score in sorted(scoreToTerms.keys(), reverse=True)[0:k]:
			terms |= scoreToTerms[score]
		
		returnMap = dict([(term, termsToScores[term]) for term in terms])
	
#	print returnMap
	return returnMap

def calcPrecRecall(target, predictedTerms, actualTerms):
#	print "Target: " + target
#	print "Pred Terms: " + str(predictedTerms)
#
#	
#	print "Act terms: " + str(actualTerms)
	
	TPs = len(actualTerms.intersection(predictedTerms))
	FPs = len(predictedTerms - actualTerms)
	FNs = len(actualTerms - predictedTerms)
	
	recall = TPs / float(TPs + FNs)
	precision = TPs / float(TPs + FPs)

	#print str(precision) + "\t" + "FPs: " + str(len(predictedTerms - actualTerms)) + "\t" + "TPs: " + str(len(actualTerms.intersection(predictedTerms)))
	
	return recall, precision

def scores2ranks(scoreTermsTuples, ztol = 1.0e-6):  
	
	X = [tuplei[1]*(-1.0) for tuplei in scoreTermsTuples]
	
	Z = [(x, i) for i, x in enumerate(X)]  
	Z.sort()  
	n = len(Z)  
	Rx = [0] * n	
	for j, (x,i) in enumerate(Z):
#		print "%d is %d" % (i, j+1)
		Rx[i] = j+1  
	s = 1			  # sum of ties.  
	start = end = 0 # starting and ending marks.  
	for i in range(1, n):
#		print i
		
		same = abs(Z[i][0] -Z[i-1][0]) < ztol
		if same:
			pos = Z[i][1]
			s+= Rx[pos]
			end = i
		if (not same) or i == n-1: #end of similar x values.  
#			print "sum " + str(s)
			tiedRank = float(s)/(end-start+1)  
			for j in range(start, end+1):
#				print "%d is %d" % (Z[j][1], tiedRank)
				Rx[Z[j][1]] = tiedRank 
			for j in range(start, end+1):  
				Rx[Z[j][1]] = tiedRank  
			start = end = i  
			s = Rx[Z[i][1]]


	returnList = []

	
	minR = min(Rx)

	for i, tuplei in enumerate(scoreTermsTuples):
		returnList.append((tuplei[0], Rx[i]-minR+1))
	
	return returnList  



if __name__ == "__main__":
	
	predictionFilePath = "/mnt/project/interres/cafa/datasets/predictions/TEAM_59_BPO_PRED.txt.new.txt"	
	goTreeFilePath = "/mnt/project/interres/cafa/goGraph.txt"
	closureFilePath = "/mnt/project/interres/cafa/fullTransitiveClosureGO.txt"
	ontology="BPO"
	numTargets = 0
	goAnnotations = parseGOAnnotations(goTreeFilePath, closureFilePath, ontology, annotationFilePath="/mnt/project/interres/cafa/datasets/targets/BPO_TARGET_ANNOTATIONS.txt.new.txt")
	goPrediction = GOPrediction(predictionFilePath, goAnnotations, len(goAnnotations.annotations))
	
#	print scores2ranks([("bla",0.5),("go2",1.0),("go4",1.0),("go5",1.0),("go6",0.5),("go7",0.5),("go0",0.5),("go1",0.5),("go3",0.5),("bla",0.5)])
	
	
	
	calcTopNMeasure(goPrediction)
	
