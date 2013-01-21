

PROJDIR=/home/proj/pp2b

for (( cluster=45; cluster <= 95; cluster+=5 )); do

	./splitall.sh $PROJDIR/clustered_sets/cluster$cluster/sprot-go-$cluster-filtered.f $PROJDIR/clustered_sets/cluster$cluster

done

