# Dataset

The original dataset used in this project is a **2017 Kocaeli retail sales dataset** provided by course instructors for academic use.

The dataset is used for a multiclass classification task in which the goal is to predict the **product category** associated with a retail transaction.

Because the public redistribution license of the original coursework dataset is unknown, the full CSV file is **not distributed with this repository**.

---

## Local Setup

To run the project with the original dataset, place your local copy in the repository root with the following filename:

```text
MarketSalesKocaeli.csv
```

The application expects this filename by default.

A different compatible CSV file can also be selected manually through the Java Swing interface.

---

## Features Used

The machine-learning pipeline uses the following fields from the dataset:

- Customer / client identifier
- Gender
- Transaction net amount
- Brand
- Product category

The models use the following predictive features:

```text
Gender
Transaction Net Amount
Brand
```

The target variable is:

```text
Product Category
```

---

## Preprocessing

The current version of the project applies the following preprocessing workflow:

```text
CSV Data
   ↓
Parsing
   ↓
Missing / Invalid Record Filtering
   ↓
Deterministic Train/Test Split
   ↓
Fit Preprocessor on Training Data Only
   ↓
Min-Max Normalization of Transaction Amount
   ↓
Model Training and Evaluation
```

Normalization parameters are calculated only from the training dataset in order to prevent test-set information from leaking into preprocessing.

Brand values are preserved as categorical strings rather than being converted into arbitrary numerical identifiers.

---

## Dataset Availability

The original dataset was supplied as part of university coursework.

Its public redistribution terms are not known, so this repository intentionally excludes the full dataset.

This keeps the source code and project methodology publicly available without assuming redistribution rights for the original data.

---

## Expected File

After obtaining an authorized copy of the dataset, the local project structure should look similar to:

```text
Java-ML-Classification-From-Scratch/
│
├── MarketSalesKocaeli.csv
├── src/
├── data/
│   └── README.md
├── docs/
├── .gitignore
└── README.md
```

`MarketSalesKocaeli.csv` is ignored by Git and should remain local.