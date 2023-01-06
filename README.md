# buffer-splaynet

The structore for my thesis project was as follows: 
the main folder contained this project, a python 
project and the results folder. The python project created
datasets, plots and the complexity map. Therefore the 
results are stored in a higher folder. The start the 
experiment run the Main of MainBufferSplayNet.java. In
the terminal the settings for the experiment are requested.
It is possible to run the experiment normally to test one csv
trace of input an own trace or start the experiment as simulation.
The simulation setting is used for experimental evaluation. The 
normal setting was also used to verify the correctness of the
code and test own traces. For the experiments in my thesis the simulation
setting was used. This setting reads all files from the csvExperiment
folder and starts an experiment for each trace and buffer sizes.
Several questions are asked in the console regarding the parameters for the experiment.
If you want to test custom buffer sizes or traces, after you answered with "y" 
you need to input them seperated by a space. Answer yes or no questions with y/n.
It is also possible to track timestamp for each served request. This enables
to analyze the delay of requests for the distance algorithm. If other SPs
should be included, you need to change them in the code line 418 in MainBufferSplayNet.
Depending on which algorithm you want to evaluate, you need to comment the others out (line 180).
If you want to log the order of the served trace, you need to enable in line 17 monitoreTraceOrder.

Each Algorithm Id represents following algorithm:
- 0 = Regrouping
- 1 = Distance
- 2 = Cluster + Distance
- 3 = Cluster + Frequency
- 4 = Cluster + Sequential 

To optimally run the experiment with possible paritions of clusters,
you need to install the following python packages on your engine:
- community
- networkx
- numpy





