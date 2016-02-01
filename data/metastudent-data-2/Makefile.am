export XZ_OPT

# lkajan: keep directory name in sync with dataversion.txt (dataset_`cat dataversion.txt`)
DATASET = dataset_$(shell cat dataversion.txt)

EXTRA_DIST = $(DATASET)

dist_pkgdata_DATA = dataversion.txt

install-data-local:
	mkdir -p "$(DESTDIR)$(pkgdatadir)" && \
		rsync -rt --include '*.txt' --exclude '*.*' "$(srcdir)/$(DATASET)" "$(DESTDIR)$(pkgdatadir)/"  && \
		chmod -R a+rX "$(DESTDIR)$(pkgdatadir)" && \
		chmod -R u+rwX "$(DESTDIR)$(pkgdatadir)"

dist-hook:
	find "$(distdir)" -name .svn | xargs rm -rf --

uninstall-local:
	rm -rf "$(DESTDIR)$(pkgdatadir)"
