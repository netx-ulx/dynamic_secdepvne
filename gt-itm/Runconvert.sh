#!/bin/bash

# sgb2alt <gb input> <alt output>

fileType="-*.gb"

for f in ../gt-itm/graphs/gb_files/*
do

   filename=$f
   filename=${filename##*/}
   filename=${filename%$fileType}

   ../gt-itm/tools/sgb2alt $f ../gt-itm/graphs/alt_files/random/$filename.alt

done
