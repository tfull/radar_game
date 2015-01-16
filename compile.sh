cd src
fs="game/Server.java game/GUIClient.java game/CLIClient.java"
for f in $fs
do
    javac -J-Dfile.encoding=utf-8 -d ../bin $f
done
