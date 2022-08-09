package it.units;

import net.efabrika.util.DBTablePrinter;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Main {
    static final String DB_NAME = "uwcad";
    static String connection_string;
    static final String EXIT_COMMAND = "exit";

    public static void main(String[] args) throws SQLException {
        Scanner sc = SingletonScanner.getScanner();
        String user = SingletonScanner.getString("Nome utente con cui collegarsi al DBMS:");
        String password = SingletonScanner.getString("Password:");
        connection_string = String.format("jdbc:mariadb://localhost:3306/%s?user=%s&password=%s",
            DB_NAME, user, password);
        Connection connection = SingletonConnection.getConnection();
        System.out.println("=== COLLEGIO DEL MONDO UNITO DELL'ADRIATICO - DEMO DATABASE INTERNO ===");
        while (true) {
            System.out.print(
                "\nOperazioni disponibili (digitare la cifra corrispondente):\n" +
                    "\t 1. Visualizza i posti liberi delle residenze\n" +
                    "\t 2. Visualizza il numero di iscritti di ciascun corso\n" +
                    "\t 3. Visualizza informazioni su attività CAS\n" +
                    "\t 4. Cerca studente per cognome\n" +
                    "\t 5. Cerca personale per cognome\n" +
                    "\t 6. Cerca attività CAS per nome\n" +
                    "\t 7. Cerca istruttore attività CAS per cognome\n" +
                    "\t 9. Aggiungi dati al database\n\n" +
                    "\t Digitare 'exit' per uscire\n\n" +
                    "> "
            );
            String input = sc.nextLine();
            if (input.equals(EXIT_COMMAND)) {
                break;
            }
            byte choice;
            try {
                choice = Byte.parseByte(input);
            } catch (InputMismatchException | NumberFormatException e) {
                System.out.println("\n===== Input non valido! =====\n");
                continue;
            }
            startOperation(choice);
            if (choice != 9) {
                System.out.println("Premere invio per tornare al menù principale");
                sc.nextLine();
            }
        }
        System.out.println("EXITING...");
        sc.close();
        connection.close();
    }

    static void startOperation(byte operation) throws SQLException {
        Connection connection = SingletonConnection.getConnection();
        PreparedStatement preparedStatement;
        switch (operation) {
            case 1 -> preparedStatement = connection.prepareStatement("SELECT * FROM postiliberi");
            case 2 -> preparedStatement = connection.prepareStatement("SELECT * FROM iscrizionicorsi");
            case 3 -> preparedStatement = connection.prepareStatement("SELECT * FROM infocas");
            case 4, 5, 7 -> preparedStatement = searchBySurname(operation);
            case 6 -> preparedStatement = searchCASByName();
            case 9 -> {
                handleInsertion();
                return;
            }
            default -> {
                System.out.println("Operazione non valida");
                return;
            }
        }
        ResultSet rs = preparedStatement.executeQuery();
        DBTablePrinter.printResultSet(rs);
    }

    static PreparedStatement searchBySurname(byte option) throws SQLException {
        Connection connection = SingletonConnection.getConnection();
        String searchStr = SingletonScanner.getString("Inserisci (parte del) cognome:");
        String table;
        switch (option) {
            case 4 -> table = "studenti";
            case 5 -> table = "personale";
            case 7 -> table = "istruttori";
            // default case is unreachable at runtime, fixes error
            default -> table = "";
        }
        // No risks of SQL injection, 'table' is not an user-input string
        PreparedStatement preparedStatement = connection.prepareStatement(
            "SELECT id, nome, cognome FROM " + table + " WHERE cognome LIKE CONCAT('%', ?, '%')");
        preparedStatement.setString(1, searchStr);
        return preparedStatement;
    }

    static PreparedStatement searchCASByName() throws SQLException {
        Connection connection = SingletonConnection.getConnection();
        String searchStr = SingletonScanner.getString("Inserisci (parte del) nome dell'attività:");
        PreparedStatement preparedStatement = connection.prepareStatement(
            "SELECT * FROM cas WHERE attività LIKE CONCAT('%', ?, '%')");
        preparedStatement.setString(1, searchStr);
        return preparedStatement;
    }

    static void handleInsertion() throws SQLException {
        while (true) {
            System.out.print(
                "\nOperazioni disponibili (digitare la cifra corrispondente):\n" +
                    "\t 1. Aggiungi studente\n" +
                    "\t 2. Aggiungi membro del personale\n" +
                    "\t 3. Associa studente a corso\n" +
                    "\t 4. Associa studente ad attività CAS\n" +
                    "\t 5. Associa referente ad attività CAS\n" +
                    "\t 6. Associa studente a residenza\n\n" +
                    "\t Digitare 'exit' per tornare al menù precedente\n\n" +
                    "> "
            );
            Scanner sc = SingletonScanner.getScanner();
            String input = sc.nextLine();
            if (input.equals(EXIT_COMMAND)) {
                break;
            }
            byte choice;
            try {
                choice = Byte.parseByte(input);
            } catch (InputMismatchException | NumberFormatException e) {
                System.out.println("\n===== Input non valido! =====\n");
                continue;
            }
            switch (choice) {
                case 1 -> addStudente();
                case 2 -> addPersonale();
                case 3 -> bindStudent2Course();
                case 4 -> bindStudent2CAS();
                case 5 -> bindEmployee2CAS();
                case 6 -> bindStudent2House();
                default -> System.out.println("Operazione non valida");
            }
        }
    }

    static void addStudente() throws SQLException {
        Connection connection = SingletonConnection.getConnection();
        String name = SingletonScanner.getString("Nome:");
        String surname = SingletonScanner.getString("Cognome:");
        String nationality = SingletonScanner.getString("Nazionalità:");
        CallableStatement cst = connection.prepareCall("{call addStudente(?, ?, ?)}");
        setParameters(cst, name, surname, nationality);
        execStatement(cst);
    }

    static void addPersonale() throws SQLException {
        Connection connection = SingletonConnection.getConnection();
        String name = SingletonScanner.getString("Nome:");
        String surname = SingletonScanner.getString("Cognome:");
        String role = SingletonScanner.getString("Reparto:");
        CallableStatement cst = connection.prepareCall("{call addPersonale(?, ?, ?)}");
        setParameters(cst, name, surname, role);
        execStatement(cst);
    }

    static void bindStudent2Course() throws SQLException {
        Connection connection = SingletonConnection.getConnection();
        String id = SingletonScanner.getString("ID studente:");
        String course = SingletonScanner.getString("Codice corso:");
        CallableStatement cst = connection.prepareCall("{call addFrequenza(?, ?)}");
        setParameters(cst, id, course);
        execStatement(cst);
    }

    static void bindStudent2CAS() throws SQLException {
        Connection connection = SingletonConnection.getConnection();
        String id = SingletonScanner.getString("ID studente:");
        String name = SingletonScanner.getString("(Parte del) nome attivtà CAS:");
        CallableStatement cst = connection.prepareCall("{call addPartecipazione(?, ?)}");
        setParameters(cst, id, name);
        execStatement(cst);
    }

    static void bindEmployee2CAS() throws SQLException {
        Connection connection = SingletonConnection.getConnection();
        String name = SingletonScanner.getString("(Parte del) nome attivtà CAS:");
        String id = SingletonScanner.getString("ID impiegato:");
        CallableStatement cst = connection.prepareCall("{call setReferente(?, ?)}");
        setParameters(cst, name, id);
        execStatement(cst);
    }

    static void bindStudent2House() throws SQLException {
        Connection connection = SingletonConnection.getConnection();
        String id = SingletonScanner.getString("ID studente:");
        String house = SingletonScanner.getString("Nome residenza:");
        CallableStatement cst = connection.prepareCall("{call setResidenza(?, ?)}");
        setParameters(cst, id, house);
        execStatement(cst);
    }

    static void execStatement(CallableStatement statement) {
        try {
            statement.execute();
        } catch (SQLException e) {
            System.err.println("ERRORE: l'operazione non è andata a buon fine. Ricontrolla i dati immessi.");
        }
    }

    static void setParameters(CallableStatement statement, String... parameters) throws SQLException {
        for (int i = 0; i < parameters.length; i++) {
            statement.setString(i + 1, parameters[i]);
        }
    }
}
