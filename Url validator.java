// =MID(A1, FIND("]", A1, FIND("]", A1) + 1) + 2, FIND("]", A1 & "]", FIND("]", A1, FIND("]", A1) + 1) + 1) - FIND("]", A1, FIND("]", A1) + 1) - 2)

import java.util.regex.*;
import java.util.Scanner;

public class DBUrlValidator {

    // Regular expression patterns for different databases
    private static final String MYSQL_PATTERN = "^([a-zA-Z0-9.-]+)(?::(\\d+))?/(\\w+)$";
    private static final String POSTGRES_PATTERN = "^([a-zA-Z0-9.-]+)(?::(\\d+))?/(\\w+)$";
    private static final String ORACLE_PATTERN = "^([a-zA-Z0-9.-]+)(?::(\\d+))?/([a-zA-Z0-9._]+)$";
    private static final String MSSQL_PATTERN = "^([a-zA-Z0-9.-]+)(?::(\\d+))?\\/(\\w+)$";
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter the database URL (MySQL/PostgreSQL/Oracle/MSSQL): ");
        String dbUrl = scanner.nextLine();

        String validatedUrl = null;

        // Try MySQL format first
        validatedUrl = validateAndConvertMySQL(dbUrl);
        if (validatedUrl != null) {
            System.out.println("Validated MySQL URL: " + validatedUrl);
        } else {
            // Try PostgreSQL format
            validatedUrl = validateAndConvertPostgres(dbUrl);
            if (validatedUrl != null) {
                System.out.println("Validated PostgreSQL URL: " + validatedUrl);
            } else {
                // Try Oracle format
                validatedUrl = validateAndConvertOracle(dbUrl);
                if (validatedUrl != null) {
                    System.out.println("Validated Oracle URL: " + validatedUrl);
                } else {
                    // Try MSSQL format
                    validatedUrl = validateAndConvertMSSQL(dbUrl);
                    if (validatedUrl != null) {
                        System.out.println("Validated MSSQL URL: " + validatedUrl);
                    } else {
                        System.out.println("Invalid database URL format.");
                    }
                }
            }
        }

        scanner.close();
    }

    // Method to validate and convert MySQL URL
    private static String validateAndConvertMySQL(String dbUrl) {
        Pattern pattern = Pattern.compile(MYSQL_PATTERN);
        Matcher matcher = pattern.matcher(dbUrl);

        if (matcher.matches()) {
            String hostname = matcher.group(1);
            String port = matcher.group(2) != null ? matcher.group(2) : "3306"; // Default MySQL port
            String dbName = matcher.group(3);

            // Convert to standard format if necessary
            return "jdbc:mysql://" + hostname + ":" + port + "/" + dbName;
        }

        return null; // Invalid MySQL format
    }

    // Method to validate and convert PostgreSQL URL
    private static String validateAndConvertPostgres(String dbUrl) {
        Pattern pattern = Pattern.compile(POSTGRES_PATTERN);
        Matcher matcher = pattern.matcher(dbUrl);

        if (matcher.matches()) {
            String hostname = matcher.group(1);
            String port = matcher.group(2) != null ? matcher.group(2) : "5432"; // Default PostgreSQL port
            String dbName = matcher.group(3);

            // Convert to standard format if necessary
            return "jdbc:postgresql://" + hostname + ":" + port + "/" + dbName;
        }

        return null; // Invalid PostgreSQL format
    }

    // Method to validate and convert Oracle URL
    private static String validateAndConvertOracle(String dbUrl) {
        Pattern pattern = Pattern.compile(ORACLE_PATTERN);
        Matcher matcher = pattern.matcher(dbUrl);

        if (matcher.matches()) {
            String hostname = matcher.group(1);
            String port = matcher.group(2) != null ? matcher.group(2) : "1521"; // Default Oracle port
            String serviceName = matcher.group(3);

            // Convert to standard format if necessary
            return "jdbc:oracle:thin:@" + hostname + ":" + port + ":" + serviceName;
        }

        return null; // Invalid Oracle format
    }

    // Method to validate and convert MSSQL URL
    private static String validateAndConvertMSSQL(String dbUrl) {
        Pattern pattern = Pattern.compile(MSSQL_PATTERN);
        Matcher matcher = pattern.matcher(dbUrl);

        if (matcher.matches()) {
            String hostname = matcher.group(1);
            String port = matcher.group(2) != null ? matcher.group(2) : "1433"; // Default MSSQL port
            String dbName = matcher.group(3);

            // Convert to standard format if necessary
            return "jdbc:sqlserver://" + hostname + ":" + port + ";databaseName=" + dbName;
        }

        return null; // Invalid MSSQL format
    }
}


---------------
WITH RecursiveDependencies AS (
    -- Step 1: Get Direct Dependencies (Tables, Views, Synonyms, Functions)
    SELECT 
        dep.referenced_id AS ObjectID, 
        obj.name AS ObjectName,
        obj.type_desc AS ObjectType,
        syn.base_object_name AS SynonymBaseObject
    FROM sys.sql_expression_dependencies dep
    INNER JOIN sys.objects obj ON dep.referenced_id = obj.object_id
    LEFT JOIN sys.synonyms syn ON obj.name = syn.name -- Resolving synonyms
    WHERE dep.referencing_id = OBJECT_ID('YourViewOrProcedureName') -- Replace with actual view/procedure name

    UNION ALL

    -- Step 2: Recursively Traverse Views and Synonyms That Are Views
    SELECT 
        dep.referenced_id,
        obj.name,
        obj.type_desc,
        syn.base_object_name
    FROM sys.sql_expression_dependencies dep
    INNER JOIN sys.objects obj ON dep.referenced_id = obj.object_id
    LEFT JOIN sys.synonyms syn ON obj.name = syn.name
    INNER JOIN RecursiveDependencies rd ON dep.referencing_id = rd.ObjectID
    WHERE obj.type_desc IN ('VIEW', 'SYNONYM') -- Keep resolving views and synonyms
)
-- Step 3: Select Only Base Tables and Functions
SELECT DISTINCT 
    rd.ObjectName, 
    rd.ObjectType,
    COALESCE(rd.SynonymBaseObject, 'Direct Reference') AS ResolvedBaseObject
FROM RecursiveDependencies rd
WHERE rd.ObjectType IN ('USER_TABLE', 'SQL_SCALAR_FUNCTION', 'SQL_TABLE_VALUED_FUNCTION')
ORDER BY rd.ObjectName;

----------

// print all the db objects accesssed like views, synonyms, tables etc...
WITH RecursiveDependencies AS (
    -- Step 1: Get Direct Dependencies (Tables, Views, Functions, Synonyms)
    SELECT 
        dep.referenced_id AS ObjectID, 
        obj.name AS ObjectName,
        obj.type_desc AS ObjectType,
        syn.base_object_name AS SynonymBaseObject
    FROM sys.sql_expression_dependencies dep
    INNER JOIN sys.objects obj ON dep.referenced_id = obj.object_id
    LEFT JOIN sys.synonyms syn ON obj.name = syn.name -- Resolving synonyms
    WHERE dep.referencing_id = OBJECT_ID('YourViewOrProcedureName') -- Replace with actual view/procedure name

    UNION ALL

    -- Step 2: Recursively Traverse Views and Synonyms That Are Views
    SELECT 
        dep.referenced_id,
        obj.name,
        obj.type_desc,
        syn.base_object_name
    FROM sys.sql_expression_dependencies dep
    INNER JOIN sys.objects obj ON dep.referenced_id = obj.object_id
    LEFT JOIN sys.synonyms syn ON obj.name = syn.name
    INNER JOIN RecursiveDependencies rd ON dep.referencing_id = rd.ObjectID
    WHERE obj.type_desc IN ('VIEW', 'SYNONYM') -- Keep resolving views and synonyms
)
-- Step 3: Select User Tables, Functions, Views, and Synonyms
SELECT DISTINCT 
    rd.ObjectName, 
    rd.ObjectType,
    COALESCE(rd.SynonymBaseObject, 'Direct Reference') AS ResolvedBaseObject
FROM RecursiveDependencies rd
WHERE rd.ObjectType IN ('USER_TABLE', 'SQL_SCALAR_FUNCTION', 'SQL_TABLE_VALUED_FUNCTION', 'VIEW', 'SYNONYM')
ORDER BY rd.ObjectType, rd.ObjectName;

----------
// V3
DECLARE @ViewOrProcedureName NVARCHAR(255) = 'YourViewOrProcedureName'; -- Replace with your view or procedure name
DECLARE @SQL NVARCHAR(MAX);

WITH RecursiveDependencies AS (
    -- Step 1: Get Direct Dependencies (Tables, Views, Functions, Synonyms)
    SELECT 
        dep.referenced_id AS ObjectID, 
        obj.name AS ObjectName,
        obj.type_desc AS ObjectType,
        syn.base_object_name AS SynonymBaseObject,
        CASE 
            WHEN syn.base_object_name LIKE '%.%' THEN PARSENAME(syn.base_object_name, 3) 
            ELSE DB_NAME() 
        END AS ReferencedDB, -- Extracts DB name if exists
        CASE 
            WHEN syn.base_object_name LIKE '%.%' THEN PARSENAME(syn.base_object_name, 2) 
            ELSE SCHEMA_NAME(obj.schema_id) 
        END AS ReferencedSchema
    FROM sys.sql_expression_dependencies dep
    INNER JOIN sys.objects obj ON dep.referenced_id = obj.object_id
    LEFT JOIN sys.synonyms syn ON obj.name = syn.name  -- Resolving synonyms
    WHERE dep.referencing_id = OBJECT_ID(@ViewOrProcedureName)

    UNION ALL

    -- Step 2: Recursively Traverse Views and Synonyms
    SELECT 
        dep.referenced_id,
        obj.name,
        obj.type_desc,
        syn.base_object_name,
        CASE 
            WHEN syn.base_object_name LIKE '%.%' THEN PARSENAME(syn.base_object_name, 3) 
            ELSE DB_NAME() 
        END AS ReferencedDB, -- Extract DB name
        CASE 
            WHEN syn.base_object_name LIKE '%.%' THEN PARSENAME(syn.base_object_name, 2) 
            ELSE SCHEMA_NAME(obj.schema_id) 
        END AS ReferencedSchema
    FROM sys.sql_expression_dependencies dep
    INNER JOIN sys.objects obj ON dep.referenced_id = obj.object_id
    LEFT JOIN sys.synonyms syn ON obj.name = syn.name
    INNER JOIN RecursiveDependencies rd ON dep.referencing_id = rd.ObjectID
    WHERE obj.type_desc IN ('VIEW', 'SYNONYM') -- Keep resolving views and synonyms
)
-- Step 3: Select Base Tables, Views, Functions, Synonyms (Including External DB References)
SELECT DISTINCT 
    rd.ObjectName, 
    rd.ObjectType,
    COALESCE(rd.SynonymBaseObject, 'Direct Reference') AS ResolvedBaseObject,
    rd.ReferencedDB AS DatabaseName,
    rd.ReferencedSchema AS SchemaName
FROM RecursiveDependencies rd
WHERE rd.ObjectType IN ('USER_TABLE', 'SQL_SCALAR_FUNCTION', 'SQL_TABLE_VALUED_FUNCTION', 'VIEW', 'SYNONYM')
ORDER BY rd.DatabaseName, rd.SchemaName, rd.ObjectName;

----------
// without recursion 
DECLARE @ViewOrProcedureName NVARCHAR(255) = 'YourViewOrProcedureName'; -- Replace with your object name
DECLARE @SQL NVARCHAR(MAX);
DECLARE @ObjectID INT;

-- Table to Store Dependencies
IF OBJECT_ID('tempdb..#Dependencies') IS NOT NULL DROP TABLE #Dependencies;
CREATE TABLE #Dependencies (
    ObjectID INT,
    ObjectName NVARCHAR(255),
    ObjectType NVARCHAR(255),
    SynonymBaseObject NVARCHAR(4000),
    DatabaseName NVARCHAR(255),
    SchemaName NVARCHAR(255),
    Processed BIT DEFAULT 0
);

-- Step 1: Insert Direct Dependencies
INSERT INTO #Dependencies (ObjectID, ObjectName, ObjectType, SynonymBaseObject, DatabaseName, SchemaName)
SELECT 
    dep.referenced_id, 
    obj.name,
    obj.type_desc,
    syn.base_object_name,
    CASE 
        WHEN syn.base_object_name LIKE '%.%' THEN PARSENAME(syn.base_object_name, 3) 
        ELSE DB_NAME() 
    END AS DatabaseName, 
    CASE 
        WHEN syn.base_object_name LIKE '%.%' THEN PARSENAME(syn.base_object_name, 2) 
        ELSE SCHEMA_NAME(obj.schema_id) 
    END AS SchemaName
FROM sys.sql_expression_dependencies dep
INNER JOIN sys.objects obj ON dep.referenced_id = obj.object_id
LEFT JOIN sys.synonyms syn ON obj.name = syn.name
WHERE dep.referencing_id = OBJECT_ID(@ViewOrProcedureName);

-- Step 2: Process Dependencies in a Loop Until No More Views/Synonyms to Process
WHILE EXISTS (SELECT 1 FROM #Dependencies WHERE Processed = 0)
BEGIN
    -- Get Next Unprocessed Object
    SELECT TOP 1 @ObjectID = ObjectID FROM #Dependencies WHERE Processed = 0;

    -- Insert Dependencies of This Object
    INSERT INTO #Dependencies (ObjectID, ObjectName, ObjectType, SynonymBaseObject, DatabaseName, SchemaName)
    SELECT 
        dep.referenced_id, 
        obj.name,
        obj.type_desc,
        syn.base_object_name,
        CASE 
            WHEN syn.base_object_name LIKE '%.%' THEN PARSENAME(syn.base_object_name, 3) 
            ELSE DB_NAME() 
        END AS DatabaseName, 
        CASE 
            WHEN syn.base_object_name LIKE '%.%' THEN PARSENAME(syn.base_object_name, 2) 
            ELSE SCHEMA_NAME(obj.schema_id) 
        END AS SchemaName
    FROM sys.sql_expression_dependencies dep
    INNER JOIN sys.objects obj ON dep.referenced_id = obj.object_id
    LEFT JOIN sys.synonyms syn ON obj.name = syn.name
    WHERE dep.referencing_id = @ObjectID
    AND NOT EXISTS (SELECT 1 FROM #Dependencies WHERE ObjectID = dep.referenced_id);

    -- Mark Processed
    UPDATE #Dependencies SET Processed = 1 WHERE ObjectID = @ObjectID;
END

-- Step 3: Return Final Results
SELECT DISTINCT 
    ObjectName, 
    ObjectType,
    COALESCE(SynonymBaseObject, 'Direct Reference') AS ResolvedBaseObject,
    DatabaseName,
    SchemaName
FROM #Dependencies
WHERE ObjectType IN ('USER_TABLE', 'SQL_SCALAR_FUNCTION', 'SQL_TABLE_VALUED_FUNCTION', 'VIEW', 'SYNONYM')
ORDER BY DatabaseName, SchemaName, ObjectName;

-- Cleanup
DROP TABLE #Dependencies;



 -------
    DECLARE @ViewOrProcedureName NVARCHAR(255) = 'YourViewOrProcedureName'; -- Replace with your object name
DECLARE @SQL NVARCHAR(MAX);
DECLARE @ObjectID INT, @BaseObject NVARCHAR(4000);

-- Table to Store Dependencies
IF OBJECT_ID('tempdb..#Dependencies') IS NOT NULL DROP TABLE #Dependencies;
CREATE TABLE #Dependencies (
    ObjectID INT NULL,
    ObjectName NVARCHAR(255),
    ObjectType NVARCHAR(255),
    SynonymBaseObject NVARCHAR(4000),
    ResolvedBaseObject NVARCHAR(4000),
    Processed BIT DEFAULT 0
);

-- Step 1: Insert Direct Dependencies (Tables, Views, Functions, Synonyms)
INSERT INTO #Dependencies (ObjectID, ObjectName, ObjectType, SynonymBaseObject, ResolvedBaseObject)
SELECT 
    dep.referenced_id, 
    obj.name,
    obj.type_desc,
    syn.base_object_name AS SynonymBaseObject,
    COALESCE(syn.base_object_name, obj.name) AS ResolvedBaseObject
FROM sys.sql_expression_dependencies dep
INNER JOIN sys.objects obj ON dep.referenced_id = obj.object_id
LEFT JOIN sys.synonyms syn ON obj.name = syn.name  -- Resolving synonyms
WHERE dep.referencing_id = OBJECT_ID(@ViewOrProcedureName);

-- Step 2: Process Dependencies in a Loop Until All Base Tables Are Found
WHILE EXISTS (SELECT 1 FROM #Dependencies WHERE Processed = 0)
BEGIN
    -- Get Next Unprocessed Object
    SELECT TOP 1 @ObjectID = ObjectID, @BaseObject = SynonymBaseObject 
    FROM #Dependencies WHERE Processed = 0;

    -- If the Object is a Synonym, Find its Base Table/View
    IF @BaseObject IS NOT NULL
    BEGIN
        INSERT INTO #Dependencies (ObjectID, ObjectName, ObjectType, SynonymBaseObject, ResolvedBaseObject)
        SELECT 
            obj.object_id,
            obj.name,
            obj.type_desc,
            NULL AS SynonymBaseObject, -- It's now resolved
            obj.name AS ResolvedBaseObject
        FROM sys.objects obj
        WHERE obj.name = PARSENAME(@BaseObject, 1) -- Extract only the table/view name
        AND NOT EXISTS (SELECT 1 FROM #Dependencies WHERE ObjectName = obj.name);
    END

    -- Mark Processed
    UPDATE #Dependencies SET Processed = 1 WHERE ObjectID = @ObjectID OR SynonymBaseObject = @BaseObject;
END

-- Step 3: Return Final Results (Base Tables, Views, Functions, Synonyms)
SELECT DISTINCT 
    ObjectName, 
    ObjectType,
    COALESCE(SynonymBaseObject, 'Direct Reference') AS SynonymUsed,
    ResolvedBaseObject AS FinalBaseObject
FROM #Dependencies
WHERE ObjectType IN ('USER_TABLE', 'SQL_SCALAR_FUNCTION', 'SQL_TABLE_VALUED_FUNCTION', 'VIEW', 'SYNONYM')
ORDER BY ObjectType, ObjectName;

-- Cleanup
DROP TABLE #Dependencies;

--------
DECLARE @ViewOrProcedureName NVARCHAR(255) = 'YourViewOrProcedureName'; -- Replace with your object name
DECLARE @SQL NVARCHAR(MAX);
DECLARE @ObjectID INT, @BaseObject NVARCHAR(4000);

-- Temporary Table to Store Dependencies
IF OBJECT_ID('tempdb..#Dependencies') IS NOT NULL DROP TABLE #Dependencies;
CREATE TABLE #Dependencies (
    ObjectID INT NULL,
    ObjectName NVARCHAR(255),
    ObjectType NVARCHAR(255),
    SynonymBaseObject NVARCHAR(4000),
    ResolvedBaseObject NVARCHAR(4000),
    Processed BIT DEFAULT 0
);

-- Step 1: Insert Direct Dependencies (Tables, Views, Functions, Synonyms)
INSERT INTO #Dependencies (ObjectID, ObjectName, ObjectType, SynonymBaseObject, ResolvedBaseObject)
SELECT 
    dep.referenced_id, 
    obj.name,
    obj.type_desc,
    syn.base_object_name AS SynonymBaseObject,
    COALESCE(syn.base_object_name, obj.name) AS ResolvedBaseObject
FROM sys.sql_expression_dependencies dep
INNER JOIN sys.objects obj ON dep.referenced_id = obj.object_id
LEFT JOIN sys.synonyms syn ON obj.name = syn.name  -- Resolving synonyms
WHERE dep.referencing_id = OBJECT_ID(@ViewOrProcedureName);

-- Step 2: Process Dependencies Recursively Until All Base Tables Are Found
WHILE EXISTS (SELECT 1 FROM #Dependencies WHERE Processed = 0)
BEGIN
    -- Get Next Unprocessed Object
    SELECT TOP 1 @ObjectID = ObjectID, @BaseObject = SynonymBaseObject 
    FROM #Dependencies WHERE Processed = 0;

    -- If the Object is a Synonym, Find its Base Table/View
    IF @BaseObject IS NOT NULL
    BEGIN
        INSERT INTO #Dependencies (ObjectID, ObjectName, ObjectType, SynonymBaseObject, ResolvedBaseObject)
        SELECT 
            obj.object_id,
            obj.name,
            obj.type_desc,
            NULL AS SynonymBaseObject, -- It's now resolved
            obj.name AS ResolvedBaseObject
        FROM sys.objects obj
        WHERE obj.name = PARSENAME(@BaseObject, 1) -- Extract only the table/view name
        AND NOT EXISTS (SELECT 1 FROM #Dependencies WHERE ObjectName = obj.name);
    END

    -- If the Object is a View, Find the Base Tables It References
    IF EXISTS (SELECT 1 FROM sys.views WHERE object_id = @ObjectID)
    BEGIN
        INSERT INTO #Dependencies (ObjectID, ObjectName, ObjectType, SynonymBaseObject, ResolvedBaseObject)
        SELECT 
            dep.referenced_id, 
            obj.name,
            obj.type_desc,
            NULL AS SynonymBaseObject,
            obj.name AS ResolvedBaseObject
        FROM sys.sql_expression_dependencies dep
        INNER JOIN sys.objects obj ON dep.referenced_id = obj.object_id
        WHERE dep.referencing_id = @ObjectID
        AND NOT EXISTS (SELECT 1 FROM #Dependencies WHERE ObjectName = obj.name);
    END

    -- Mark Processed
    UPDATE #Dependencies SET Processed = 1 WHERE ObjectID = @ObjectID OR SynonymBaseObject = @BaseObject;
END

-- Step 3: Return Final Results (Base Tables, Views, Functions, Synonyms)
SELECT DISTINCT 
    ObjectName, 
    ObjectType,
    COALESCE(SynonymBaseObject, 'Direct Reference') AS SynonymUsed,
    ResolvedBaseObject AS FinalBaseObject
FROM #Dependencies
WHERE ObjectType IN ('USER_TABLE', 'SQL_SCALAR_FUNCTION', 'SQL_TABLE_VALUED_FUNCTION', 'VIEW', 'SYNONYM')
ORDER BY ObjectType, ObjectName;

-- Cleanup
DROP TABLE #Dependencies;

--------------------
DECLARE @ViewOrProcedureName NVARCHAR(255) = 'YourViewOrProcedureName'; -- Replace with your object name
DECLARE @SQL NVARCHAR(MAX);
DECLARE @ObjectID INT, @BaseObject NVARCHAR(4000);

-- Temporary Table to Store Dependencies
IF OBJECT_ID('tempdb..#Dependencies') IS NOT NULL DROP TABLE #Dependencies;
CREATE TABLE #Dependencies (
    ObjectID INT NULL,
    ObjectName NVARCHAR(255),
    ObjectType NVARCHAR(255),
    SynonymBaseObject NVARCHAR(4000),
    ResolvedBaseObject NVARCHAR(4000),
    Processed BIT DEFAULT 0
);

-- Step 1: Insert Direct Dependencies (Tables, Views, Functions, Synonyms)
INSERT INTO #Dependencies (ObjectID, ObjectName, ObjectType, SynonymBaseObject, ResolvedBaseObject)
SELECT 
    dep.referenced_id, 
    obj.name,
    obj.type_desc,
    syn.base_object_name AS SynonymBaseObject,
    COALESCE(syn.base_object_name, obj.name) AS ResolvedBaseObject
FROM sys.sql_expression_dependencies dep
INNER JOIN sys.objects obj ON dep.referenced_id = obj.object_id
LEFT JOIN sys.synonyms syn ON obj.name = syn.name  -- Resolving synonyms
WHERE dep.referencing_id = OBJECT_ID(@ViewOrProcedureName); -- Replace with your view or stored procedure

-- Step 2: Recursive CTE to resolve dependencies of views, synonyms, etc.
WITH RecursiveDependencies AS (
    -- Base case: Starting with the initial view or stored procedure
    SELECT 
        dep.referenced_id AS ObjectID,
        obj.name AS ObjectName,
        obj.type_desc AS ObjectType,
        syn.base_object_name AS SynonymBaseObject,
        COALESCE(syn.base_object_name, obj.name) AS ResolvedBaseObject,
        1 AS Depth
    FROM sys.sql_expression_dependencies dep
    INNER JOIN sys.objects obj ON dep.referenced_id = obj.object_id
    LEFT JOIN sys.synonyms syn ON obj.name = syn.name  -- Resolving synonyms
    WHERE dep.referencing_id = OBJECT_ID(@ViewOrProcedureName)

    UNION ALL

    -- Recursive case: Resolving further dependencies (views, synonyms, etc.)
    SELECT 
        dep.referenced_id AS ObjectID,
        obj.name AS ObjectName,
        obj.type_desc AS ObjectType,
        syn.base_object_name AS SynonymBaseObject,
        COALESCE(syn.base_object_name, obj.name) AS ResolvedBaseObject,
        r.Depth + 1 AS Depth
    FROM sys.sql_expression_dependencies dep
    INNER JOIN sys.objects obj ON dep.referenced_id = obj.object_id
    INNER JOIN sys.synonyms syn ON obj.name = syn.name  -- Resolving synonyms
    JOIN RecursiveDependencies r ON r.ObjectID = dep.referencing_id
    WHERE dep.referencing_id != dep.referenced_id  -- Prevent cyclic references
)

-- Step 3: Return Final Results (Base Tables, Views, Functions, Synonyms)
SELECT DISTINCT 
    ObjectName, 
    ObjectType,
    COALESCE(SynonymBaseObject, 'Direct Reference') AS SynonymUsed,
    ResolvedBaseObject AS FinalBaseObject,
    Depth
FROM RecursiveDependencies
WHERE ObjectType IN ('USER_TABLE', 'VIEW', 'SQL_SCALAR_FUNCTION', 'SQL_TABLE_VALUED_FUNCTION', 'SYNONYM')
ORDER BY Depth, ObjectType, ObjectName;

-- Cleanup
DROP TABLE IF EXISTS #Dependencies;

------------------
-- Declare the entity (table or view) name
DECLARE @EntityName NVARCHAR(128) = 'YourEntityName'; -- Replace 'YourEntityName' with the actual entity name

-- Query to find dependencies and relationships of the entity
SELECT 
    referencing_entity.name AS ReferencingEntity,
    referenced_entity.name AS ReferencedEntity,
    fk.name AS FK_ConstraintName,
    fk.type_desc AS FK_Type,
    fk.parent_object_id AS ParentObjectId,
    fk.referenced_object_id AS ReferencedObjectId,
    fk.is_disabled AS IsFK_Disabled,
    fk.is_enabled AS IsFK_Enabled,
    fk.delete_referential_action_desc AS OnDeleteAction,
    fk.update_referential_action_desc AS OnUpdateAction
FROM 
    sys.foreign_keys AS fk
JOIN 
    sys.tables AS referencing_entity
    ON fk.parent_object_id = referencing_entity.object_id
JOIN 
    sys.tables AS referenced_entity
    ON fk.referenced_object_id = referenced_entity.object_id
WHERE 
    referencing_entity.name = @EntityName OR referenced_entity.name = @EntityName
ORDER BY 
    ReferencingEntity, ReferencedEntity;

-------------
// constraints of entities
WITH Constraints AS (
    -- Foreign Key Constraints
    SELECT 
        fk.name AS ConstraintName,
        'FOREIGN KEY' AS ConstraintType,
        OBJECT_NAME(fk.parent_object_id) AS ReferencingTable,
        COL_NAME(fkc.parent_object_id, fkc.parent_column_id) AS ReferencingColumn,
        OBJECT_NAME(fk.referenced_object_id) AS ReferencedTable,
        COL_NAME(fkc.referenced_object_id, fkc.referenced_column_id) AS ReferencedColumn,
        fk.delete_referential_action_desc AS DeleteAction,
        fk.update_referential_action_desc AS UpdateAction
    FROM sys.foreign_keys fk
    JOIN sys.foreign_key_columns fkc 
        ON fk.object_id = fkc.constraint_object_id

    UNION ALL

    -- Primary Key Constraints
    SELECT 
        kc.name AS ConstraintName,
        'PRIMARY KEY' AS ConstraintType,
        t.name AS ReferencingTable,
        COL_NAME(kc.parent_object_id, kcu.column_id) AS ReferencingColumn,
        NULL AS ReferencedTable,
        NULL AS ReferencedColumn,
        NULL AS DeleteAction,
        NULL AS UpdateAction
    FROM sys.key_constraints kc
    JOIN sys.tables t 
        ON kc.parent_object_id = t.object_id
    JOIN sys.index_columns kcu 
        ON kc.parent_object_id = kcu.object_id 
        AND kc.unique_index_id = kcu.index_id
    WHERE kc.type = 'PK'

    UNION ALL

    -- Unique Constraints
    SELECT 
        kc.name AS ConstraintName,
        'UNIQUE CONSTRAINT' AS ConstraintType,
        t.name AS ReferencingTable,
        COL_NAME(kc.parent_object_id, kcu.column_id) AS ReferencingColumn,
        NULL AS ReferencedTable,
        NULL AS ReferencedColumn,
        NULL AS DeleteAction,
        NULL AS UpdateAction
    FROM sys.key_constraints kc
    JOIN sys.tables t 
        ON kc.parent_object_id = t.object_id
    JOIN sys.index_columns kcu 
        ON kc.parent_object_id = kcu.object_id 
        AND kc.unique_index_id = kcu.index_id
    WHERE kc.type = 'UQ'

    UNION ALL

    -- Check Constraints
    SELECT 
        cc.name AS ConstraintName,
        'CHECK CONSTRAINT' AS ConstraintType,
        t.name AS ReferencingTable,
        COL_NAME(cc.parent_object_id, c.column_id) AS ReferencingColumn,
        NULL AS ReferencedTable,
        NULL AS ReferencedColumn,
        NULL AS DeleteAction,
        NULL AS UpdateAction
    FROM sys.check_constraints cc
    JOIN sys.tables t 
        ON cc.parent_object_id = t.object_id
    JOIN sys.columns c 
        ON cc.parent_object_id = c.object_id 
        AND cc.parent_column_id = c.column_id

    UNION ALL

    -- Default Constraints
    SELECT 
        dc.name AS ConstraintName,
        'DEFAULT CONSTRAINT' AS ConstraintType,
        t.name AS ReferencingTable,
        COL_NAME(dc.parent_object_id, dc.parent_column_id) AS ReferencingColumn,
        NULL AS ReferencedTable,
        NULL AS ReferencedColumn,
        NULL AS DeleteAction,
        NULL AS UpdateAction
    FROM sys.default_constraints dc
    JOIN sys.tables t 
        ON dc.parent_object_id = t.object_id
)
SELECT *
FROM Constraints
ORDER BY ReferencingTable, ConstraintType;

-----------------------
-- Declare the entity name to search
DECLARE @EntityName NVARCHAR(128) = 'YourEntityName'; -- Replace with the name of the table or view you're interested in

WITH Constraints AS (
    -- Foreign Key Constraints
    SELECT 
        fk.name AS ConstraintName,
        'FOREIGN KEY' AS ConstraintType,
        OBJECT_NAME(fk.parent_object_id) AS ReferencingTable,
        COL_NAME(fkc.parent_object_id, fkc.parent_column_id) AS ReferencingColumn,
        OBJECT_NAME(fk.referenced_object_id) AS ReferencedTable,
        COL_NAME(fkc.referenced_object_id, fkc.referenced_column_id) AS ReferencedColumn,
        fk.delete_referential_action_desc AS DeleteAction,
        fk.update_referential_action_desc AS UpdateAction
    FROM sys.foreign_keys fk
    JOIN sys.foreign_key_columns fkc 
        ON fk.object_id = fkc.constraint_object_id

    UNION ALL

    -- Primary Key Constraints
    SELECT 
        kc.name AS ConstraintName,
        'PRIMARY KEY' AS ConstraintType,
        t.name AS ReferencingTable,
        COL_NAME(kc.parent_object_id, kcu.column_id) AS ReferencingColumn,
        NULL AS ReferencedTable,
        NULL AS ReferencedColumn,
        NULL AS DeleteAction,
        NULL AS UpdateAction
    FROM sys.key_constraints kc
    JOIN sys.tables t 
        ON kc.parent_object_id = t.object_id
    JOIN sys.index_columns kcu 
        ON kc.parent_object_id = kcu.object_id 
        AND kc.unique_index_id = kcu.index_id
    WHERE kc.type = 'PK'

    UNION ALL

    -- Unique Constraints
    SELECT 
        kc.name AS ConstraintName,
        'UNIQUE CONSTRAINT' AS ConstraintType,
        t.name AS ReferencingTable,
        COL_NAME(kc.parent_object_id, kcu.column_id) AS ReferencingColumn,
        NULL AS ReferencedTable,
        NULL AS ReferencedColumn,
        NULL AS DeleteAction,
        NULL AS UpdateAction
    FROM sys.key_constraints kc
    JOIN sys.tables t 
        ON kc.parent_object_id = t.object_id
    JOIN sys.index_columns kcu 
        ON kc.parent_object_id = kcu.object_id 
        AND kc.unique_index_id = kcu.index_id
    WHERE kc.type = 'UQ'

    UNION ALL

    -- Check Constraints
    SELECT 
        cc.name AS ConstraintName,
        'CHECK CONSTRAINT' AS ConstraintType,
        t.name AS ReferencingTable,
        COL_NAME(cc.parent_object_id, c.column_id) AS ReferencingColumn,
        NULL AS ReferencedTable,
        NULL AS ReferencedColumn,
        NULL AS DeleteAction,
        NULL AS UpdateAction
    FROM sys.check_constraints cc
    JOIN sys.tables t 
        ON cc.parent_object_id = t.object_id
    JOIN sys.columns c 
        ON cc.parent_object_id = c.object_id 
        AND cc.parent_column_id = c.column_id

    UNION ALL

    -- Default Constraints
    SELECT 
        dc.name AS ConstraintName,
        'DEFAULT CONSTRAINT' AS ConstraintType,
        t.name AS ReferencingTable,
        COL_NAME(dc.parent_object_id, dc.parent_column_id) AS ReferencingColumn,
        NULL AS ReferencedTable,
        NULL AS ReferencedColumn,
        NULL AS DeleteAction,
        NULL AS UpdateAction
    FROM sys.default_constraints dc
    JOIN sys.tables t 
        ON dc.parent_object_id = t.object_id
    JOIN sys.columns c 
        ON dc.parent_object_id = c.object_id
        AND dc.parent_column_id = c.column_id

    UNION ALL

    -- Composite Key Constraints (Primary or Unique on Multiple Columns)
    SELECT 
        kc.name AS ConstraintName,
        'COMPOSITE KEY' AS ConstraintType,
        t.name AS ReferencingTable,
        COL_NAME(kc.parent_object_id, kcu.column_id) AS ReferencingColumn,
        NULL AS ReferencedTable,
        NULL AS ReferencedColumn,
        NULL AS DeleteAction,
        NULL AS UpdateAction
    FROM sys.key_constraints kc
    JOIN sys.tables t 
        ON kc.parent_object_id = t.object_id
    JOIN sys.index_columns kcu 
        ON kc.parent_object_id = kcu.object_id 
        AND kc.unique_index_id = kcu.index_id
    WHERE kc.type IN ('PK', 'UQ')

    UNION ALL

    -- Spatial Index Constraints
    SELECT 
        idx.name AS ConstraintName,
        'SPATIAL INDEX' AS ConstraintType,
        t.name AS ReferencingTable,
        NULL AS ReferencingColumn,
        NULL AS ReferencedTable,
        NULL AS ReferencedColumn,
        NULL AS DeleteAction,
        NULL AS UpdateAction
    FROM sys.indexes idx
    JOIN sys.tables t 
        ON idx.object_id = t.object_id
    WHERE idx.type_desc = 'SPATIAL'

    UNION ALL

    -- XML Index Constraints
    SELECT 
        idx.name AS ConstraintName,
        'XML INDEX' AS ConstraintType,
        t.name AS ReferencingTable,
        NULL AS ReferencingColumn,
        NULL AS ReferencedTable,
        NULL AS ReferencedColumn,
        NULL AS DeleteAction,
        NULL AS UpdateAction
    FROM sys.indexes idx
    JOIN sys.tables t 
        ON idx.object_id = t.object_id
    WHERE idx.type_desc = 'XML'

    UNION ALL

    -- Full-text Index Constraints
    SELECT 
        idx.name AS ConstraintName,
        'FULLTEXT INDEX' AS ConstraintType,
        t.name AS ReferencingTable,
        NULL AS ReferencingColumn,
        NULL AS ReferencedTable,
        NULL AS ReferencedColumn,
        NULL AS DeleteAction,
        NULL AS UpdateAction
    FROM sys.indexes idx
    JOIN sys.tables t 
        ON idx.object_id = t.object_id
    WHERE idx.type_desc = 'FULLTEXT'
)

-- Final query to get all the constraints with additional entity information
SELECT 
    c.ConstraintName,
    c.ConstraintType,
    c.ReferencingTable,
    c.ReferencingColumn,
    c.ReferencedTable,
    c.ReferencedColumn,
    c.DeleteAction,
    c.UpdateAction,
    t.is_ms_shipped,         -- Identify if the entity is a system (box) entity
    t.is_locked,             -- Check if the entity is locked for editing
    t.create_date,           -- Creation date of the entity
    t.modify_date            -- Last modified date of the entity
FROM Constraints c
JOIN sys.tables t 
    ON c.ReferencingTable = t.name
WHERE c.ReferencingTable = @EntityName OR c.ReferencedTable = @EntityName
ORDER BY c.ReferencingTable, c.ConstraintType;

------------------
-- Declare the view name for which we want to find dependencies
DECLARE @ViewName NVARCHAR(128) = 'YourViewName';  -- Replace with your view name

-- Find the dependencies for the given view using system views with level information
WITH Dependencies AS (
    -- Base query to find the direct dependencies of the view
    SELECT 
        OBJECT_NAME(d.referencing_id) AS ReferencingEntity,
        o.type_desc AS ObjectType,
        OBJECT_NAME(d.referenced_id) AS ReferencedEntity,
        ro.type_desc AS ReferencedObjectType,
        1 AS Level  -- Direct dependency level is 1
    FROM sys.sql_expression_dependencies d
    JOIN sys.objects o ON d.referencing_id = o.object_id  -- Join to find referencing object type
    JOIN sys.objects ro ON d.referenced_id = ro.object_id -- Join to find referenced object type
    WHERE o.name = @ViewName  -- Filtering for the specific view name

    UNION ALL

    -- Recursively get dependencies if the referenced object is also a view or any other dependent entity
    SELECT 
        OBJECT_NAME(d.referencing_id) AS ReferencingEntity,
        o.type_desc AS ObjectType,
        OBJECT_NAME(d.referenced_id) AS ReferencedEntity,
        ro.type_desc AS ReferencedObjectType,
        d.Level + 1 AS Level  -- Increment the level for each recursive iteration
    FROM sys.sql_expression_dependencies d
    JOIN sys.objects o ON d.referencing_id = o.object_id
    JOIN sys.objects ro ON d.referenced_id = ro.object_id
    JOIN Dependencies dep ON dep.ReferencedEntity = OBJECT_NAME(d.referencing_id)  -- Join to recursive part
    WHERE ro.type_desc IN ('VIEW', 'SQL_SCALAR_FUNCTION', 'SQL_TABLE_VALUED_FUNCTION', 'USER_TABLE')  -- This includes views, functions, and tables
)

-- Select the unique dependencies and include the level column
SELECT DISTINCT
    ReferencingEntity,
    ObjectType,
    ReferencedEntity,
    ReferencedObjectType,
    Level  -- Include the dependency level
FROM Dependencies
ORDER BY Level, ReferencingEntity, ReferencedEntity;


--------------
// view dependencies recursive 
DECLARE @EntityName NVARCHAR(128) = 'YourViewOrProcedure';  -- Replace with your entity

-- Recursive CTE to find dependencies, including synonyms resolution
WITH Dependencies AS (
    -- Base query: Direct dependencies of the given entity (view/procedure)
    SELECT 
        OBJECT_NAME(d.referencing_id) AS ReferencingEntity,
        o.type_desc AS ObjectType,
        -- If referenced entity is a synonym, resolve its base object name
        COALESCE(
            PARSENAME(s.base_object_name, 1),  -- Extract actual table/view name from synonym
            OBJECT_NAME(d.referenced_id)      -- Use normal referenced entity name if not a synonym
        ) AS ReferencedEntity,
        -- Determine the type of the referenced entity
        COALESCE(ro.type_desc, 'SYNONYM') AS ReferencedObjectType,
        1 AS Level  -- Direct dependencies have level 1
    FROM sys.sql_expression_dependencies d
    JOIN sys.objects o ON d.referencing_id = o.object_id  -- Get referencing object details
    LEFT JOIN sys.objects ro ON d.referenced_id = ro.object_id  -- Get referenced object details
    LEFT JOIN sys.synonyms s ON ro.object_id = s.object_id  -- Check if referenced object is a synonym
    WHERE o.name = @EntityName

    UNION ALL

    -- Recursive query to find indirect dependencies (including synonyms resolution)
    SELECT 
        OBJECT_NAME(d.referencing_id) AS ReferencingEntity,
        o.type_desc AS ObjectType,
        COALESCE(
            PARSENAME(s.base_object_name, 1),  -- Extract actual table/view name from synonym
            OBJECT_NAME(d.referenced_id)      -- Use normal referenced entity name if not a synonym
        ) AS ReferencedEntity,
        COALESCE(ro.type_desc, 'SYNONYM') AS ReferencedObjectType,
        dep.Level + 1 AS Level  -- Increment the recursion level
    FROM sys.sql_expression_dependencies d
    JOIN sys.objects o ON d.referencing_id = o.object_id
    LEFT JOIN sys.objects ro ON d.referenced_id = ro.object_id
    LEFT JOIN sys.synonyms s ON ro.object_id = s.object_id
    JOIN Dependencies dep ON dep.ReferencedEntity = OBJECT_NAME(d.referencing_id)  -- Recursive join
    WHERE ro.type_desc IN ('VIEW', 'SQL_SCALAR_FUNCTION', 'SQL_TABLE_VALUED_FUNCTION', 'USER_TABLE', 'SYNONYM')
)

-- Final output: Display dependencies with resolved synonym targets
SELECT DISTINCT
    ReferencingEntity,
    ObjectType,
    ReferencedEntity,
    ReferencedObjectType,
    Level  -- Indicates depth of dependency
FROM Dependencies
ORDER BY Level, ReferencingEntity, ReferencedEntity;

-------------------------
// filtered fk constraints 
-- Define your list1 and list2 as table variables or use temp tables
DECLARE @list1 TABLE (TableName SYSNAME);
DECLARE @list2 TABLE (TableName SYSNAME);

-- Sample data
INSERT INTO @list1 (TableName) VALUES ('Orders'), ('Invoices');
INSERT INTO @list2 (TableName) VALUES ('Customers'), ('Products');

-- Query to get FK relationships from list1 tables to list2 tables
SELECT
    fk.name AS ForeignKeyName,
    OBJECT_SCHEMA_NAME(fk.parent_object_id) AS SourceSchema,
    src.name AS SourceTable,
    src_col.name AS SourceColumn,
    OBJECT_SCHEMA_NAME(fk.referenced_object_id) AS TargetSchema,
    tgt.name AS TargetTable,
    tgt_col.name AS TargetColumn
FROM sys.foreign_keys AS fk
JOIN sys.foreign_key_columns AS fkc ON fk.object_id = fkc.constraint_object_id
JOIN sys.tables AS src ON fkc.parent_object_id = src.object_id
JOIN sys.columns AS src_col ON fkc.parent_object_id = src_col.object_id AND fkc.parent_column_id = src_col.column_id
JOIN sys.tables AS tgt ON fkc.referenced_object_id = tgt.object_id
JOIN sys.columns AS tgt_col ON fkc.referenced_object_id = tgt_col.object_id AND fkc.referenced_column_id = tgt_col.column_id
WHERE src.name IN (SELECT TableName FROM @list1)
  AND tgt.name IN (SELECT TableName FROM @list2)
ORDER BY SourceTable, TargetTable, ForeignKeyName;

---------------
// find the row count of all table
SELECT 
    SCHEMA_NAME(t.schema_id) + '.' + t.name AS TableName,
    SUM(p.rows) AS RowCount
FROM 
    sys.tables AS t
INNER JOIN 
    sys.partitions AS p ON t.object_id = p.object_id
WHERE 
    p.index_id IN (0,1) -- 0: heap, 1: clustered index
GROUP BY 
    t.schema_id, t.name
ORDER BY 
    RowCount DESC;
---------

DECLARE @sql NVARCHAR(MAX) = '';

SELECT @sql += 
    'SELECT ''' + QUOTENAME(s.name) + '.' + QUOTENAME(t.name) + ''' AS TableName, COUNT(*) AS RowCount FROM ' + 
    QUOTENAME(s.name) + '.' + QUOTENAME(t.name) + ' UNION ALL '
FROM 
    sys.tables t
JOIN 
    sys.schemas s ON t.schema_id = s.schema_id;

-- Remove trailing UNION ALL
SET @sql = LEFT(@sql, LEN(@sql) - 10);

EXEC sp_executesql @sql;

-----------
