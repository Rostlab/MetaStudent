'''
Created on Nov 1, 2011

@author: hampt
'''
'''
Created on Sep 25, 2011

@author: hampt
'''
from multiprocessing.pool import Pool
from CAFA.EVAL.commons import GOPrediction, parseGOAnnotations
from operator import itemgetter

def calcLeafThreshMeasure(goPrediction):
	print "calcing top N measure"
	returnVals = []
	
	inputs = [(target, goPrediction) for target in goPrediction.targetToTermToScore.keys()]
	
	p=None
#	if len(inputs) > 1000: 
#		pass
##		p=Pool(processes=10)
##		results = p.map(topNmeasure, inputs,chunksize=50)
#	else:
	results = map(ultraCurve, inputs)
	
	for i in range(100):
		allPrec = []
		allRecs = []
		for result in results:
			if result != None and len(result) > 0 and result[i] !=  (-1,-1):
				if len(result) >= i+1:
					allRecs.append(result[i][0])
					allPrec.append(result[i][1])
		
#		if i==0:
#			print "First prec: " + str(allPrec)

		if len(allPrec) == 0:
			returnVals.append((0, 0))
		else:
			returnVals.append((sum(allPrec) / len(allPrec), sum(allRecs) / goPrediction.numberOfTargets))
	
	print "\t".join([str(tuplei) for tuplei in returnVals])
	return returnVals


def ultraCurve((target, goPrediction)):

	result = []
	go = goPrediction.goAnnotations.go
	termToScore = goPrediction.targetToTermToScore[target]
	targetLeaves = go.getLeaveTerms(goPrediction.goAnnotations.annotations[target])
	if len(targetLeaves) > 0:
	
		predictedLeaves = go.getLeaveTerms(termToScore.keys())
		
		precAll = None
		precLeaf = None
	#	leavesMissed = targetLeaves - predictedLeaves
	#	leavesSuperfl = predictedLeaves - targetLeaves
	#	rec, prec = (1-len(leavesMissed) / float(len(targetLeaves)), 1-len(leavesSuperfl) / float(len(predictedLeaves)))
	#	print (rec, prec)
		
	#	for i in range(20):
	##		print i
	#		termToScoreKBest =  getKBest(termToScore, i+1)
	#		termToScoreKBest = goPrediction.makeCompletePrediction(termToScoreKBest)
	#		if len(termToScoreKBest) > 0:
	#			rec, precAll = calcPrecRecall(target, set(termToScoreKBest.keys()), goPrediction.goAnnotations.annotations[target])
	#		else:
	#			break
	#	
		termToScoreLeafs = dict([(key,val) for key,val in termToScore.iteritems() if key in predictedLeaves])
		
	#	print termToScore
	#	print termToScoreLeafs
	
		threshs = range(100)
		threshs.reverse()
		for i in threshs:
			currThresh = i / 100.0
			predictedLeavesSub =  set([term for term, score in termToScoreLeafs.iteritems() if score >= currThresh])
	
			if len(predictedLeavesSub) > 0:
				leavesMissed = targetLeaves - predictedLeavesSub
				leavesSuperfl = predictedLeavesSub - targetLeaves
				rec, precLeaf = (1-len(leavesMissed) / float(len(targetLeaves)), 1-len(leavesSuperfl) / float(len(predictedLeavesSub)))
	#			print str(precLeaf) + "\tleaves: " + str(predictedLeavesSub)
				result.append((rec, precLeaf))
			else:
				result.append((-1,-1))
	#	if (precAll == None or precLeaf == None) and (precAll != precLeaf):
	#		print "ERRROR"
	#	else:
	#		if precAll < precLeaf:
	#			print "!!!!!!!!!!!!!"
			
	#	print result
	else:
		print "empty target: " + target

	
	return result


#def topNmeasure((target, goPrediction)):
#	result = []
#	
##	if target != "Q8C7Q4":
##		return result
#	
##	print target
#	
##	if len(goPrediction.goAnnotations.annotations[target]) == 0:
##		return None
#	
#	termsToScores = goPrediction.targetToTermToScore[target]
#	
#	
#	
#	termScoreTuples = []
#	
#	#to Tuples
#	for term in termsToScores.keys():
#		termScoreTuples.append((term, termsToScores[term]))
#	
#	termScoreTuplesSorted = sorted(termScoreTuples, key=itemgetter(1))
#	
#	termRankTuples = scores2ranks(termScoreTuplesSorted) 
##	
##	print termScoreTuplesSorted
##	print termRankTuples
##	print
#	
##	print termToScore
#	for index in range(20):
#		
#		i=index+1
##		print i
#		goodTerms = set([tuplei[0] for tuplei in termRankTuples if tuplei[1] <= i])
#
##		print goodTerms
##		print
#		#termToScoreKBest =  getKBest(goPrediction.targetToTermToScore[target], i+1)
#
#		termToScoreKBest =  dict([(term, score) for term, score in  termsToScores.iteritems() if term in goodTerms])
##		print termToScoreKBest
#		
#		termToScoreKBest = goPrediction.makeCompletePrediction(termToScoreKBest)
##		print termToScoreKBest
#
#		if len(goPrediction.goAnnotations.annotations[target]) == 0:
#			print "!! ERROR !! target empty"
#			print "target:" + target
#			print goPrediction.goAnnotations.annotations[target]
#		else:
#			if len(termToScoreKBest.keys()) > 0:
#				result.append((calcPrecRecall(target, set(termToScoreKBest.keys()), goPrediction.goAnnotations.annotations[target] )))
#			else: 
#				print "!! ERROR !! first hit empty"
#				print "target:" + target
#				print goPrediction.goAnnotations.annotations[target]
#
#		if index==0 and len(result) == 0:
#			print "!!!!!"
#
##	print result
#	
##	if target == "Q8C7Q4":
##		print
##		print result
##		print
##		print termsToScores
##		print
##		print goPrediction.goAnnotations.annotations[target]
#	
#	return result

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
	
	predictionFilePath = "/mnt/project/interres/cafa/datasets/predictions/TEAM_29_BPO_PRED.txt.new.txt"	
	goTreeFilePath = "/mnt/project/interres/cafa/goGraph.txt"
	closureFilePath = "/mnt/project/interres/cafa/fullTransitiveClosureGO.txt"
	ontology="BPO"
	numTargets = 0
	goAnnotations = parseGOAnnotations(goTreeFilePath, closureFilePath, ontology, annotationFilePath="/mnt/project/interres/cafa/datasets/targets/BPO_TARGET_ANNOTATIONS.txt.new.txt")
	goPrediction = GOPrediction(predictionFilePath, goAnnotations, len(goAnnotations.annotations))
	
#	print scores2ranks([("bla",0.5),("go2",1.0),("go4",1.0),("go5",1.0),("go6",0.5),("go7",0.5),("go0",0.5),("go1",0.5),("go3",0.5),("bla",0.5)])

	calcLeafThreshMeasure(goPrediction)
	
