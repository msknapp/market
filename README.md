
To build:

mvn clean install

To run:
make a directory somewhere on your path,
copy in the jar found in target/market-1.jar,

have the run script in it just do:

java -jar market.jar

you could provide the full path to that jar to be safe.

Add an alias to your $HOME/.bashrc 

alias market=$HOME/bin/market-analysis/run.sh

make sure that the run script is executable.

now you should just be able to type "market" 
from the command line, then cd to the output directory, 
and view the files for some investment advice.
