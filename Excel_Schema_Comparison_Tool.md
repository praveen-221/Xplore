
# Excel Schema Comparison Tool

This Excel-based tool allows you to compare table schemas from two different data sources (e.g., SQL Server, Dataverse) by comparing their metadata in two sheets. It highlights any mismatches in `DataType` or `Size` for columns with matching `TableName` and `ColumnName`.

---

## 📄 Sheet Structure

- **Sheet1**: `Source1`
- **Sheet2**: `Source2`
- Columns in each:
  - `TableName` (A)
  - `ColumnName` (B)
  - `DataType` (C)
  - `Size` (D)

---

## 🧾 Comparison Output Sheet

Create a third sheet named: `ComparisonResult`  
Add the following headers in row 1:

| Column | Header        |
|--------|---------------|
| A      | TableName     |
| B      | ColumnName    |
| C      | DataType1     |
| D      | DataType2     |
| E      | Size1         |
| F      | Size2         |
| G      | Mismatch      |

---

## 🔢 How to Populate Values

### A2 & B2
Manually copy or list all unique `(TableName, ColumnName)` pairs you want to compare from `Source1`.

---

### C2 - Get `DataType1` from Source1

```
=VLOOKUP(A2 & "|" & B2, CHOOSE({1,2,3}, Source1!A2:A1000 & "|" & Source1!B2:B1000, Source1!C2:C1000, Source1!D2:D1000), 2, FALSE)
```

---

### D2 - Get `DataType2` from Source2

```
=VLOOKUP(A2 & "|" & B2, CHOOSE({1,2,3}, Source2!A2:A1000 & "|" & Source2!B2:B1000, Source2!C2:C1000, Source2!D2:D1000), 2, FALSE)
```

---

### E2 - Get `Size1` from Source1

```
=VLOOKUP(A2 & "|" & B2, CHOOSE({1,2,3}, Source1!A2:A1000 & "|" & Source1!B2:B1000, Source1!C2:C1000, Source1!D2:D1000), 3, FALSE)
```

---

### F2 - Get `Size2` from Source2

```
=VLOOKUP(A2 & "|" & B2, CHOOSE({1,2,3}, Source2!A2:A1000 & "|" & Source2!B2:B1000, Source2!C2:C1000, Source2!D2:D1000), 3, FALSE)
```

---

### G2 - Show `Mismatch` Status

```
=IF(OR(C2<>D2, E2<>F2), "Yes", "No")
```

---

## 📝 Example Output

| TableName | ColumnName | DataType1 | DataType2 | Size1 | Size2 | Mismatch |
|-----------|------------|-----------|-----------|-------|-------|----------|
| Person    | Name       | nvarchar  | nvarchar  | 256   | 128   | Yes      |
| Account   | Balance    | int       | int       |       |       | No       |
| Address   | Zip        | varchar   | varchar   | 20    | 20    | No       |

---

## ✅ Notes

- You can wrap formulas with `IFERROR(..., "")` to suppress `#N/A` errors.
- You may use Excel Tables and structured references for dynamic ranges.
- Consider using Power Query for large-scale schema diffing or automation.

---

## 📌 License

MIT © 2024 YourName
