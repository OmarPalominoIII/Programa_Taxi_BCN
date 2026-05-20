package user_interface.admin;

import config.DatabaseConnection;
import dao.impl.ServiceRequestImplDAO;
import dao.impl.TaxiImplDAO;
import manager.ReportManager;
import manager.ServiceManager;
import services.Sendable;

import javax.swing.*;
import java.sql.Connection;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {

        System.setProperty("http.agent", "MyTaxiProject_v1");

        try {

            Connection connection = DatabaseConnection.getConnection();

            TaxiImplDAO taxiDAO = new TaxiImplDAO(connection);
            ServiceRequestImplDAO serviceRequestsDAO = new ServiceRequestImplDAO(connection);

            ReportManager reportManager = new ReportManager(serviceRequestsDAO);
            Sendable messenger = message -> System.out.println("[System]: " + message);

            ServiceManager serviceManager = new ServiceManager(reportManager, messenger, serviceRequestsDAO, taxiDAO);

            SwingUtilities.invokeLater(() -> {
                UIMenuAdmin menuAdmin = new UIMenuAdmin(serviceManager, reportManager, taxiDAO);
                menuAdmin.setVisible(true);
            });

        } catch (SQLException e) {
            System.err.println("Unable to initialize the administration ecosystem: " + e.getMessage());
        }
    }
}
