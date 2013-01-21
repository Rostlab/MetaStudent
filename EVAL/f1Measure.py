'''
Created on Sep 29, 2011

@author: hampt
'''
import math
import collections
import os

def f1((prec, rec)):
	return  2 * prec * rec / (prec + rec)

def maxf1Measure(precRecPairs):
	maxF1 = 0
	for (prec, rec) in precRecPairs:
		currF1=0
		if prec + rec > 0:
			currF1 =  2 * prec * rec / (prec + rec)
		if currF1 > maxF1:
			maxF1 = currF1
			
	return maxF1

def avgf1Measure(precRecPairs):
	scores = []
	for (prec, rec) in precRecPairs:
		if prec + rec > 0:
			currF1 =  2 * prec * rec / (prec + rec)
			scores.append(currF1)
	
	if len(scores) > 0:
		return sum(scores) / len(scores)
	else:
		return 0
	
def equalWidthF1Measure(precRecPairs):
	cellToRecPrecList = collections.defaultdict(list)
	for (prec, rec) in precRecPairs:
		precIndex = int(math.floor(prec*10))
		recIndex = int(math.floor(rec*10))
#		print (precIndex, recIndex)
#		print (prec, rec)
#		print
		cellToRecPrecList[(precIndex, recIndex)].append((prec, rec))
#	print "-----"
	recPrecSum = [0.0, 0.0]
	for key, precRecList in cellToRecPrecList.iteritems():
		print precRecList
		avgPrecRec = reduce(lambda x,y: (x[0] + y[0],x[1] + y[1]), precRecList)
		avgPrecRec = avgPrecRec[0] / len(precRecList), avgPrecRec[1] / len(precRecList)
		recPrecSum[0] += avgPrecRec[0]
		recPrecSum[1] += avgPrecRec[1]
#		print recPrecSum
#		print ""
	
	prec, rec = recPrecSum[0] / len(cellToRecPrecList.keys()), recPrecSum[1] / len(cellToRecPrecList.keys())
#	print prec
#	print rec
#	print 2 * prec * rec / (prec + rec)
	return 2 * prec * rec / (prec + rec)

def equalWidthF1MeasureNew(precRecPairs):
	cellToRecPrecList = collections.defaultdict(list)
	for (prec, rec) in precRecPairs:
		precIndex = int(math.floor(prec*10))
		recIndex = int(math.floor(rec*10))
		cellToRecPrecList[(precIndex, recIndex)].append((prec, rec))

	f1Sum = 0.0
	for key, precRecList in cellToRecPrecList.iteritems():
		f1s = []
		for (prec, rec) in precRecList:
			if prec + rec > 0:
				f1s.append(f1((prec, rec)))

		avgF1=0.0
		if len(f1s) > 0:
			avgF1 = sum(f1s) / len(f1s)

		f1Sum += avgF1

	return f1Sum / len(cellToRecPrecList).keys()

def readPrecRecFile(path, delimi="\t"):
	pairs=[]
	file=open(path)
	for line in file:
		if len(line.split(delimi)) == 2:
			prec, rec = line.rstrip("\n").split(delimi)
		pairs.append((float(prec), float(rec)))
	file.close()
	return pairs

def avgDistToRandom(precRecPairs, onto):
	

	randomResults=readPrecRecFile("/mnt/project/interres/cafa/datasets_01_2010/evaluations/randomR1%s.txt" % (onto), delimi=" ")

	
	recToPrecListRandom = collections.defaultdict(list)
	for (prec, rec) in randomResults:
		recIndex = int(math.floor(rec*10))
		recToPrecListRandom[recIndex].append(prec)
		
		 
		
	recToAvgPrecRandom = {}
	for rec in recToPrecListRandom.keys():
		#print str(rec) + " " + str(recToPrecListRandom[rec])
		recToAvgPrecRandom[rec] = sum(recToPrecListRandom[rec]) / len(recToPrecListRandom[rec])
#		print str(rec) + " " + str(recToAvgPrecRandom[rec])
	
	
	
	recToPrecListMethod = collections.defaultdict(list)
	for (prec, rec) in precRecPairs:
		recIndex = int(math.floor(rec*10))
		recToPrecListMethod[recIndex].append(prec)
	recToAvgPrecMethod = {}
	for rec in recToPrecListMethod.keys():
		recToAvgPrecMethod[rec] = sum(recToPrecListMethod[rec]) / len(recToPrecListMethod[rec])
	#	print str(rec) + " " + str(recToPrecListMethod[rec])
	
	distances = []
	for rec in recToAvgPrecMethod.keys():
		if rec in recToAvgPrecRandom.keys():
			avgPrecMethod = recToAvgPrecMethod[rec]
			avgPrecRandom = recToAvgPrecRandom[rec]
	#		print str(rec) + " " + str(recToAvgPrecMethod[rec]) + " " + str(recToAvgPrecRandom[rec])
			distance = avgPrecMethod - avgPrecRandom
	#		print distance
			if distance >= 0:
				distance = distance# * distance
			else:
				distance = (distance)# * distance)
			
			#print str(rec) + " " + str(distance)
			distances.append(distance)
	
	#print distances
	if len(distances) == 0:
		return 0.0
	else:
		return sum(distances) / len(distances)
	



if __name__ == "__main__":
#	test = [(1.0,0.5), (0.1,0.013),(0.1,0.01),(0.11,0.01),(0.8,0.51),(0.12,0.06), (0.01,0.03)]
#	test2 = [(1.0,0.65), (0.1,0.013),(0.1,0.01),(0.11,0.01),(0.8,0.51),(0.12,0.06), (0.01,0.03)]
#	
	methodAndOntoToBestParas = collections.defaultdict(list)

	bestParas = ""
	bestScore= 0
	parasScores=[]
	folder= "/mnt/project/interres/cafa/methods/meta/paraEvalResults"
	for file in os.listdir(folder):
		fileAbs = os.path.join(folder, file)
		onto=""
		if "BPO" in file: onto="BPO"
		else: onto="MFO"
		score = maxf1Measure(readPrecRecFile(fileAbs))
		parasScores.append((file, score))
		
		method = file.split("_")[0]
		methodAndOntoToBestParas[(method+"_"+onto)].append((file, score))
		
		if score > bestScore:
			bestScore=score
			bestParas=file
			
	print bestParas
	print bestScore
	print
	print
	
	for methodAndOnto, parasList in methodAndOntoToBestParas.iteritems():
		print [ para + "\t" + str(score) for (para, score) in sorted( parasList, key=lambda x: x[1] , reverse=True  )    ][0]
	
	
#	test3 = readPrecRecFile("/mnt/project/interres/cafa/datasets_01_2010/paraEvalResults/C_1e-05_0.002_3_1_exp_BPO")
#	test4 = readPrecRecFile("/mnt/project/interres/cafa/datasets_01_2010/paraEvalResults/C_0.001_1e-99_3_0_all_BPO")
#	test5 = readPrecRecFile("/mnt/project/interres/cafa/datasets_01_2010/paraEvalResults/B_0.001_10e-100_2_5_exp_MFO")
#	#equalWidthF1Measure(test)
#	print avgDistToRandom(test3, "BPO")
#	print avgDistToRandom(test4, "BPO")
	#print avgDistToRandom(test5, "MFO")

