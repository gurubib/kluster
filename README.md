# kluster
Project for BCE thesis.

## Aim of the project and thesis

The main goal of the project is to explore, understand, implement and examine various time series clusterisation methods.
In this short document the main corner stones of the steps are listed and described in a bit more detail.

## Data

The first crucial thing is to decide about the data in question.

We will use two main types of data regarding the stocks.

* OHLC stock data on multiple resolution (first dayly)
* Tick-by-tick stock data

The former can be easily obtained freely from Finnhub's API - the first goal is to examine the SNP500 or NASDAQ stocks.

With the latter the free acquisition of the data is a bit more complicated, nevertheless the previously mentioned
API provides a sample TBT dataset (SNP500 futures).

The data will be stored in a time series compatible database.

Firstly there should be generated data for the sake of simplicity.

## Normailization

The aim is to use two main ways of normailzation.

* Use some transformation on the values (e.g. close prices) - like standardization or range fit
* Calculate the change in the data (examine the yield)

## Representation

In order to examine the huge amount of data, the dimension of it must be reduced. There are 
numerous representation methods. Again two main categories.

* Fitting a function (e.g. exponential) or a curve
* Transforming the data point-by-point

## Similarity measure

In order to compare the representations some proper similarity measures must be defined. These
must have necessary properties (like symmetry, etc.)

## Clustering algorithm

Firstly there will be used some popular algorithms, in order to compare the results.

## Questions?

Are there _good_ clusters?
Are there _huge_ differences between the results of the algorithms?
Can the trend and the difference from it be measured?
