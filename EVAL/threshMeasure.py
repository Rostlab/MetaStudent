'''
Created on Nov 1, 2011

@author: hampt
'''
import sys
from commons import bpRoot, mfRoot, parseGOAnnotations, GOPrediction
from f1Measure import maxf1Measure
'''
Created on Sep 25, 2011

@author: hampt
'''
from multiprocessing.pool import Pool
from operator import itemgetter

def calcThreshMeasure(goPrediction):
	#print "calcing top N measure"
	returnVals = []
	
	inputs = [(target, goPrediction) for target in goPrediction.targetToTermToScore.keys()]
	
	p=None
#	if len(inputs) > 1000: 
#		pass
##		p=Pool(processes=10)
##		results = p.map(topNmeasure, inputs,chunksize=50)
#	else:
	results = map(threshMeasure, inputs)
	
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
	
	print "\n".join([str(tuplei) for tuplei in returnVals])
	return returnVals


def threshMeasure((target, goPrediction)):
	result = []
	go = goPrediction.goAnnotations.go
	termToScore = goPrediction.targetToTermToScore[target]
	annotTerms = goPrediction.goAnnotations.annotations[target]
	if len(annotTerms) > 0:
		threshs = range(100)
		threshs.reverse()
		for i in threshs:
			currThresh = i / 100.0
			predictedTermsSub =  set([term for term, score in termToScore.iteritems() if score >= currThresh])
			
			predictedTermsSubParents = go.getAllParents(predictedTermsSub)
			allPredictedTermsSub = predictedTermsSub | predictedTermsSubParents
		
			if bpRoot in allPredictedTermsSub:
				allPredictedTermsSub.remove(bpRoot)
			if mfRoot in allPredictedTermsSub:
				allPredictedTermsSub.remove(mfRoot)

			if len(allPredictedTermsSub) > 0:
				result.append((calcPrecRecall(target, allPredictedTermsSub, annotTerms )))
			else:
				result.append((-1,-1))

	else:
		print "empty target: " + target

	
	return result

def calcPrecRecall(target, predictedTerms, actualTerms):

	TPs = len(actualTerms.intersection(predictedTerms))
	FPs = len(predictedTerms - actualTerms)
	FNs = len(actualTerms - predictedTerms)
	
	recall = TPs / float(TPs + FNs)
	precision = TPs / float(TPs + FPs)

	#print str(precision) + "\t" + "FPs: " + str(len(predictedTerms - actualTerms)) + "\t" + "TPs: " + str(len(actualTerms.intersection(predictedTerms)))
	
	return recall, precision





if __name__ == "__main__":
		
	predictionFilePath = sys.argv[1]  #"/mnt/project/interres/cafa/datasets/predictions/TEAM_31_BPO_PRED.txt.new.txt"	
	annotationFilePath = sys.argv[2]
	goTreeFilePath = sys.argv[3] #"/mnt/project/interres/cafa/goGraph.txt"
	closureFilePath = sys.argv[4] #"/mnt/project/interres/cafa/fullTransitiveClosureGO.txt"
	ontology= sys.argv[5] #"BPO"
	numTargets = sys.argv[6] #Number of targets
	goAnnotations = parseGOAnnotations(goTreeFilePath, closureFilePath, ontology, annotationFilePath=annotationFilePath)
	goPrediction = GOPrediction(predictionFilePath, goAnnotations, int(numTargets))
	
	
	
#	print scores2ranks([("bla",0.5),("go2",1.0),("go4",1.0),("go5",1.0),("go6",0.5),("go7",0.5),("go0",0.5),("go1",0.5),("go3",0.5),("bla",0.5)])

	print maxf1Measure(calcThreshMeasure(goPrediction))
	
