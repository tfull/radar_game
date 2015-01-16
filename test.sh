#!/bin/sh

python input.py | java game/CLIClient 157.82.7.148 3007 player1 > result1.txt &
python input.py | java game/CLIClient 157.82.7.148 3007 player2 
