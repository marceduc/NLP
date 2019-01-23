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
    
    # load classification data
    test = pd.read_csv(INPUT_FILE, sep = '\t',header=None, names= ['token', 'label'], skip_blank_lines=False)
    test['lower'] = test.token.str.lower()
    
    # make predictions
    preds = []

    for i in range(len(test)):
        if test.lower[i] != test.lower[i]:
            preds.append(np.nan)
        elif test.lower.iloc[i] in stopwords:
            preds.append('O')
        elif test.lower.iloc[i] in genes:
            preds.append('B-protein')
        else:
            preds.append('O')
    
    pred_df = pd.DataFrame({'token':test.token.values, 'preds':preds})
    pred_df = pred_df.fillna('')
    pred_df.to_csv(OUTPUT_FILE, sep = '\t', header = None, index=None)
    
main()
    
