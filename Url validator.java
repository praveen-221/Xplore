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
