

PROJDIR=/home/proj/pp2b


DATABASE=$1 #/home/proj/pp2b/sprot_go_filtered.fasta
PREFIX=$2 #/home/proj/pp2b/sequence_sets

for percentage in 0.1 1 2 5 10 15 20 25 50 75 100; do

	echo Generating $percentage% set...
	mkdir -p $PREFIX/$percentage-percent

	PERCENTSET=$PREFIX/$percentage-percent/$percentage-percent-complete.f

	./make_percent.pl -p $percentage < $DATABASE > $PERCENTSET

	for fold in 3 10; do
		echo Generating $fold-fold split...
		
		mkdir -p $PREFIX/$percentage-percent/splits/$fold-fold

		ARGS=''
		for (( i=0; i<$fold; i++ )); do 
			ARGS="$ARGS 1 $PREFIX/$percentage-percent/splits/$fold-fold/part$i.f"
		done



		./make_splits.pl$ARGS < $PERCENTSET

	done
done
