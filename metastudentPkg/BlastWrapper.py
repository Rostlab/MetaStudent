'''
Created on May 18, 2010

@author: tobiashamp
'''


class blastallParameters:
	def __init__(self):
		self.hValue = ""
		self.numberOfCPUs = 1
	
	def setBlastExePath(self, path):
		self.blastExePath = path
	
	def setOutputFilePath(self, path):
		self.outputFilePath = path
		
	def getOutputFilePath(self):
		return self.outputFilePath

	def setInputFilePath(self, path):
		self.inputFilePath = path
		
	def getBlastExePath(self):
		return self.blastExePath
	
	def getInputFilePath(self):
		return self.inputFilePath

	def setBlastDatabasePath(self, path):
		self.blastDatabasePath = path
		
	def getBlastDatabasePath(self):
		return self.blastDatabasePath

	def setEValue(self, value):
		self.eValue = value
		
	def getEValue(self):
		return self.eValue

	def setJ(self, value):
		self.j = value
		
	def getJ(self):
		return self.j
		

def createBlastPGPCommand(blastParameters):
	returnString = blastParameters.getBlastExePath()
	returnString +=	" -i " + blastParameters.getInputFilePath() + \
		" -o " + blastParameters.getOutputFilePath() + \
		" -d " + blastParameters.getBlastDatabasePath() + \
		" -e " + str(blastParameters.getEValue()) + \
		" -j " + str(blastParameters.getJ()) + \
		" 1>" + blastParameters.getOutputFilePath()+".out" + \
		" 2>" + blastParameters.getOutputFilePath()+".err"

	return returnString

