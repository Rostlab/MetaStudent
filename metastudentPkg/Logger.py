# Written by Tobias Hamp <hampt@rostlab.org>
#
# Copyright (c) 2011-2012 Tobias Hamp <hampt@rostlab.org>



import logging
import sys
class Logger:
	
	internalLogger = None
	logFile = None

	def __init__(self, logfile=None):
		Logger.internalLogger = logging.getLogger('metastudent')
		#self.logFile = open(logfile, 'a')
#		hdlr = logging.FileHandler('/var/log/metatsudent.log')
#		formatter = logging.Formatter('%(asctime)s %(levelname)s %(message)s')
#		hdlr.setFormatter(formatter)
#		Logger.internalLogger.addHandler(hdlr) 
#		Logger.internalLogger.setLevel(logging.INFO)
	
	@classmethod
	def log(cls, msg, level=0):
		if level==0:
			print msg
		else:
			print >> sys.stderr,msg
		
		#cls.internalLogger.info(msg)
		


