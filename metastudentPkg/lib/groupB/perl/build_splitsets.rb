

class BuildSplitsets
  
  @@help = []
  @@help << "build_splitset.rb [options]"
  @@help << "Options:"
  @@help << "-i \t inputfile with sequences"
  @@help << "-o \t path where to put the generated split sets training/testing(crossvalidation) and hold-out (if not existent it will be generated)"
  @@help << "-f \t x fold crossvalidation"
  @@help << "-s \t how much percent of the sequences shoud be used for the training/testing set. The remaining sequences are used for the hold-out set"
  @@help << "-p \t plot the distributions of the GO-Numbers"
  @@help << "-h(elp)  print this help"
  def initialize(opts)
    @xfold = 0
    @split = 0
    @seqs = []
    @seqid = 0
    @plotfile = ""
    @plot =false
    @outpath = ""
    @cv_files = []
    i = 0
    
    # parse options
    while i < opts.size
      if opts[i].eql?("-f") 
        if opts[i+1] =~ /\d+/
          @xfold = opts[i+1].to_i
          i+=1
        else
          raise "Option -f needs an integer parameter!"
        end
      elsif opts[i].eql?("-s") 
        if  opts[i+1] =~ /\d+/
          @split = opts[i+1].to_i
          i+=1
        else
          raise "Option -s needs an integer parameter!"
        end
      elsif opts[i].eql?("-i") 
        if File.exists?(opts[i+1])
          file = File.open(opts[i+1],'rb')
          lines = file.readlines
          parseSequences(lines)
          if opts[i+1] =~ /(\d+)\.f/ # for the naming of the plot files
            @seqid = $1
          end
          i+=1
        else
          raise "File #{opts[i+1]} does not exist!"
        end
      elsif opts[i].eql?("-p")
        @plot = true
      elsif opts[i].eql?("-o")
         if !(opts[i+1].length.nil?)
           @outpath = File.expand_path(opts[i+1])+"/"
           i+=1
         else
           raise "-o needs a directoryname"
         end
      elsif opts[i].eql?("-h") || opts[i].eql?("-help")
        puts @@help
        exit(0)
      else
        raise "Unknown option #{opts[i]}"
      end
      i+=1
    end
    
    if @outpath.eql?("") 
      puts "ERROR: option -o has to be set!"
      puts @@help
      exit(0)
    elsif @xfold == 0
      puts "ERROR: option -f has to be set and to be greater than 0!"
      puts @@help
      exit(0)
    elsif @split == 0
      raise "ERROR: option -s has to be set and to be greater than 0!"
      puts @@help
      exit(0)
    elsif @seqs.size < 1
      raise "ERROR: option -i has to be set!"
      puts @@help
      exit(0)
    end
    #@outpath = @outpath+@seqid+"/" # uncomment for xargs command
    system "mkdir #{@outpath}"
    #get go stats and make tabular file for plotting if -p option is set
    if @plot
      makePlots
    end

    #randomize sequences
    random = @seqs.sort_by{rand(@seqs.size)}
    @seqs = random
    
    #split in test/training and hold-out set
    buildSplitsets

    #build combinatoric file for crossvalidation
    buildCombinatoricFile
  end
  
  
  def parseSequences(lines)
    i = 0
    s = "" 
    while i < lines.size
      if lines[i] =~ /^>/
        s = lines[i]
        i+=1
        while !(lines[i] =~ /^>/) && !(lines[i].nil?)
          s+=lines[i]
          i+=1
        end
        @seqs << s
        s = ""
      else
        raise lines[i]
      end
    end 
  end

  def makePlots

    #first plot, x-GO:Number, y-frequency
    go_counter = Hash.new
    z = 0
    @seqs.each do |s|
      if s =~ /((GO:\d+,{0,1})+)/
        z = z+1
        gos = $1
        go_numbers = gos.split(",")
        go_numbers.each do |g|
          if go_counter[g].nil?
            go_counter[g] = 1
          else
            go_counter[g]+=1
          end
        end
      else 
        raise s
      end
    end    
    plotfile = @outpath+"plotfile_1"
    p_out = File.open(plotfile,'wb')
    go_counter.each_key do |k|
      p_out.write k+"\t"+go_counter[k].to_s+"\n"
    end
    p_out.close
    r_file = File.open(plotfile+".R", 'w')
    r_file.write("x <- read.table(\"#{plotfile}\", header=F)\n")
    r_file.write("plot(x[[1]],x[[2]], main=\"GO-Number frequency\", xlab=\"GO-Number\", ylab=\"frequency\", type=\"l\")")
    r_file.close
    system  "R -f #{plotfile}.R "
    system  "mv ./Rplots.pdf #{@outpath}Rplots#{@seqid}_1.pdf"


    #second plot, x-frequency, y-amount of x-frequency occurred
    frequency_counter = Hash.new
    go_counter.each_key do |g|
      if frequency_counter[go_counter[g].to_s].nil?
        frequency_counter[go_counter[g].to_s] = 1
      else
        frequency_counter[go_counter[g].to_s]+=1
      end
    end
    a = []
    i = 0
    frequency_counter.each_key do |k|
      a[k.to_i] = frequency_counter[k]
    end
    while i < a.size
      if a[i].nil?
        a[i] = 0
     # elsif i < 10  #set the threshold here to remove overrepresentatives in the plot
     #   a[i] = 0
      end
      i+=1
    end
    plotfile = @outpath+"plotfile_2"
    p_out = File.open(plotfile,'wb')
    a.each_index do |k|
      p_out.write k.to_s+"\t"+a[k].to_s+"\n"
    end
    p_out.close
    r_file = File.open(plotfile+".R", 'w')
    r_file.write("x <- read.table(\"#{plotfile}\", header=F)\n")
    r_file.write("plot(x[[1]],x[[2]], main=\"GO-Number frequency distribution\", xlab=\"frequency\", ylab=\"occurrence\", type=\"l\")")
    r_file.close
    system  "R -f #{plotfile}.R"
    system  "mv ./Rplots.pdf #{@outpath}Rplots#{@seqid}_2.pdf"
  end

  def buildSplitsets
    puts "#{@seqs.size} sequences in sequence file!"
    n = (@seqs.size).to_f * (@split.to_f / 100.0)
    puts "There will be #{n} sequences within the training/testset!"
    n = n.to_i
    i = 0 
    tt_set = []
    ho_set = []
    while i < @seqs.size
      if i < n
        tt_set << @seqs[i]
      else
        ho_set << @seqs[i]
      end
      i+=1
    end
    puts "The training/test set has #{tt_set.size} sequences!"
    puts "The hold-out set has #{ho_set.size} sequences!"
    #write hold-out set into jobfolder/holdoutXX_set.f
    holdout = File.open(@outpath+"holdout#{@seqid}_set.f", 'wb')
    ho_set.each {|seq| holdout.write(seq)}
    holdout.close

    #build the @xfold split set files
    n = (tt_set.size).to_f / (@xfold.to_f)
    puts "Each of the #{@xfold} crossvalidation sets will have #{n} sequences!"
    rest = tt_set.size % @xfold.to_f
    puts "#{rest} sequences will be equally distributed among the #{@xfold} sets!"
    n = n.to_i
    cross = []
    i = 0
    while i < @xfold
      j = 0
      cross[i] = []
      while j < n
        index = j+i*n
        cross[i] << tt_set[index]
        j+=1
      end
      if rest > 0 
        cross[i] << tt_set[tt_set.size-1-rest] #its better to have balanced sets if the amount of sequences is not dividable by @xfold, 
        rest = rest-1                          #rather than pushing all of the remaining sequences in the last crossvalidation split set
      end
      i+=1
    end
    #write all the crossvalidation splitsets in the jobfolder
    i = 0
    cross.each do |split|
      i+=1
      puts "Split #{i} has #{split.size} sequences!"
      cv_file = @outpath+"cv_split_#{@seqid}_#{i}.f"
      @cv_files << cv_file
      out = File.open(cv_file,'wb')
      split.each do |seq|
        out.write(seq)
      end
      out.close
    end 
  end
  
  def buildCombinatoricFile
    combfile = File.open(@outpath+"crossvalidation_combinatorics.txt", 'w')
    @cv_files.each_index do |i|
      line = ""
      @cv_files.each_index do |j|
        if i != j  # file i is added as the last one
          line = line+@cv_files[j]+"\t"
        end
      end
      line = line+@cv_files[i]
      combfile.write line+"\n"
    end
    combfile.close
  end   

end


BuildSplitsets.new(ARGV)
