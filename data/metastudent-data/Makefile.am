export XZ_OPT

# lkajan: keep directory name in sync with dataversion.txt (dataset_`cat dataversion.txt`)
DATASET = dataset_$(shell cat dataversion.txt)
CCOALLBLAST = $(DATASET)/CCO/goasp.fasta.psq
BPOALLBLAST = $(DATASET)/BPO/goasp.fasta.psq
MFOALLBLAST = $(DATASET)/MFO/goasp.fasta.psq
BLASTDB = $(BPOALLBLAST) $(MFOALLBLAST) $(CCOALLBLAST)

EXTRA_DIST = $(DATASET)

dist_pkgdata_DATA = dataversion.txt
noinst_DATA = $(BLASTDB)

install-data-local:
	mkdir -p "$(DESTDIR)$(pkgdatadir)" && \
		rsync -rt --include '*.txt' --include '*.dat' --include '*.phr' --include '*.pin' --include '*.psq' --exclude '*.*' "$(srcdir)/$(DATASET)" "$(DESTDIR)$(pkgdatadir)/"  && \
		chmod -R a+rX "$(DESTDIR)$(pkgdatadir)" && \
		chmod -R u+rwX "$(DESTDIR)$(pkgdatadir)"

# BLAST databases are architecture dependent and have to be installed in pkgdatadir
$(BLASTDB) : %.fasta.psq : $(srcdir)/%.fasta
	mkdir -p $(dir $@) && \
		formatdb -i "$<" -n "$(@:.psq=)" && rm -f formatdb.log

dist-hook:
	find "$(distdir)" -name .svn -o -name '*.phr' -o -name '*.pin' -o -name '*.psq' | xargs rm -rf --

clean-local:
	find -regex ".*\.pin\|.*\.phr\|.*\.psq" -delete

uninstall-local:
	rm -rf "$(DESTDIR)$(pkgdatadir)"
