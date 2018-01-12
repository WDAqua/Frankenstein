import random

from sklearn.datasets import load_boston
from sklearn import metrics, cross_validation
boston = load_boston()
import sklearn as sk
from sklearn.linear_model import LogisticRegression
from sklearn.linear_model import Ridge
from sklearn.linear_model import Lasso
from sklearn.linear_model import ElasticNet
from sklearn.metrics import r2_score
from sklearn.metrics import mean_squared_error
from sklearn import preprocessing, utils
from sklearn.naive_bayes import GaussianNB
from sklearn.tree import DecisionTreeClassifier
from sklearn.cross_validation import KFold
from sklearn import svm
import csv
import numpy as np
import math
import sklearn


def errorcalc(target, result):
    # return math.sqrt(mean_squared_error(target, result))
    count = 0
    count1 = 0
    count0 = 0
    counter = 0
    counter0 = 0
    for i in range(len(target)):
        if result[i][1]>=0.6 and target[i] == 1:
            count1 += 1
        elif result[i][1]>=0.6 and target[i] == 0:
            counter += 1
        elif result[i][1] < 0.6 and target[i] == 0:
            count0 += 1
        elif result[i][1] < 0.6 and target[i] == 1:
            counter0 += 1
    print("Predicted Positive: ", count1, counter)
    print("Predicted Negative: ", count0, counter0)
    counter = 0
    counter0 = 0
    for i in range(len(target)):
        if target[i] == 1:
            counter += 1
        else:
            counter0 += 1
    count = count1 + count0
    # print("1 corrects with target: ", count1, counter)
    # print("0 corrects with target: ", count0, counter0)
    return count / len(target)


def r2value(target, result):
    return r2_score(target, result)
    # error=0
    # result = np.array(result)
    # # result =result.astype(np.float)
    # for i in range(len(result)):
    #     # test = float(result[i]) if result[i] !="" else 0
    #     # test2 = float(target[i][0]) if target[i][0] != "" else 0
    #
    #     temp = (result[i] - target[i])
    #     error += temp * temp
    # return math.sqrt(error)


def LogisticRegressionMethod2(features, encoded):
    round = []
    cv = cross_validation.KFold(len(features), 10, shuffle=False)
    for train, test in cv:
        train_input = features[train]
        train_output = encoded[train]
        test_input = features[test]
        test_output = encoded[test]
        classifier = LogisticRegression(random_state=0, C=1000)
        classifier.fit(train_input, train_output)
        temp = classifier.predict_proba(test_input)
        round.append(classifier.predict_proba(test_input))
    return round


    return cross_validation.cross_val_predict(LogisticRegression(), features, encoded, cv=5)
    print(metrics.accuracy_score(encoded, predicted))
    print(metrics.classification_report(encoded, predicted))
    classifier = LogisticRegression(random_state=0,C=1000)
    classifier.fit(features[split:], encoded[split:])
    return classifier.predict_proba(np.array(features[:split], dtype='float64'))


def LogisticRegressionMethod(features, encoded):
    classifier = LogisticRegression(random_state=0,C=1000)
    classifier.fit(features[split:], encoded[split:])
    return classifier.predict_proba(np.array(features[:split], dtype='float64'))


def SVMMethod(features, encoded):
    classifier = svm.NuSVC(probability=True)
    classifier.fit(features[split:], encoded[split:])
    return classifier.predict_proba(np.array(features[:split], dtype='float64'))


def DecisionTreeMethod(features, encoded):
    classifier = DecisionTreeClassifier(random_state=0)
    classifier.fit(features[:split], encoded[:split])
    return classifier.predict_proba(np.array(features[split:], dtype='float64'))


def NaiveBayesMethod(features, encoded):
    classifier = GaussianNB()
    classifier.fit(features[:split], encoded[:split])
    return classifier.predict(np.array(features[split:], dtype='float64'))


def LinearRegressionMethod(features, target):
    classifier = sklearn.linear_model.LinearRegression()
    classifier.fit(features[:split], target[:split])
    return classifier.predict(np.array(features[split:], dtype='float64'))


def RidgeRegressionMethod(features, target):
    classifier = sklearn.linear_model.Ridge()
    classifier.fit(features[:split], target[:split])
    return classifier.predict(np.array(features[split:], dtype='float64'))


def LassoRegressionMethod(features, target):
    classifier = sklearn.linear_model.Lasso()
    classifier.fit(features[:split], target[:split])
    return classifier.predict(np.array(features[split:], dtype='float64'))


def ElasticRegressionMethod(features, target):
    classifier = sklearn.linear_model.ElasticNet()
    classifier.fit(features[:split], target[:split])
    return classifier.predict(np.array(features[split:], dtype='float64'))

def TotalAnalysis(main_results, target_vals):
    count = 0
    tagme = 0
    totalAnswers = 0
    indvidAccuracy  = []
    anotherwayofcounting = 0
    indvidAccuracy = IndAccMethod(indvidAccuracy, target_vals)
    precision = [0.66, 0.87, 0.63, 0.41, 0.46, 0.68, 0.53, 0.66, 0.62, 0.54, 0.43, 0.38, 0.03, 0.78, 0.44, 0.44, 0.66, 0.54]
    # precision = [0.40, 0.66 ,0.43 ,0.13, 0.19, 0.49, 0.15, 0.37, 0.41, 0.31, 0.21, 0.10, 0.006, 0.46, 0.26, 0.103, 0.43, 0.31]

    precision = [1 for i in precision]
    for i in range(len(target_vals[0])):
        max_val = 0
        max_index = 0
        flag = 0
        for ind in range(len(main_results)):
            if max_val < precision[ind]*main_results[ind][i][1]:
                max_val = precision[ind]*main_results[ind][i][1]
                max_index = ind
            if flag == 0 and target_vals[ind][i] == 1:
                totalAnswers += 1
                flag = 1

        print(max_index, max_val)

        max_val2 = 0
        max_index2 = 0
        for ind in range(len(main_results)):
            if max_val2 < precision[ind]*main_results[ind][i][1] and precision[ind]*main_results[ind][i][1] != max_val:
                max_val2 = precision[ind]*main_results[ind][i][1]
                max_index2 = ind

        max_val3 = 0
        max_index3 = 0
        for ind in range(len(main_results)):
            if max_val3 < precision[ind] * main_results[ind][i][1] and precision[ind] * main_results[ind][i][
                1] != max_val and precision[ind] * main_results[ind][i][1] != max_val2:
                max_val3 = precision[ind] * main_results[ind][i][1]
                max_index3 = ind

        val = 0

        if (target_vals[max_index][i] == 1):
            val +=1
        if (target_vals[max_index2][i] == 1):
            val +=1
        if (target_vals[max_index3][i] == 1):
            val +=1
        if val>=2:
            anotherwayofcounting +=1

        if (target_vals[max_index][i] == 1):
            count +=1
            if max_index == 1:
                tagme += 1

        print(max_index2, max_val2)
        print(max_index3, max_val3)
        print("")
        addval = ind
        rel_max_val = 0
        rel_max_index = 0
        # for ind in range(7):
        #     if max_val < main_results[addval+ind][i][1]:
        #         max_val = main_results[addval+ind][i][1]
        #         max_index = ind
        #     if flag == 0 and target_vals[addval+ind][i] == 1:
        #         totalAnswers += 1
        #         flag = 1
        #
        # # print(max_index, max_val)
        #
        # max_val2 = 0
        # max_index2 = 0
        # for ind in range(len(main_results)-7):
        #     if max_val2 < main_results[ind][i][1] and main_results[ind][i][1] != max_val:
        #         max_val2 = main_results[ind][i][1]
        #         max_index2 = ind
        # if max_val > 0.5 and (target_vals[max_index][i] == 1 or target_vals[max_index2][i] == 1):
        #     count +=1
        # pass
    pass


def IndAccMethod(indvidAccuracy, target_vals):
    for i in range(len(target_vals)):
        tempCount = 0
        for ind in range(len(target_vals[i])):
            if target_vals[i][ind] == 1:
                tempCount += 1
        indvidAccuracy.append(tempCount)
    return indvidAccuracy


###################### Data Input ########################
# Features File
f = open('questionFeature_new.csv')

input_file = csv.reader(f)
features = []
question_nums = []
for row in input_file:
    line = np.array(row[1:]).astype(np.float)
    question_nums.append(row[0])
    features.append(line)
features = np.array(features[:], dtype='float64')



# Target File
f_t = open('fscore.csv')
target = []
target_values = csv.reader(f_t)
for row in target_values:
    line = np.array(row[1:]).astype(np.float)
    target.append(line)
target = np.array(target[:], dtype='float64')

# combined = list(zip(features, target))
# random.shuffle(combined)
#
# features[:], target[:] = zip(*combined)

###################### End of Input ########################

split = int(len(features) * 0.2)



copy_of_target = np.empty_like(target)
np.copyto(copy_of_target,target)

# Target Modification
for i in range(len(target)):
    for j in range(len(target[i])):
        if target[i][j] >= 0.8:
            target[i][j] = 1
        else:
            target[i][j] = 0

main_array = []
for j in range(len(target[0])):
    temp = []
    for i in range(len(target)):
        temp.append(target[i][j])
    main_array.append(np.array(temp))


results1 = []
test_target = []
for i in range(len(main_array)):
    result = LogisticRegressionMethod(features, main_array[i])
    # result = SVMMethod(features,main_array[i])
    # result =LogisticRegressionMethod2(features,main_array[i])
    logError = errorcalc(main_array[i][:split], result)
    print("LogisticRegression: ", i, logError)
    results1.append(result)
    test_target.append(main_array[i][:split])

TotalAnalysis(results1, test_target)

results = []



pass

