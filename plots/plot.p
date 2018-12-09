set terminal postscript enhanced color "Helvetica" 8
set terminal pngcairo dashed
set output "AcceptanceRatio_NoReconfig.png"
set xlabel "Time"
set ylabel "VN request acceptance ratio"
set xrange [0:50000]
set yrange [0.01:1.01]
set key on right bottom
plot 'DynamicExp1.dat' using 1:6  with lines linewidth 2 lc 1 title 'BW', 'DynamicExp2.dat' using 1:6  with lines linewidth 2 lc 2 title 'U', \
'DynamicExp3.dat' using 1:6  with lines linewidth 2 lc 3 title 'FG', 'DynamicExp4.dat' using 1:6  with lines linewidth 2 lc 4 title 'FGSec'
######
set terminal postscript enhanced color "Helvetica" 8
set terminal png
set terminal pngcairo dashed
set output "AverageTimeRevenue_NoReconfig.png"
set xlabel "Time"
set ylabel "Average Revenue"
set xrange [0:50000]
set yrange [0:10]
plot 'DynamicExp1.dat' using 1:2  with lines linewidth 2 lc 1 title 'BW', 'DynamicExp2.dat' using 1:2  with lines linewidth 2 lc 2 title 'U', \
'DynamicExp3.dat' using 1:2  with lines linewidth 2 lc 3 title 'FG', 'DynamicExp4.dat' using 1:2  with lines linewidth 2 lc 4 title 'FGSec'
######
set terminal postscript enhanced color "Helvetica" 8
set terminal png
set terminal pngcairo dashed
set output "AverageCost_NoReconfig.png"
set xlabel "Time"
set ylabel "Average Cost"
set xrange [0:50000]
set yrange [40:750]
plot 'DynamicExp1.dat' using 1:3  with lines linewidth 2 lc 1 title 'BW', 'DynamicExp2.dat' using 1:3  with lines linewidth 2 lc 2 title 'U', \
'DynamicExp3.dat' using 1:3  with lines linewidth 2 lc 3 title 'FG', 'DynamicExp4.dat' using 1:3  with lines linewidth 2 lc 4 title 'FGSec'
######
set terminal postscript enhanced color "Helvetica" 8
set terminal png
set terminal pngcairo dashed
set output "AverageNodeUtilization_NoReconfig.png"
set xlabel "Time"
set ylabel "Node stress ratio"
set xrange [0:50000]
set yrange [0.01:1.05]
set key on right bottom
plot 'DynamicExp1.dat' using 1:4  with lines linewidth 2 lc 1 title 'BW', 'DynamicExp2.dat' using 1:4  with lines linewidth 2 lc 2 title 'U', \
'DynamicExp3.dat' using 1:4  with lines linewidth 2 lc 3 title 'FG', 'DynamicExp4.dat' using 1:4  with lines linewidth 2 lc 4 title 'FGSec'
######
set terminal png
set terminal pngcairo dashed
set output "AverageLinkUtilization_NoReconfig.png"
set xlabel "Time"
set ylabel "Link stress ratio"
set key on right top
set yrange [0.0:0.50]
set xrange [0:50000]
plot 'DynamicExp1.dat' using 1:5  with lines linewidth 2 lc 1 title 'BW', 'DynamicExp2.dat' using 1:5  with lines linewidth 2 lc 2 title 'U', \
'DynamicExp3.dat' using 1:5  with lines linewidth 2 lc 3 title 'FG', 'DynamicExp4.dat' using 1:5  with lines linewidth 2 lc 4 title 'FGSec'