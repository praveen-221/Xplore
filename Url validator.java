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
