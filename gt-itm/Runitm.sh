#!/bin/bash

rm -f ../gt-itm/graphs/input_specs/*~
for f in ../gt-itm/graphs/input_specs/*
do
    echo $f
   ../gt-itm/tools/itm $f
done

mv ../gt-itm/graphs/input_specs/*.gb ../gt-itm/graphs/gb_files/
