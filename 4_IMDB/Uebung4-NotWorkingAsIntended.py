import sys
import os
import pandas as pd
import numpy as np

from keras.models import Sequential, load_model
from keras.layers import Dense, Embedding,Bidirectional,GRU

from keras.preprocessing.text import Tokenizer
from keras.preprocessing import sequence
from sklearn.model_selection import KFold

import re
from nltk.stem import WordNetLemmatizer
from nltk.corpus import stopwords
import nltk
import pickle

def main():
    modus = sys.argv[1]

    input2 = sys.argv[2]
    input3 = sys.argv[3]

    printer(modus, input2, input3)


    #start actual code

    #add train
    if modus == 'train':

        MODEL_NAME = sys.argv[2]
        TOKENIZER_NAME = 'tokenizer.pickle'

        TRAIN_PATH = sys.argv[3]

        #input params
        MAX_VOCAB = 10000
        SEQ_LEN = 70

        #model params
        embedding_size= 100
        h_size = 32

        #train params
        batch_size = 128
        epochs = 1

        #read data
        print("loading data")
        df = pd.read_table(TRAIN_PATH, header = None,names = ['id', 'y','text'])

        ##training
        reviews_trn = df['text']
        y_trn = (df.y.values == 'pos').astype('int')

        #preprocess and tokenize training data
        print("preprocessing")
        X_trn = preprocess_reviews(reviews_trn)

        tokenizer = Tokenizer(num_words = MAX_VOCAB)
        tokenizer.fit_on_texts(X_trn)
        # save Tokenizer
        with open(TOKENIZER_NAME, 'wb') as handle:
            pickle.dump(tokenizer, handle, protocol=pickle.HIGHEST_PROTOCOL)

        X_trn = tokenizer.texts_to_sequences(X_trn)

        #add observations by dividing reviews at SEQ_LEN in sub sequences
        y_long = np.empty([0,1], int)
        long_sequences  = np.empty([0,SEQ_LEN], int)


        for s, y in zip(X_trn, y_trn):
          s, y, _ = split_seq(s, y, SEQ_LEN)
          y_long = np.vstack([y_long, y])
          long_sequences = np.vstack([long_sequences, s])

        X_trn = long_sequences
        y_trn = y_long

        #construct model
        model=Sequential()
        model.add(Embedding(MAX_VOCAB, embedding_size, input_length=SEQ_LEN))
        model.add(Bidirectional(GRU(h_size, input_shape = (SEQ_LEN,embedding_size))))
        model.add(Dense(1, activation='sigmoid'))
        model.compile(loss='binary_crossentropy', optimizer='adam', metrics=['accuracy'])

        #train model
        model.fit(X_trn,y_trn, batch_size=batch_size, epochs=epochs)
        #save model
        model.save(MODEL_NAME)  # creates a HDF5 File MODELNAME
        print("Model has been saved as " + MODEL_NAME)


    #add predict
    elif modus == 'classify':

        MODEL_NAME = sys.argv[2]
        TOKENIZER_NAME = 'tokenizer.pickle'

        PREDICT_PATH = sys.argv[3]
        OUTPUT_FILE = sys.argv[4]

        #to do dont double assigne this, can be extracted from model?
        SEQ_LEN = 70

        print("loading data")
        df = pd.read_table(PREDICT_PATH, header = None,names = ['id', 'y','text'])

        print("loading tokenizer")
        #load tokenizer
        with open(TOKENIZER_NAME, 'rb') as handle:
            tokenizer = pickle.load(handle)
        #load model
        print("loading" + MODEL_NAME)
        model = load_model(MODEL_NAME)

        reviews_val = df['text']

        print("preprocessing")
        X_val = preprocess_reviews(reviews_val)
        X_val = tokenizer.texts_to_sequences(X_val)

        #prepare validation data
        len_ar = []
        X_long  = np.empty([0,SEQ_LEN], int)

        for s in X_val:
            s, _, l = split_seq(s, 0, SEQ_LEN)
            X_long = np.vstack([X_long, s])
            len_ar.append(l)

        len_ar = np.array(len_ar)

        print("starting prediction")
        # make predictions
        pred = model.predict(X_long)
        predictions = []
        idx = 0
        for i in range(len_ar.shape[0]):
          y_hat = pred[idx: (idx+len_ar[i])].mean().round()
          predictions.append(y_hat)
          idx = idx + len_ar[i]

        predictions = np.array(predictions)
        output = pd.DataFrame(predictions, index = df['id'], columns = ['y_hat'])
        output.replace([1.0,0.0], ['pos', 'neg'], inplace = True)
        output.to_csv(OUTPUT_FILE, sep = '\t')
        print("Predictions saved to " + OUTPUT_FILE)

    else:
        print('Incorrect mode, please choose train or classify as 1st argument')
        print('Your arguments:')
        printer(sys.argv[2],sys.argv[3], sys.argv[4])

def printer(input1, input2, input3):
    print(input1 + ' - ' + input2 + ' - ' + input3)

def preprocess_reviews(reviews):
  # delete or replace special chars with whith spaces or End Of Sentence identifiers
  # remove stopwords and lemmanize
  # turn review strings into word list
  nltk.download('stopwords')
  nltk.download('wordnet')

  REPLACE_NO_SPACE = re.compile("(\;)|(\:)|(\')|(\,)|(\")|(\()|(\))|(\[)|(\])")
  REPLACE_WITH_EOS = re.compile("(\.)|(\!)|(\?)")
  REPLACE_WITH_SPACE = re.compile("(<br\s*/><br\s*/>)|(\-)|(\/)")


  reviews = [REPLACE_NO_SPACE.sub("", line.lower()) for line in reviews]
  reviews = [REPLACE_WITH_EOS.sub(" EOS ", line) for line in reviews]
  reviews = [REPLACE_WITH_SPACE.sub(" ", line) for line in reviews]

  stop_words = set(stopwords.words("english"))
  lemmatizer = WordNetLemmatizer()

  for i in range(len(reviews)):
    review = reviews[i]

    #lemmanize
    review = [lemmatizer.lemmatize(token) for token in review.split(" ")]
    review = [lemmatizer.lemmatize(token, "v") for token in review]

    #remove stopwords
    review = [word for word in review if not word in stop_words]
    review = " ".join(review)

    reviews[i] = review

  return reviews

def split_seq(s, y, SEQ_LEN):
  #creates for
  s_extend = sequence.pad_sequences([s[i:i+SEQ_LEN]  for i in range(0,len(s), SEQ_LEN)],  maxlen = SEQ_LEN)
  y_extend = np.repeat(y,s_extend.shape[0]).reshape(-1,1)
  l = s_extend.shape[0]

  return(s_extend, y_extend, l)

main()
