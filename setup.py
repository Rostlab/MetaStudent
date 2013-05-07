#!/usr/bin/python

from distutils.core import setup
from subprocess import Popen, PIPE
import sys
from metastudentPkg.version import VERSION
import os

from metastudentPkg.runMethods import getPkgPath
import shutil
import tempfile


print 'Creating man page metastudent.1...'
childP = Popen(["pod2man", "-c", "'User Commands'", "-r",  "'%s'",  "-name", "METASTUDENT",  "metastudent",  "metastudent.1"], stdout=PIPE, stderr=PIPE)
stdout, stderr = childP.communicate()
if childP.poll() != 0:
	print >> sys.stderr, "Error: pod2man failed"
	print >> sys.stderr, "Stdout: %s" % (stdout)
	print >> sys.stderr, "Stderr: %s" % (stderr)
	sys.exit(1)
	
	
pkgPath=getPkgPath()
dataFiles=[]
exeSuffixes=set([".rb",".py",".pl",".sh",".class",".jar",".exe"])
executables=set(["CafaWrapper3.pl","exercise3.pl","knn_weighted","treehandler.pl"])
#for path, dirs, files in os.walk(os.path.join(pkgPath, "data")):
#	if "/.svn" not in path and "dataset_201012" not in path:
#		for filename in files:
#			filenameSplit = filename.split(".")
#			if len(filenameSplit) > 2 and "."+filenameSplit[-1] in exeSuffixes:
#				os.chmod(os.path.join(path, filename),0755)
#			else:
#				os.chmod(os.path.join(path, filename),0644)
#			dataFiles.append(os.path.relpath(os.path.join(path, filename), pkgPath))

for path, dirs, files in os.walk(os.path.join(pkgPath, "lib")):
	if "/.svn" not in path:
		for dir in dirs:
			os.chmod(os.path.join(path, dir),0755)
		for filename in files:
			if not filename.endswith(".java"):
				filenameSplit = filename.split(".")
				if len(filenameSplit) > 2 and "."+filenameSplit[-1] in exeSuffixes:
					os.chmod(os.path.join(path, filename),0755)
				else:
					os.chmod(os.path.join(path, filename),0644)
				dataFiles.append(os.path.relpath(os.path.join(path, filename), pkgPath))
			if filename in executables:
				os.chmod(os.path.join(path, filename),0755)

metastudentPath=""
if hasattr(sys, "frozen"):
	metastudentPath=os.path.dirname(unicode(sys.executable, sys.getfilesystemencoding( )))
else:
	metastudentPath = os.path.dirname(os.path.abspath(__file__))
for filei in os.listdir(metastudentPath):
	fileAbs = os.path.join(metastudentPath, filei)
	if os.path.isdir(fileAbs):
		os.chmod(os.path.join(metastudentPath, filei),0755)
	else:
		os.chmod(os.path.join(metastudentPath, filei),0644)
	
os.chmod(os.path.join(metastudentPath, "metastudent"),0755);
os.chmod(os.path.join(metastudentPath, "setup.py"),0755);

#metastudentPath=""
#if hasattr(sys, "frozen"):
#	metastudentPath=os.path.dirname(unicode(sys.executable, sys.getfilesystemencoding( )))
#else:
#	metastudentPath = os.path.dirname(os.path.abspath(__file__))
#dataPackagePath = os.path.join(sys.prefix, "share", "metastudent-data")
#defaultConfigFilePath = os.path.join(metastudentPath, "metastudentrc.default")
#defaultConfigFile = open(defaultConfigFilePath)
#defaultConfigFileContent = defaultConfigFile.read()
#defaultConfigFileContent.replace("XX_DATAPATH_XX", dataPackagePath)
#defaultConfigFile.close()
#
#defaultConfigFile = open(defaultConfigFilePath, 'w')
#defaultConfigFile.write(defaultConfigFileContent)
#defaultConfigFile.close()


setup (name = 'metastudent',
       fullname = "metastudent",
       version = VERSION,
       description = "Predict GO terms from sequence",
       long_description = "Metastudent predicts the putative GO terms for a protein using only its sequence.",
       maintainer = "Tobias Hamp",
       maintainer_email = "hampt@rostlab.org",
       author = "Tobias Hamp",
       author_email = "hampt@rostlab.org",
       url = "www.rostlab.org",
       license = "GPL",
       keywords = ["GO","term","prediction","BLAST","ontology","protein","sequence"],
       scripts = ["metastudent"],
       data_files =[	("share/metastudent", ["metastudentrc.default"]),
			("share/doc/metastudent/examples", ["test.fasta","test.result.BPO.txt","test.result.MFO.txt"]),
			("share/man/man1", ["metastudent.1"])
		   ],
       packages = ['metastudentPkg'],
       package_data={'metastudentPkg': dataFiles}
       )

