oimport java.util.regex.*;
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
