#!/bin/sh
mkdir -p out
javac -encoding UTF-8 -cp "lib/*" -d out $(find src -name '*.java') && echo "Compilado com sucesso. Execute ./executar.sh"
