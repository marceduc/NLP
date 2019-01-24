import sys

def main():

    GOLD_STANDARD = sys.argv[1]
    PREDICTION_FILE = sys.argv[2]

    # read true labels, tokens and predictions

    with open(GOLD_STANDARD, 'r') as f:
        lines = f.readlines()

    lines = [line.strip() for line in lines]
    labels = [str.split(line, '\t')[1] if len(line) is not 0 else str.split(line, '\t')[0] for line in lines]
    token = [str.split(line, '\t')[0] for line in lines]

    with open(PREDICTION_FILE, 'r') as f:
        lines = f.readlines()

    lines = [line.strip() for line in lines]
    preds = [str.split(line, '\t')[1] if len(line) is not 0 else str.split(line, '\t')[0] for line in lines]

    # create dictionary of TP,FP, TN,TP with tokens and ids

    TN = {}
    FP = {}

    TP = {}
    FN = {}

    for i in range(len(labels)):

        if labels[i] == '':
            if preds[i] == '':
                next
            else:
                print("non matching empty line at row " + str(i))
                break
        elif labels[i] == 'O':
            if labels[i] == preds[i]:
                if token[i] in TN:
                    TN.get(token[i]).append(i)
                else:
                    TN[token[i]] = [i]
            else:
                if token[i] in FP:
                    FP.get(token[i]).append(i)
                else:
                    FP[token[i]] = [i]

        elif labels[i] == 'B-protein':
            if labels[i] == preds[i]:
                if token[i] in TP:
                    TP.get(token[i]).append(i)
                else:
                    TP[token[i]] = [i]
            else:
                if token[i] in FN:
                    FN.get(token[i]).append(i)
                else:
                    FN[token[i]] = [i]


    # count class lengths
    TP_count = sum([len(value) for key, value in TP.items()])
    FP_count = sum([len(value) for key, value in FP.items()])

    TN_count = sum([len(value) for key, value in TN.items()])
    FN_count = sum([len(value) for key, value in FN.items()])

    # calculate results

    precision = TP_count / (TP_count + FP_count)
    recall = TP_count / (TP_count + FN_count)

    F1 = 2 * precision * recall / (precision + recall)

    # print results

    for key, value in FN.items():
        line = [key]
        ids = [str(i) for i in value]
        line.extend(ids)
        print("FP   (" + ', '.join(line) + ')')

    print('\n')

    for key, value in FN.items():
        line = [key]
        ids = [str(i) for i in value]
        line.extend(ids)
        print("FN   (" + ', '.join(line) + ')')

    print('\n')
    print('True Positives     ' + str(TP_count))
    print('False Positives     ' + str(FP_count))
    print('False Negatives     ' + str(FN_count))
    print('Precision     ' + str(precision))
    print('Recall     ' + str(recall))
    print('F1 Score     ' + str(F1))

main()
