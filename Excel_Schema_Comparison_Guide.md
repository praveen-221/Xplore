
# 📊 Excel Formula Guide: Schema Comparison Between Two Data Sources

This guide helps you compare two Excel sheets (`Sheet1` and `Sheet2`) that contain metadata of SQL tables and columns, and detect:
- DataType mismatches
- Size mismatches
- Missing columns

---

## 📄 Input Sheet Format

Each sheet has the following columns:

| Column        | Description                      |
|---------------|----------------------------------|
| TableName     | Name of the table                |
| ColumnName    | Name of the column               |
| DataType      | Data type of the column          |
| Size          | Size of the data type if applicable |

---

## 🧾 Output Sheet Structure

| TableName | ColumnName | DS1 DataType | DS2 DataType | DS1 Size | DS2 Size | Mismatch |
|-----------|------------|--------------|--------------|----------|----------|----------|

---

## ✅ Excel Formulas

Assuming you are working in the output sheet and copying from `Sheet1`:

### 🔹 A2: TableName
```
=Sheet1!A2
```

### 🔹 B2: ColumnName
```
=Sheet1!B2
```

### 🔹 C2: DS1 DataType
```
=Sheet1!C2
```

### 🔹 D2: DS2 DataType
```
=IF(COUNTIFS(Sheet2!A:A,A2,Sheet2!B:B,B2)=0,"Not Found", INDEX(Sheet2!C:C, MATCH(1, (Sheet2!A:A=A2)*(Sheet2!B:B=B2), 0)))
```

### 🔹 E2: DS1 Size
```
=Sheet1!D2
```

### 🔹 F2: DS2 Size
```
=IF(COUNTIFS(Sheet2!A:A,A2,Sheet2!B:B,B2)=0,"Not Found", INDEX(Sheet2!D:D, MATCH(1, (Sheet2!A:A=A2)*(Sheet2!B:B=B2), 0)))
```

> ⚠️ Note: Press `Ctrl + Shift + Enter` for array formulas unless using Excel 365/2021.

### 🔹 G2: Mismatch?
```
=IF(D2="Not Found","Missing in DS2",IF(AND(C2=D2,D2<>"",E2=F2),"No","Yes"))
```

---

## 🧪 Sample Output

| TableName | ColumnName | DS1 DataType | DS2 DataType | DS1 Size | DS2 Size | Mismatch         |
|-----------|------------|--------------|--------------|----------|----------|------------------|
| person    | name       | nvarchar     | nvarchar     | 256      | 128      | Yes              |
| person    | ssn        | int          | int          |          |          | No               |
| person    | email      | nvarchar     | Not Found    | 100      | Not Found| Missing in DS2   |

---

## 🛠️ Summary

- Use `COUNTIFS` to check existence.
- Use `INDEX` + `MATCH` to fetch matching values.
- Use a formula in `Mismatch` to identify issues.

