import numpy as np
import pandas as pd
import sys

def main():
    INPUT_FILE = sys.argv[1]
    OUTPUT_FILE = sys.argv[2]
    STOP_WORDS_FILE = 'english_stop_words.txt'
    DICT_FILE = 'genenames_extended.txt'

    #load gene dict, and stopwords
    stopwords = pd.read_csv(STOP_WORDS_FILE, sep = '\t', names= ['stopword'])
    genenames = pd.read_csv(DICT_FILE, sep = '\t', names= ['GenName'])

    #create set of lower case entries
    genes = set(genenames.GenName.str.lower().unique())
    stopwords = set(stopwords.stopword.str.lower().unique())

    with open(INPUT_FILE, 'r') as f:
        lines = f.readlines()

    lines = [line.strip() for line in lines]
    token = [str.split(line, '\t')[0] for line in lines]


    f = open(OUTPUT_FILE, "w")
    for tok in token:
        if(tok == ''):
            f.write('\n')
        elif(tok.lower() in stopwords):
            f.write(tok + '\t' + 'O' + '\n')
        elif(tok.lower() in genes):
            f.write(tok + '\t' + 'B-protein' +'\n')
        else:
            f.write(tok + '\t' + 'O' + '\n')
    f.close()

main()
