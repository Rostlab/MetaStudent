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
	results = map(brutalCurve, inputs)
	
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
#		print sorted(allPrec)
		if len(allPrec) == 0:
			returnVals.append((0, 0))
		else:
			returnVals.append((sum(allPrec) / len(allPrec), sum(allRecs) / goPrediction.numberOfTargets))
	
	print "\n".join([str(tuplei) for tuplei in returnVals])
	return returnVals


def brutalCurve((target, goPrediction)):

	result = []
	go = goPrediction.goAnnotations.go
	termToScore = goPrediction.targetToTermToScore[target]
	targetLeaves = go.getLeaveTerms(goPrediction.goAnnotations.annotations[target])
	predictedLeaves = go.getLeaveTerms(termToScore.keys())
	
	
	leafPropagations = []
	for leaf in targetLeaves:
		leafProp = go.getAllParents([leaf])
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
	
	
	precAll = None
	precLeaf = None

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
			functionHitCounter = 0
			for uniqueLeafProp in uniqueLeafProps:
				if len(predictedLeavesSub.intersection(uniqueLeafProp)) > 0:
					functionHitCounter += 1

			rec, prec =  (functionHitCounter / float(len(targetLeaves)), functionHitCounter / float(len(predictedLeavesSub)))

#			if i==80:
#				print str(len(targetLeaves)) + " " + str(len(predictedLeavesSub)) + " " + str(functionHitCounter / float(len(predictedLeavesSub)))

			result.append((rec, prec))
		else:
			result.append((-1,-1))
#	if (precAll == None or precLeaf == None) and (precAll != precLeaf):
#		print "ERRROR"
#	else:
#		if precAll < precLeaf:
#			print "!!!!!!!!!!!!!"
		
#	print result
	return result



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



if __name__ == "__main__":
	
	predictionFilePath = "/mnt/project/interres/cafa/datasets/predictions/TEAM_29_BPO_PRED.txt.new.txt"	
	goTreeFilePath = "/mnt/project/interres/cafa/goGraph.txt"
	closureFilePath = "/mnt/project/interres/cafa/fullTransitiveClosureGO.txt"
	ontology="BPO"
	numTargets = 0
	goAnnotations = parseGOAnnotations(goTreeFilePath, closureFilePath, ontology, annotationFilePath="/mnt/project/interres/cafa/datasets/targets/BPO_TARGET_ANNOTATIONS.txt.new.txt")
	goPrediction = GOPrediction(predictionFilePath, goAnnotations, len(goAnnotations.annotations))
	
#	print scores2ranks([("bla",0.5),("go2",1.0),("go4",1.0),("go5",1.0),("go6",0.5),("go7",0.5),("go0",0.5),("go1",0.5),("go3",0.5),("bla",0.5)])

	calcTopNMeasure(goPrediction)
	

